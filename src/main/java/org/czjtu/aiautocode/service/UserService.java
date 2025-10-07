package org.czjtu.aiautocode.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.czjtu.aiautocode.model.dto.UserQueryRequest;
import org.czjtu.aiautocode.model.entity.User;
import org.czjtu.aiautocode.model.vo.LoginUserVO;
import org.czjtu.aiautocode.model.vo.UserVO;

import java.util.List;

/**
* @author 画外人易朽
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-10-05 17:49:46
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 登录
     *
     * @param user
     * @return 脱敏后的用户信息
     */
    LoginUserVO getUserLoginVO(User user);

    /**
     * 登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     *
     * @param  user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息(分页)
     *
     * @param  userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户登出
     * @param request
     * @return 登出成功/失败
     */
    boolean userLogout(HttpServletRequest request);
    /**
     * 加密密码
     *
     * @param password
     * @return
     */
    String getEncryptPassword(String password);


}

