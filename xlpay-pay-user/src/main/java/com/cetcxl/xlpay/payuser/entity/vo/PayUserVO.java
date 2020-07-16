package com.cetcxl.xlpay.payuser.entity.vo;

import com.cetcxl.xlpay.common.entity.vo.BaseVO;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author ${author}
 * @since 2020-06-28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "PayUser对象", description = "")
public class PayUserVO extends BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String icNo;

    private String phone;

    private Integer functions;

    private Integer identityFlag;

    private PayUser.PayUserStatus status;

    private Boolean cookieAlive;

}
