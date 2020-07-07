package com.cetcxl.xlpay.admin.entity.vo;

import com.cetcxl.xlpay.common.entity.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class StoreVO extends BaseVO {
    private Integer id;
    private String name;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 授信企业数量
     */
    private Integer companyNum;

    /**
     * 统一社会信用代码
     */
    private String socialCreditCode;

    /**
     * 营业执照
     */
    private Integer businessLicense;
}
