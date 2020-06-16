package com.cetcxl.xlpay.admin.server.entity.conveter;

import com.cetcxl.xlpay.admin.server.entity.model.CompanyMember;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyMemberVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMemberConverter {
    CompanyMemberVO toCompanyMemberVO(CompanyMember companyMember);
}
