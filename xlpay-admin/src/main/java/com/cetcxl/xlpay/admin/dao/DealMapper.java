package com.cetcxl.xlpay.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.controller.DealsController;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.cetcxl.xlpay.common.constants.PatternConstants.DATE_TIME;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
public interface DealMapper extends BaseMapper<Deal> {
    @Data
    @ApiModel
    class DealDTO {
        private Integer id;
        private String name;
        private String department;
        private BigDecimal amount;
        private String companyName;
        private String storeName;
        private Deal.PayType payType;
        @JsonFormat(pattern = DATE_TIME)
        private LocalDateTime created;
        private Deal.Status status;
        private Integer storeId;
    }

    static String listDealSql(DealsController.ListDealReq req) {
        return new SQL() {{
            SELECT("d.*,\n" +
                    "	cm.`name`,\n" +
                    "	cm.department,\n" +
                    "	c.`name`  as company_name,\n" +
                    "	s.`name`  as store_name,\n" +
                    "   s.`id`    as store_id");
            FROM(" deal d ");
            INNER_JOIN("company_member cm ON d.company_member = cm.id", "store s ON d.store = s.id ", "company c ON d.company = c.id ");
            WHERE("d.type>3");
            if (Objects.nonNull(req.getCompanyId())) {
                WHERE("d.company=#{req.companyId}");
            }

            if (StringUtils.isNotBlank(req.getCompanyName())) {
                WHERE("c.`name` like concat('%',#{req.companyName},'%')");
            }

            if (StringUtils.isNotBlank(req.getStoreName())) {
                WHERE("s.`name` like concat('%',#{req.storeName},'%')");
            }
            if (Objects.nonNull(req.getStoreId())) {
                WHERE("d.store=#{req.storeId}");
            }
            if (Objects.nonNull(req.getPayType())) {
                WHERE("d.pay_type=#{req.payType}");
            }
            if (Objects.nonNull(req.getStatus())) {
                WHERE("d.status=#{req.status}");
            }
            if (StringUtils.isNotBlank(req.getName())) {
                WHERE("cm.name like concat('%',#{req.name},'%')");
            }
            if (StringUtils.isNotBlank(req.getDepartment())) {
                WHERE("cm.department like concat('%',#{req.department},'%')");
            }
            if (Objects.nonNull(req.getBegin())) {
                WHERE("d.created >= #{req.begin}");
            }
            if (Objects.nonNull(req.getEnd())) {
                WHERE("d.created <= #{req.end}");
            }
        }}.toString();
    }

    @SelectProvider(type = DealMapper.class, method = "listDealSql")
    IPage<DealDTO> listDeal(Page page, DealsController.ListDealReq req);

    @SelectProvider(type = DealMapper.class, method = "listDealSql")
    List<DealDTO> listDealExport(@Param("req") DealsController.ListDealReq req);


    @Data
    @ApiModel
    class DashboardDTO {
        private BigDecimal totalAmount;
        private BigDecimal cashAmount;
        private BigDecimal creditAmount;
        private BigDecimal cashCheckAmount;
        private BigDecimal creditCheckAmount;
        private BigDecimal totalCheckAmount;
        private BigDecimal creditUncheckAmount;
        private BigDecimal cashUncheckAmount;
        private BigDecimal totalUncheckAmount;
    }

    @Select("SELECT\n" +
            "	SUM( d.amount ) AS total_amount,\n" +
            "	SUM( CASE WHEN d.pay_type = 0 THEN d.amount ELSE 0 END ) AS cash_amount,\n" +
            "	SUM( CASE WHEN d.pay_type = 1 THEN d.amount ELSE 0 END ) AS credit_amount,\n" +
            "	SUM( CASE WHEN ( d.pay_type = 0 AND d.`status` = 2 ) THEN d.amount ELSE 0 END ) AS cash_check_amount,\n" +
            "	SUM( CASE WHEN ( d.pay_type = 1 AND d.`status` = 2 ) THEN d.amount ELSE 0 END ) AS credit_check_amount \n" +
            "FROM\n" +
            "	deal d \n" +
            "WHERE\n" +
            "	d.company = #{req.companyId} \n" +
            "	AND d.type IN ( 4, 5 ) \n" +
            "	AND d.created >= #{req.begin} \n" +
            "	AND d.created <= #{req.end}")
    DashboardDTO companyDashboardWithOutDepartment(@Param("req") DealsController.DashboardReq req);

