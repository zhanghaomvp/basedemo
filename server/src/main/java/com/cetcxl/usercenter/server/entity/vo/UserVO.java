package com.cetcxl.usercenter.server.entity.vo;

import com.cetcxl.usercenter.server.entity.model.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel
public class UserVO {
    @ApiModelProperty(value = "昵称")
    private String nickName;
    private String mobile;
    private User.UserStatusEnum status;
    private LocalDateTime created;
}
