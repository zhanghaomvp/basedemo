package com.cetcxl.usercenter.server.entity.conveter;

import com.cetcxl.usercenter.server.entity.model.Company;
import com.cetcxl.usercenter.server.entity.vo.CompanyVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyConverter {
    CompanyVO toCompanyVO(Company company);
}
