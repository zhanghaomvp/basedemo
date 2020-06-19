package com.cetcxl.xlpay.admin.server.entity.vo;

import com.cetcxl.xlpay.admin.server.entity.model.Company;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel
public class CompanyVO extends BaseVO {
    private Integer id;
    private String Name;
    private String socialCreditCode;
    private String phone;
    private String email;
    private Company.CompanyStatus status;
    private LocalDateTime created;
}
