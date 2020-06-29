package com.cetcxl.xlpay.payuser.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.common.entity.model.Store;
import lombok.Data;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
public interface StoreMapper extends BaseMapper<Store> {
    @Data
    class CompanyStoreDTO {
        private String id;
        private String name;
        private String contact;
        private String phone;
        private String address;
        private Integer relation;
        private Integer applyReleation;
        private Integer relationStatus;
    }

    @Select("SELECT\n" +
            "	s.*,\n" +
            "	csr.relation,\n" +
            "	csr.apply_releation,\n" +
            "	csr.`status` as relation_status \n" +
            "FROM\n" +
            "	store s,\n" +
            "	company_store_relation csr \n" +
            "WHERE\n" +
            "   s.id = csr.store \n" +
            "	AND csr.company = #{companyId}")
    IPage<CompanyStoreDTO> listCompanyStoresWithRelation(Page page, Integer companyId);


    static String listCompanyStoresNotWithRelationSql() {
        return new SQL() {{
            SELECT("s.*");
            FROM("store s");
            WHERE("NOT EXISTS ( SELECT 1 FROM company_store_relation csr WHERE csr.company = #{companyId} AND s.id = csr.store )");
            ORDER_BY("s.id desc");
        }}.toString();
    }

    @SelectProvider(type = StoreMapper.class, method = "listCompanyStoresNotWithRelationSql")
    IPage<CompanyStoreDTO> listCompanyStoresNotWithRelation(Page page, Integer companyId);


}
