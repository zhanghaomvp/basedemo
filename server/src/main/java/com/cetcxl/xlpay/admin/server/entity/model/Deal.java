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
@ApiModel(value="Deal对象", description="")
public class Deal implements Serializable {

    private static final long serialVersionUID=1L;

    private String id;

    private Integer company;

    private Integer companyMember;

    private Integer store;

    private BigDecimal amount;

    private Integer type;

    private Integer payType;

    private String info;

    private Integer check;

    private Integer status;

    private LocalDateTime created;

    private LocalDateTime updated;


}
