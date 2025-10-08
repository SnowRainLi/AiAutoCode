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
import org.czjtu.aiautocode.constant.AppConstant;
import org.czjtu.aiautocode.core.AICodeGeneratorFacade;
import org.czjtu.aiautocode.exception.BusinessException;
import org.czjtu.aiautocode.exception.ErrorCode;
import org.czjtu.aiautocode.exception.ThrowUtils;
import org.czjtu.aiautocode.mapper.AppMapper;
import org.czjtu.aiautocode.model.dto.app.AppQueryRequest;
import org.czjtu.aiautocode.model.entity.App;
import org.czjtu.aiautocode.model.entity.User;
import org.czjtu.aiautocode.model.enums.CodeGenTypeEnum;
import org.czjtu.aiautocode.model.vo.AppVO;
import org.czjtu.aiautocode.model.vo.UserVO;
import org.czjtu.aiautocode.service.AppService;
import org.czjtu.aiautocode.service.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 画外人易朽
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2025-10-07 16:52:13
*/
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService{
    @Resource
    private UserService userService;

    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;

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
        //5.调用ai 生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
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
        return String.format("%s/%s",AppConstant.CODE_DEPLOY_HOST, deployKey);
    }


}




