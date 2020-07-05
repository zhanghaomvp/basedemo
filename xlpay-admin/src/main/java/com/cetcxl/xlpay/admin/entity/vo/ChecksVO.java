package com.cetcxl.xlpay.admin.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cetcxl.xlpay.common.entity.model.Checks;
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
 * @since 2020-07-05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Checks对象", description = "")
public class ChecksVO extends BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "batch", type = IdType.AUTO)
    private Integer batch;

    private Integer company;

    private Integer store;

    private Deal.PayType payType;

    private Integer totalDealCount;

    private BigDecimal totalDealAmonut;

    private String attachments;

    private Checks.Status status;

    private LocalDateTime created;

    private LocalDateTime updated;

}
