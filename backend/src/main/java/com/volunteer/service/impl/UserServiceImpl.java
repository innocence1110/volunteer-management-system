package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.dto.UserUpdateRequest;
import com.volunteer.entity.User;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.UserService;
import com.volunteer.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findByAccount(String account) {
        return baseMapper.selectByAccount(account);
    }

    @Override
    public String login(String account, String password) {
        User user = findByAccount(account);
        if (user == null) {
            throw new RuntimeException("账号或密码不正确");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("账号或密码不正确");
        }
        return jwtUtil.generateToken(user.getId(), user.getAccount(), user.getRole());
    }

    @Override
    public void register(String name, String phone, String account, String password, String role) {
        // 检查账号是否已存在
        User existing = findByAccount(account);
        if (existing != null) {
            throw new RuntimeException("该账号已被注册");
        }

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setAccount(account);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPoints(0);
        save(user);
    }

    @Override
    public void updateProfile(Long userId, UserUpdateRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (request.getPhone() != null) {
            if (!request.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new RuntimeException("电话号码格式不正确");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getMajor() != null) {
            user.setMajor(request.getMajor());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getStudentId() != null) {
            user.setStudentId(request.getStudentId());
        }

        updateById(user);
    }

    @Override
    public User getProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return user;
    }
}
