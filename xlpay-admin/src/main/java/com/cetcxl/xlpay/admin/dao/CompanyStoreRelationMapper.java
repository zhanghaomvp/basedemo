package com.cetcxl.xlpay.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import org.apache.ibatis.annotations.Select;

import java.sql.ClientInfoStatus;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
public interface CompanyStoreRelationMapper extends BaseMapper<CompanyStoreRelation> {

    @Select("SELECT DISTINCT\n" +
            "	c. NAME\n" +
            "FROM\n" +
            "	company_store_relation AS csr\n" +
            "LEFT JOIN company c ON c.id = csr.company\n" +
            "WHERE\n" +
            "	csr.store = #{storeId}")
    List<String> getAllCompanyNames(Integer storeId);
}
