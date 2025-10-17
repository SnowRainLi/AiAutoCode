package org.czjtu.aiautocode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.czjtu.aiautocode.ai.AiCodeGenTypeRoutingService;
import org.czjtu.aiautocode.ai.AiCodeGenTypeRoutingServiceFactory;
import org.czjtu.aiautocode.constant.AppConstant;
import org.czjtu.aiautocode.core.AICodeGeneratorFacade;
import org.czjtu.aiautocode.core.build.VueProjectBuilder;
import org.czjtu.aiautocode.core.handler.StreamHandlerExecutor;
import org.czjtu.aiautocode.exception.BusinessException;
import org.czjtu.aiautocode.exception.ErrorCode;
import org.czjtu.aiautocode.exception.ThrowUtils;
import org.czjtu.aiautocode.mapper.AppMapper;
import org.czjtu.aiautocode.model.dto.app.AppAddRequest;
import org.czjtu.aiautocode.model.dto.app.AppQueryRequest;
import org.czjtu.aiautocode.model.entity.App;
import org.czjtu.aiautocode.model.entity.User;
import org.czjtu.aiautocode.model.enums.ChatHistoryMessageTypeEnum;
import org.czjtu.aiautocode.model.enums.CodeGenTypeEnum;
import org.czjtu.aiautocode.model.vo.AppVO;
import org.czjtu.aiautocode.model.vo.UserVO;
import org.czjtu.aiautocode.service.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 画外人易朽
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2025-10-07 16:52:13
*/
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService{
    @Resource
    private UserService userService;

    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用 AI 智能选择代码生成类型（多例模式）
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }




    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(appName != null, "appName", appName);
        queryWrapper.like(cover != null, "cover", cover);
        queryWrapper.like(initPrompt != null, "initPrompt", initPrompt);
        queryWrapper.eq(codeGenType != null, "codeGenType", codeGenType);
        queryWrapper.eq(deployKey != null, "deployKey", deployKey);
        queryWrapper.eq(priority != null, "priority", priority);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.orderBy(sortField != null && sortOrder != null,
                "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User longinUser) {
        //1. 校验
        ThrowUtils.throwIf(appId==null||appId<=0, ErrorCode.PARAMS_ERROR, "应用参数错误");
        ThrowUtils.throwIf(StrUtil.isBlank( message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        //2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //3.权限校验，仅本人可以与自己的应用对话
        if (!app.getUserId().equals(longinUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
        }
        //4.获取代码生成模式
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum==null, ErrorCode.PARAMS_ERROR, "代码生成模式错误");
        //调用ai前先保存用户消息
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), longinUser.getId());
        //5.调用ai 生成代码(流式)
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        //收集ai响应内容，并在响应完成后保存到对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, longinUser,codeGenTypeEnum);
    }

    @Override
    public String deployApp(Long appId, User longinUser) {
        //1. 校验
        ThrowUtils.throwIf(appId==null||appId<=0, ErrorCode.PARAMS_ERROR, "应用参数错误");
        ThrowUtils.throwIf(longinUser==null, ErrorCode.NOT_LOGIN_ERROR, "未登录");
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //2.权限校验，仅本人可以部署自己的应用
        if (!app.getUserId().equals(longinUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
        }
        //检查是否有deployKey
        String deployKey = app.getDeployKey();
        //若没有则生成6位的deployKey(字母加数字)
        if (StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6);
        }
        //获取代码生成类型，获取代码生成模式
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType+"_"+appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR+ File.separator+sourceDirName;
        //检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists()||!sourceDir.isDirectory()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成目录不存在，请生成应用");
        }
        //vue项目特殊构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum== CodeGenTypeEnum.VUE_PROJECT){
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            if (!buildSuccess){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "构建失败,请重试");
            }
            //检查dist 目录
            File distDir = new File(sourceDirPath,"dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "dist 目录不存在，请检查");
            //构建完成后将文件复制到部署目录
            sourceDir=distDir;
        }
        //复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR+ File.separator+deployKey;
        try {
            FileUtil.copyContent(sourceDir,new File(deployDirPath),true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败："+e.getMessage());
        }
        //更新应用信息
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(new Date());
        boolean updataResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updataResult, ErrorCode.SYSTEM_ERROR, "更新应用信息失败");

        //返回部署地址
        String appDeployUrl = String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
        //异步生成应用截图并更新封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }



    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }


    /**
     * 删除应用，删除应用时删除对话历史
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable  id){
        if (id == null){
            return false;
        }
        long appId = Long.parseLong(id.toString());
        if (appId <= 0){
            return false;
        }
        //删除对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用时删除对话历史失败：{}",e.getMessage());
        }
        //删除应用
        return  super.removeById(id);
    }

}




