package com.cetcxl.xlpay.common.entity.model;

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
@ApiModel(value = "Deal对象", description = "")
public class Deal implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer company;

    private Integer companyMember;

    private String icNo;

    private Integer store;

    private BigDecimal amount;

    private DealType type;

    private PayType payType;

    private String info;

    private Integer checkBatch;

    private Status status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum DealType implements IEnum<Integer> {
        ADMIN_RECHARGE(1),
        ADMIN_REDUCE(2),
        ADMIN_QUOTA(3),
        CASH_DEAL(4),
        CREDIT_DEAL(5),
        ;
        private Integer status;

        DealType(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }

    public enum PayType implements IEnum<Integer> {
        CASH(0, "余额消费"),
        CREDIT(1, "信用消费"),
        ;
        private Integer status;
        private String desc;

        PayType(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }

        public String getDesc() {
            return this.desc;
        }
    }

    public enum Status implements IEnum<Integer> {
        PAID(0, "待结算"),
        CHECKING(1, "结算中"),
        CHECK_FINISH(2, "已结算"),
        ;
        private Integer status;
        private String desc;

        Status(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }

        public String getDesc() {
            return this.desc;
        }
    }
}
