package com.cetcxl.xlpay.payuser.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.payuser.controller.DealsController;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        private BigDecimal amount;
        private Deal.PayType payType;
        private String storeName;
        @JsonFormat(pattern = DATE_TIME)
        private LocalDateTime created;
    }

    static String listDealSql(DealsController.ListDealReq req) {
        return new SQL() {{
            SELECT("d.*,\n" +
                    "	s.`name`  as store_name,\n" +
                    "   s.`id`    as store_id");
            FROM(" deal d ");
            INNER_JOIN(
                    " company c ON d.company = c.id ",
                    "store s ON d.store = s.id "
            );
            WHERE("d.type in(4,5)");
            WHERE("d.ic_no =  #{icNo}");
            WHERE("c.`social_credit_code` = #{req.socialCreditCode}");
            if (StringUtils.isNotBlank(req.getStoreName())) {
                WHERE("s.`name` like concat('%',#{req.storeName},'%')");
            }
            if (Objects.nonNull(req.getPayType())) {
                WHERE("d.pay_type=#{req.payType}");
            }
            if (Objects.nonNull(req.getBegin())) {
                WHERE("d.created >= #{req.begin}");
            }
            if (Objects.nonNull(req.getEnd())) {
                WHERE("d.created <= #{req.end}");
            }
            ORDER_BY("d.id desc");
        }}.toString();
    }

    @SelectProvider(type = DealMapper.class, method = "listDealSql")
    IPage<DealDTO> listDeal(Page page, DealsController.ListDealReq req, String icNo);


    @Data
    @ApiModel
    class DealAmountsDTO {
        private BigDecimal cashAmount;
        private BigDecimal creditAmount;
    }

    static String listDealAmountsSql(DealsController.ListDealReq req) {
        return new SQL() {{
            SELECT("  SUM( CASE WHEN d.pay_type = 0 THEN d.amount ELSE 0 END ) AS cash_amount,\n" +
                    "  SUM( CASE WHEN d.pay_type = 1 THEN d.amount ELSE 0 END ) AS credit_amount ");
            FROM(" deal d ");
            INNER_JOIN(
                    " company c ON d.company = c.id ",
                    "store s ON d.store = s.id "
            );
            WHERE("d.type in(4,5)");
            WHERE("d.ic_no =  #{icNo}");
            WHERE("c.`social_credit_code` = #{req.socialCreditCode}");
            if (StringUtils.isNotBlank(req.getStoreName())) {
                WHERE("s.`name` like concat('%',#{req.storeName},'%')");
            }
            if (Objects.nonNull(req.getBegin())) {
                WHERE("d.created >= #{req.begin}");
            }
            if (Objects.nonNull(req.getEnd())) {
                WHERE("d.created <= #{req.end}");
            }
        }}.toString();
    }

    @SelectProvider(type = DealMapper.class, method = "listDealAmountsSql")
    DealAmountsDTO listDealAmounts(DealsController.ListDealReq req, String icNo);
}
