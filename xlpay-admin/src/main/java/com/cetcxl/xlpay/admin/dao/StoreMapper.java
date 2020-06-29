package com.cetcxl.xlpay.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.common.entity.model.Store;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModel
    class CompanyStoreDTO {
        private String id;
        private String name;
        private String contact;
        private String phone;
        private String address;
        @ApiModelProperty(value = "已生效关联关系，该值用位表示关联关系 第一位为1 表示余额消费授信 第二位为1 表示信用消费授信")
        private Integer relation;
        @ApiModelProperty(value = "待确认关联关系，该值用位表示关联关系 第一位为1 表示企业正发起余额消费授信 第二位为1 表示企业正发起信用消费授信")
        private Integer applyReleation;
        @ApiModelProperty(value = "当前企业与商家关联关系状态")
        private CompanyStoreRelation.RelationStatus relationStatus;
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
