package com.cetcxl.xlpay.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.common.entity.model.Store;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
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
        @ApiModelProperty(value = "CompanyStoreRelation关联Id")
        private Integer csrId;
    }

    static String listCompanyStoresWithRelationSql(String name) {
        return new SQL() {{

            SELECT("s.*,csr.relation,csr.apply_releation,csr.`status` as relation_status ,csr.id as csr_id");
            FROM(" store s, company_store_relation csr");
            WHERE("s.id=csr.store AND csr.company=#{companyId}");
            if (StringUtils.isNotBlank(name)) {
                WHERE(" s.name like concat('%',#{name},'%')");
            }
        }}.toString();
    }

    @SelectProvider(type = StoreMapper.class, method = "listCompanyStoresWithRelationSql")
    IPage<CompanyStoreDTO> listCompanyStoresWithRelation(Page page, Integer companyId, String name);


    static String listCompanyStoresNotWithRelationSql(String name) {
        return new SQL() {{

            SELECT("s.*");
            FROM(" store s");
            WHERE("NOT EXISTS ( SELECT 1 FROM company_store_relation csr WHERE csr.company = #{companyId} AND s.id = csr.store )");
            if (StringUtils.isNotBlank(name)) {
                WHERE(" s.name like concat('%',#{name},'%')");
            }
            ORDER_BY(" s.id desc");
        }}.toString();
    }

    @SelectProvider(type = StoreMapper.class, method = "listCompanyStoresNotWithRelationSql")
    IPage<CompanyStoreDTO> listCompanyStoresNotWithRelation(Page page, Integer companyId, String name);

    @Data
    @ApiModel
    class StoreCompanyDTO {
        private String id;
        private String name;
        private String contact;
        private String phone;
        @ApiModelProperty(value = "已生效关联关系，该值用位表示关联关系 第一位为1 表示余额消费授信 第二位为1 表示信用消费授信")
        private Integer relation;
        @ApiModelProperty(value = "待确认关联关系，该值用位表示关联关系 第一位为1 表示企业正发起余额消费授信 第二位为1 表示企业正发起信用消费授信")
        private Integer applyReleation;
        @ApiModelProperty(value = "当前企业与商家关联关系状态")
        private CompanyStoreRelation.RelationStatus relationStatus;
        @ApiModelProperty(value = "CompanyStoreRelation关联Id")
        private Integer csrId;
    }

    static String listStoreCompanyIsApprovalSql(String companyName) {
        return new SQL() {{

            SELECT("c.*,csr.relation,csr.apply_releation,csr.`status` as relation_status,csr.`id` as csr_id ");
            FROM(" company c, company_store_relation csr");
            WHERE("c.id=csr.company AND csr.store=#{storeId}");
            if (StringUtils.isNotBlank(companyName)) {
                WHERE(" c.name like concat('%',#{companyName},'%')");
            }
            WHERE("csr.`status` = 0");

        }}.toString();
    }

    @SelectProvider(type = StoreMapper.class, method = "listStoreCompanyIsApprovalSql")
    IPage<StoreCompanyDTO> listStoreCompanyIsApproval(Page page, Integer storeId, String companyName);

    static String listStoreCompanyNotApprovalSql(String companyName) {
        return new SQL() {{

            SELECT("c.*,csr.relation,csr.apply_releation,csr.`status` as relation_status,csr.`id` as csr_id ");
            FROM(" company c, company_store_relation csr");
            WHERE("c.id=csr.company AND csr.store=#{storeId}");
            if (StringUtils.isNotBlank(companyName)) {
                WHERE(" c.name like concat('%',#{companyName},'%')");
            }
            WHERE("csr.relation > 0");
        }}.toString();
    }

    @SelectProvider(type = StoreMapper.class, method = "listStoreCompanyNotApprovalSql")
    IPage<StoreCompanyDTO> listStoreCompanyNotApproval(Page page, Integer storeId, String companyName);
}
