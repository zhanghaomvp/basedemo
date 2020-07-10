package com.cetcxl.xlpay.payuser.entity.vo;

import com.cetcxl.xlpay.common.entity.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "WalletCash对象", description = "")
public class WalletCashVO extends BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private BigDecimal cashBalance;
}
