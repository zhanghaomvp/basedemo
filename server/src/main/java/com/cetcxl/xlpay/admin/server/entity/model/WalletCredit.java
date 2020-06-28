package com.cetcxl.xlpay.admin.server.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "WalletCredit对象", description = "")
public class WalletCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer companyMember;

    private BigDecimal creditBalance;

    private BigDecimal creditQuota;

    private WalletCreditStaus status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum WalletCreditStaus implements IEnum<Integer> {
        DISABLE(0, "冻结"),
        ENABLE(1, "正常"),
        ;
        private Integer status;
        private String desc;


        WalletCreditStaus(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }

        public String getDesc() {
            return desc;
        }
    }

}
