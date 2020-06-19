package com.cetcxl.xlpay.admin.server.entity.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="WalletCashFlow对象", description="")
public class WalletCashFlow implements Serializable {

    private static final long serialVersionUID=1L;

    private String id;

    private Integer walletCash;

    private String deal;

    private BigDecimal amount;

    private BigDecimal balance;

    private LocalDateTime created;


}
