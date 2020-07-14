package com.cetcxl.xlpay.admin.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.controller.ChecksController;
import com.cetcxl.xlpay.admin.entity.model.Checks;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Joiner;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cetcxl.xlpay.common.constants.PatternConstants.DATE_TIME;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-07-05
 */
public interface ChecksMapper extends BaseMapper<Checks> {
    @Data
    @NoArgsConstructor
    @ApiModel
    class CheckDTO {
        private Integer batch;
        private String companyName;
        private String storeName;
        private BigDecimal totalDealAmonut;

        private Deal.PayType payType;
        @JsonFormat(pattern = DATE_TIME)
        private LocalDateTime created;
        private Checks.Status status;

        private String applyPhone;
        @JsonFormat(pattern = DATE_TIME)
        private LocalDateTime applyTime;
        private String approvalPhone;
        @JsonFormat(pattern = DATE_TIME)
        private LocalDateTime approvalTime;
        private String confirmPhone;
        @JsonFormat(pattern = DATE_TIME)
        private LocalDateTime confirmTime;
        private String denyPhone;
        @JsonFormat(pattern = DATE_TIME)
        private LocalDateTime denyTime;
    }

    static String listCheckSql(ChecksController.ListCheckReq req) {
        return new SQL() {{
            SELECT("    c.*,\n" +
                    "	c1.`name` AS company_name,\n" +
                    "	s.`name` AS store_name,\n" +
                    "   cr1.operator AS apply_operator,\n" +
                    "	cu1.phone AS apply_phone,\n" +
                    "	cr1.created AS apply_time,\n" +
                    "	cr2.operator AS approval_operator,\n" +
                    "	cu2.phone AS approval_phone,\n" +
                    "	cr2.created AS approval_time,\n" +
                    "	cr3.operator AS confirm_operator,\n" +
                    "	cu3.phone AS confirm_phone,\n" +
                    "	cr3.created AS confirm_time,\n" +
                    "	cr4.operator AS deny_operator,\n" +
                    "	cu4.phone AS deny_phone,\n" +
                    "	cr4.created AS deny_time");
            FROM(" checks c ");
            INNER_JOIN(
                    "company c1 ON c.company = c1.id",
                    "store s ON c.store = s.id"
            );
            LEFT_OUTER_JOIN(
                    "  checks_record cr1 ON c.batch = cr1.check_batch AND cr1.action = 0",
                    " company_user cu1 ON cr1.operator = cu1.id ",
                    " checks_record cr2 ON c.batch = cr2.check_batch AND cr2.action = 2 ",
                    " company_user cu2 ON cr2.operator = cu2.id ",
                    " checks_record cr3 ON c.batch = cr3.check_batch AND cr3.action = 3 ",
                    " company_user cu3 ON cr3.operator = cu3.id ",
                    " checks_record cr4 ON c.batch = cr4.check_batch AND cr4.action = 4 ",
                    " company_user cu4 ON cr3.operator = cu4.id "
            );

            if (Objects.nonNull(req.getCompanyId())) {
                WHERE("c.company=#{req.companyId}");
            }
            if (Objects.nonNull(req.getStoreId())) {
                WHERE("c.store=#{req.storeId}");
            }
            if (Objects.nonNull(req.getPayType())) {
                WHERE("c.pay_type=#{req.payType}");
            }

            if (Objects.nonNull(req.getStatues())) {
                List<Integer> statues = Stream
                        .of(req.getStatues())
                        .map(status -> status.getValue())
                        .collect(Collectors.toList());

                WHERE("c.status" + " in ( " + Joiner.on(",").skipNulls().join(statues) + " ) ");
            }

            if (StringUtils.isNotBlank(req.getStoreName())) {
                WHERE("s.name like concat('%',#{req.storeName},'%')");
            }
            if (StringUtils.isNotBlank(req.getCompanyName())) {
                WHERE("c1.name like concat('%',#{req.companyName},'%')");
            }
            if (Objects.nonNull(req.getApprovalTimeBegin())) {
                WHERE("cr2.created >=#{req.approvalTimeBegin}");
            }
            if (Objects.nonNull(req.getApprovalTimeEnd())) {
                WHERE("cr2.created <=#{req.approvalTimeEnd}");
            }
            ORDER_BY(" batch asc ");
        }}.toString();
    }

    @SelectProvider(type = ChecksMapper.class, method = "listCheckSql")
    IPage<CheckDTO> listCheck(Page page, @Param("req") ChecksController.ListCheckReq req);

    @SelectProvider(type = ChecksMapper.class, method = "listCheckSql")
    List<CheckDTO> listCheckExport(@Param("req") ChecksController.ListCheckReq req);
}
