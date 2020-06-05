package com.cetcxl.usercenter.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.usercenter.server.dao.UserMapper;
import com.cetcxl.usercenter.server.entity.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

}
