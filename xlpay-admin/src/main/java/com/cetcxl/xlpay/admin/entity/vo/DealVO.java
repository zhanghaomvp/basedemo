package com.cetcxl.xlpay.admin.entity.vo;

import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.*;

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
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Deal对象", description = "")
public class DealVO extends BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer company;

    private String companyName;

    private Integer companyMember;

    private Integer store;

    private String storeName;

    private BigDecimal amount;

    private Deal.DealType type;

    private Deal.PayType payType;

    private String info;

    private Integer checkBatch;

    private Deal.Status status;

    private LocalDateTime created;

    private LocalDateTime updated;
}
