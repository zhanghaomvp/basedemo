package com.cetcxl.usercenter.server.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.usercenter.server.common.controller.BaseController;
import com.cetcxl.usercenter.server.common.entity.ResBody;
import com.cetcxl.usercenter.server.constants.ResultCode;
import com.cetcxl.usercenter.server.entity.model.User;
import com.cetcxl.usercenter.server.entity.vo.UserVO;
import com.cetcxl.usercenter.server.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

@RestController
@Api(tags = "用户管理相关接口")
public class UserController extends BaseController {
    @Autowired
    private Mapper dozerMapper;

    @Autowired
    private UserService userService;

    @Data
    @ApiModel("用户注册请求体")
    public static class UserRegisterReq {
        @ApiModelProperty(value = "手机号", required = true)
        @Pattern(regexp = "^(1[3-9])\\d{9}$")
        String mobile;

        @ApiModelProperty(value = "密码", required = true)
        @NotNull
        String password;
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public ResBody<UserVO> register(@RequestBody @Validated UserRegisterReq req) {

        User one = userService.getOne(
                Wrappers.lambdaQuery(User.class)
                        .eq(User::getMobile, req.getMobile())
                        .ne(User::getStatus, User.UserStatusEnum.DISABLE)
        );



        if (Objects.nonNull(one)) {
            return ResBody.success(dozerMapper.map(one, UserVO.class));
        }

        User user = User.builder()
                .mobile(req.getMobile())
                .password(req.getPassword())
                .status(User.UserStatusEnum.ACTIVE)
                .build();
        userService.save(user);

        return ResBody.success(dozerMapper.map(user, UserVO.class));
    }

}
