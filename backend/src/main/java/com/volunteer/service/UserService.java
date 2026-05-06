package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.dto.UserUpdateRequest;
import com.volunteer.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /** 根据账号查找用户 */
    User findByAccount(String account);

    /** 用户登录，返回 token */
    String login(String account, String password);

    /** 用户注册 */
    void register(String name, String phone, String account, String password, String role);

    /** 更新用户信息 */
    void updateProfile(Long userId, UserUpdateRequest request);

    /** 获取用户详情 */
    User getProfile(Long userId);
}