    @Select("SELECT\n" +
            "	SUM( d.amount ) AS total_amount,\n" +
            "	SUM( CASE WHEN d.pay_type = 0 THEN d.amount ELSE 0 END ) AS cash_amount,\n" +
            "	SUM( CASE WHEN d.pay_type = 1 THEN d.amount ELSE 0 END ) AS credit_amount,\n" +
            "	SUM( CASE WHEN ( d.pay_type = 0 AND d.`status` = 2 ) THEN d.amount ELSE 0 END ) AS cash_check_amount,\n" +
            "	SUM( CASE WHEN ( d.pay_type = 1 AND d.`status` = 2 ) THEN d.amount ELSE 0 END ) AS credit_check_amount \n" +
            "FROM\n" +
            "	deal d \n" +
            "   INNER JOIN company_member cm ON d.company_member = cm.id \n" +
            "WHERE\n" +
            "	d.company = #{req.companyId} \n" +
            "	AND d.type IN ( 4, 5 ) \n" +
            "	AND d.created >= #{req.begin} \n" +
            "	AND d.created <= #{req.end} \n" +
            "   AND cm.department = #{req.department}")
    DashboardDTO companyDashboardWithDepartment(@Param("req") DealsController.DashboardReq req);


    @Select(
            "SELECT\n" +
            "	SUM( d.amount ) AS total_amount,\n" +
            "	SUM( CASE WHEN d.pay_type = 0 THEN d.amount ELSE 0 END ) AS cash_amount,\n" +
            "	SUM( CASE WHEN d.pay_type = 1 THEN d.amount ELSE 0 END ) AS credit_amount,\n" +
            "	SUM( CASE WHEN ( d.pay_type = 0 AND d.`status` = 2 ) THEN d.amount ELSE 0 END ) AS cash_check_amount,\n" +
            "	SUM( CASE WHEN ( d.pay_type = 1 AND d.`status` = 2 ) THEN d.amount ELSE 0 END ) AS credit_check_amount \n" +
            "FROM\n" +
            "	deal d \n" +
            "WHERE\n" +
            "	d.store = #{req.storeId} \n" +
            "	AND d.type IN ( 4, 5 ) \n" +
            "	AND d.created >= #{req.begin} \n" +
            "	AND d.created <= #{req.end}"
    )
    DashboardDTO storeDashboardWithOutCompany(@Param("req") DealsController.DashboardReq req);

    @Select(
            "SELECT\n" +
            "	SUM( d.amount ) AS total_amount,\n" +
            "	SUM( CASE WHEN d.pay_type = 0 THEN d.amount ELSE 0 END ) AS cash_amount,\n" +
            "	SUM( CASE WHEN d.pay_type = 1 THEN d.amount ELSE 0 END ) AS credit_amount,\n" +
            "	SUM( CASE WHEN ( d.pay_type = 0 AND d.`status` = 2 ) THEN d.amount ELSE 0 END ) AS cash_check_amount,\n" +
            "	SUM( CASE WHEN ( d.pay_type = 1 AND d.`status` = 2 ) THEN d.amount ELSE 0 END ) AS credit_check_amount \n" +
            "FROM\n" +
            "	deal d \n" +
            "   INNER JOIN company c ON d.company = c.id \n" +
            "WHERE\n" +
            "	d.store = #{req.storeId} \n" +
            "	AND d.type IN ( 4, 5 ) \n" +
            "	AND d.created >= #{req.begin} \n" +
            "	AND d.created <= #{req.end} \n" +
            "   AND c.name  = #{req.companyName}"
    )
    DashboardDTO storeDashboardWithCompany(@Param("req") DealsController.DashboardReq req);
}
