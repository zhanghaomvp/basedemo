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
 * @since 2020-07-05
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Checks对象", description = "")
public class Checks implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "batch", type = IdType.AUTO)
    private Integer batch;

    private Integer company;

    private Integer store;

    private Deal.PayType payType;

    private Integer totalDealCount;

    private BigDecimal totalDealAmonut;

    private String attachments;

    private String info;

    private Status status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum Status implements IEnum<Integer> {
        APPROVAL(0),
        REJECT(1),
        CONFIRM(2),
        DENY(3),
        FINISH(4),
        ;
        private Integer status;

        Status(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }

}
