package com.cetcxl.xlpay.admin.server.entity.vo;

import com.cetcxl.xlpay.admin.server.entity.model.Company;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel
public class CompanyVO {
    private String Name;
    private String loginName;
    private String phone;
    private String email;
    private String socialCreditCode;


    private Company.CompanyStatusEnum status;
    private LocalDateTime created;
}
