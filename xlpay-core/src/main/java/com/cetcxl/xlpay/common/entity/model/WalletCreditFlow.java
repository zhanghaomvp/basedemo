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
@ApiModel(value = "WalletCreditFlow对象", description = "")
public class WalletCreditFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer walletCredit;

    private Integer deal;

    private CreditFlowType type;

    private BigDecimal amount;

    private BigDecimal balance;

    private BigDecimal quota;

    private String info;

    private LocalDateTime created;

    public enum CreditFlowType implements IEnum<Integer> {
        BALANCE_MINUS(1),
        QUOTA_PLUS(2),
        QUOTA_MINUS(3),
        ;
        private Integer status;

        CreditFlowType(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }

    public void caculateBanlanceAndQuota() {
        if (Objects.isNull(type)) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }

        switch (type) {
            case QUOTA_PLUS:
                setBalance(balance.add(this.getAmount()));
                setQuota(quota.add(this.amount));
                break;
            case QUOTA_MINUS:
                setBalance(balance.subtract(this.getAmount()));
                setQuota(quota.subtract(this.getAmount()));
                break;
            case BALANCE_MINUS:
                setBalance(balance.subtract(this.getAmount()));
                break;
            default:
        }
    }
}
