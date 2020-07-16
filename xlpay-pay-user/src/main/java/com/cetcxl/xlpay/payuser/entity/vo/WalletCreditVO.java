package com.cetcxl.xlpay.payuser.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
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
@ApiModel(value = "WalletCredit对象", description = "")
public class WalletCreditVO extends BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private BigDecimal creditBalance;

    private BigDecimal creditQuota;

    private WalletCredit.WalletCreditStaus status;

}
