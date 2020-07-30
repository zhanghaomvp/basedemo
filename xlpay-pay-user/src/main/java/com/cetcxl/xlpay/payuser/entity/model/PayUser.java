package com.cetcxl.xlpay.payuser.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
import com.cetcxl.xlpay.common.entity.plugin.IBitEnum;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author ${author}
 * @since 2020-06-28
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "PayUser对象", description = "")
public class PayUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String icNo;

    private String phone;

    private String password;

    private Integer functions;

    private Integer identityFlag;

    private PayUserStatus status;

    private LocalDateTime created;

    private LocalDateTime updated;

    private LocalDateTime lockedDeadLine;


    public enum PayUserStatus implements IEnum<Integer> {
        DISABLE(0),
        ACTIVE(1),
        LOCKED(2),
        ;
        private Integer status;

        PayUserStatus(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }

    public enum PayUserFuntion implements IBitEnum {
        NO_PASSWORD_PAY(1),
        ;
        private Integer bitPos;

        PayUserFuntion(Integer bitPos) {
            this.bitPos = bitPos;
        }

        @Override
        public int getBitPos() {
            return this.bitPos;
        }
    }
}
