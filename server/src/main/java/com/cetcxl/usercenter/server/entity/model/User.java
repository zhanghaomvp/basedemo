package com.cetcxl.usercenter.server.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String nickName;
    private String mobile;
    private String password;
    private String icNo;
    private String realName;
    private boolean identifyFlag;
    private UserStatusEnum status;
    private LocalDateTime created;
    private LocalDateTime updated;


    public enum UserStatusEnum implements IEnum<Integer> {
        DISABLE(0),
        ACTIVE(1),
        ;
        private Integer status;

        UserStatusEnum(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }
}
