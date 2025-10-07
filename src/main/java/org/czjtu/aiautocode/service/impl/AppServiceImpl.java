package org.czjtu.aiautocode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.czjtu.aiautocode.exception.BusinessException;
import org.czjtu.aiautocode.exception.ErrorCode;
import org.czjtu.aiautocode.mapper.AppMapper;
import org.czjtu.aiautocode.model.dto.app.AppQueryRequest;
import org.czjtu.aiautocode.model.entity.App;
import org.czjtu.aiautocode.model.entity.User;
import org.czjtu.aiautocode.model.vo.AppVO;
import org.czjtu.aiautocode.model.vo.UserVO;
import org.czjtu.aiautocode.service.AppService;
import org.czjtu.aiautocode.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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


}




