package com.cetcxl.xlpay.admin.server.entity.conveter;

import com.cetcxl.xlpay.admin.server.entity.model.Company;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyConverter {
    CompanyVO toCompanyVO(Company company);
}
