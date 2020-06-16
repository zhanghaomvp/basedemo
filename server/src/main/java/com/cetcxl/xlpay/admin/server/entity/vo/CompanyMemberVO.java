package com.cetcxl.xlpay.admin.server.entity.vo;

import com.cetcxl.xlpay.admin.server.entity.model.CompanyMember;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class CompanyMemberVO {
    private Integer company;
    private Integer wallet;
    private String icNo;
    private String Name;
    private String phone;
    private String department;
    private String employeeNo;

    private CompanyMember.CompanyMemberStatusEnum status;
}
