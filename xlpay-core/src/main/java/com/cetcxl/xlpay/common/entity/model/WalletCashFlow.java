package com.cetcxl.xlpay.common.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.SYSTEM_LOGIC_ERROR;

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
@ApiModel(value = "WalletCashFlow对象", description = "")
public class WalletCashFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer walletCash;

    private Integer deal;

    private CashFlowType type;

    private BigDecimal amount;

    private BigDecimal balance;

    private String info;

    private LocalDateTime created;

    public enum CashFlowType implements IEnum<Integer> {
        PLUS(0),
        MINUS(1),
        ;
        private Integer status;

        CashFlowType(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }

    public void caculateBalance() {
        if (Objects.isNull(type)) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }

        switch (type) {
            case PLUS:
                setBalance(this.balance.add(this.getAmount()));
                break;
            case MINUS:
                setBalance(this.balance.subtract(this.getAmount()));
                break;
            default:
        }
    }

    @Data
    @Builder
    public static class CashFlowInfo {

    }
}
