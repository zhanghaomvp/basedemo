package com.cetcxl.xlpay.admin.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
public interface CompanyMemberMapper extends BaseMapper<CompanyMember> {

    @Select(
            "SELECT distinct  department FROM company_member WHERE company = #{companyId}"
    )
    List<String> getAllDepartment(Integer companyId);
}
