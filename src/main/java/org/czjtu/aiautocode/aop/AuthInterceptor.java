package org.czjtu.aiautocode.aop;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.czjtu.aiautocode.annotation.AuthCheck;
import org.czjtu.aiautocode.exception.BusinessException;
import org.czjtu.aiautocode.exception.ErrorCode;
import org.czjtu.aiautocode.model.entity.User;
import org.czjtu.aiautocode.model.enums.UserRoleEnum;
import org.czjtu.aiautocode.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;
    /**
     * 执行拦截
     * @param joinPoint 切面点
     * @param authCheck 权限校验
     * @return
     */

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck)throws Throwable{
        String mustRole = authCheck.mustRole();
        //获取请求
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //不需要权限直接放行
        if (mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //必须要权限才能通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        //无权限则拒绝
        if (userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //必须有管理员权限才能通过，但当前登录用户没有管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)&& !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过普通用户权限放行
        return joinPoint.proceed();

    }

}
