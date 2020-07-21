package com.cetcxl.xlpay.admin.service;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.controller.ChecksController;
import com.cetcxl.xlpay.admin.dao.ChecksMapper;
import com.cetcxl.xlpay.admin.entity.model.Checks;
import com.cetcxl.xlpay.admin.entity.model.ChecksRecord;
import com.cetcxl.xlpay.common.chaincode.entity.CheckSlip;
import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import com.cetcxl.xlpay.common.component.RedisLockComponent;
import com.cetcxl.xlpay.common.constants.CommonResultCode;
import com.cetcxl.xlpay.common.constants.Constants;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.common.service.ChainCodeService;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cetcxl.xlpay.admin.entity.model.Checks.Status.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-07-05
 */
@Service
public class ChecksService extends ServiceImpl<ChecksMapper, Checks> {
    @Autowired
    ChecksRecordService checksRecordService;
    @Autowired
    DealService dealService;
    @Autowired
    WalletCreditService walletCreditService;

    @Autowired
    ChainCodeService chainCodeService;
    @Autowired
    CompanyService companyService;
    @Autowired
    StoreService storeService;

    @Transactional
    public void process(Integer operator, int checkBatch, Checks.Status status, String info) {
        Checks checks = getById(checkBatch);

        switch (status) {
            case APPROVAL:
                if (APPLY != checks.getStatus()) {
                    throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
                }
                break;

            case REJECT:
                if (CONFIRM == checks.getStatus()) {
                    throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
                }

                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.PAID)
                        .set(Deal::getCheckBatch, null)
                        .eq(Deal::getCheckBatch, checkBatch)
                        .update();
                break;

            case DENY:
                if (CONFIRM == checks.getStatus()) {
                    throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
                }

                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.PAID)
                        .set(Deal::getCheckBatch, null)
                        .eq(Deal::getCheckBatch, checkBatch)
                        .update();
                break;

            case CONFIRM:
                if (APPROVAL != checks.getStatus()) {
                    throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
                }

                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.CHECK_FINISH)
                        .eq(Deal::getCheckBatch, checkBatch)
                        .update();

                String companySocialCreditCode = companyService.getById(checks.getCompany()).getSocialCreditCode();
                String businessSocialCreditCode = storeService.getById(checks.getStore()).getSocialCreditCode();
                List<Deal> deals = dealService
                        .lambdaQuery()
                        .eq(Deal::getCheckBatch, checkBatch)
                        .list();

                if (checks.getPayType() == Deal.PayType.CREDIT) {
                    List<String> walletIdLists = deals.stream()
                            .map(
                                    deal ->
                                            Constants.KEY_CREDIT_DEAL +
                                                    walletCreditService.
                                                            lambdaQuery()
                                                            .eq(WalletCredit::getCompanyMember, deal.getCompanyMember())
                                                            .one()
                                                            .getId()
                            )
                            .distinct()
                            .collect(Collectors.toList());

                    try (RedisLockComponent.RedisLock redisLock =
                                 new RedisLockComponent.RedisLock(walletIdLists)) {
                        deals.forEach(deal -> walletCreditService.payment(deal));

                        //XXX 涉及钱包调整 此处上链需要在分布式锁范围中
                        chainCodeService.saveCheckSlip(
                                CheckSlip.builder()
                                        .checkNo(checks.getBatch().toString())
                                        .companySocialCreditCode(companySocialCreditCode)
                                        .businessSocialCreditCode(businessSocialCreditCode)
                                        .totalDeal(checks.getTotalDealCount().toString())
                                        .totalAmount(checks.getTotalDealAmonut().toString())
                                        .checkType(PayType.CREDIT)
                                        .tradeNos(
                                                deals.stream()
                                                        .map(deal -> deal.getId().toString())
                                                        .collect(Collectors.toList())
                                        )
                                        .build()
                        );
                    }

                } else {

                    chainCodeService.saveCheckSlip(
                            CheckSlip.builder()
                                    .checkNo(checks.getBatch().toString())
                                    .companySocialCreditCode(companySocialCreditCode)
                                    .businessSocialCreditCode(businessSocialCreditCode)
                                    .totalDeal(checks.getTotalDealCount().toString())
                                    .totalAmount(checks.getTotalDealAmonut().toString())
                                    .checkType(PayType.CASH)
                                    .tradeNos(
                                            deals.stream()
                                                    .map(deal -> deal.getId().toString())
                                                    .collect(Collectors.toList())
                                    )
                                    .build()
                    );
                }

                break;

            default:
        }

        checks.setStatus(status);
        checks.appendInfo(info);
        updateById(checks);

        checksRecordService.save(
                ChecksRecord.builder()
                        .checkBatch(checkBatch)
                        .action(status)
                        .operator(operator)
                        .build()
        );
    }

    @Transactional
    public Checks addCheck(
            Integer operator,
            Integer companyId,
            Integer storeId,
            Deal.PayType payType,
            List<Integer> dealIds,
            List<String> attachments,
            String info
    ) {

        List<Deal> deals = dealService.lambdaQuery()
                .in(Deal::getId, dealIds)
                .list();

        if (deals.size() != dealIds.size()) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
        }

        Optional<Deal> otherStoreDeal = deals.stream()
                .filter(
                        deal -> !deal.getStore().equals(storeId) || deal.getPayType() != payType
                )
                .findAny();

        if (otherStoreDeal.isPresent()) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);

        }

        Checks checks = Checks.builder()
                .company(companyId)
                .store(storeId)
                .payType(payType)
                .status(APPLY)
                .totalDealCount(deals.size())
                .totalDealAmonut(new BigDecimal("0"))
                .attachments(
                        Joiner.on(",").skipNulls().join(attachments)
                )
                .build()
                .appendInfo(info);

        deals.stream()
                .forEach(
                        deal ->
                                checks.setTotalDealAmonut(
                                        checks.getTotalDealAmonut().add(deal.getAmount())
                                )

                );

        save(checks);

        checksRecordService.save(
                ChecksRecord.builder()
                        .checkBatch(checks.getBatch())
                        .action(APPLY)
                        .operator(operator)
                        .build()
        );

        dealService.lambdaUpdate()
                .in(Deal::getId, dealIds)
                .set(Deal::getStatus, Deal.Status.CHECKING)
                .set(Deal::getCheckBatch, checks.getBatch())
                .update();

        return checks;
    }

    public List<CheckExportRow> listCheckExport(ChecksController.ListCheckReq req) {

        List<ChecksMapper.CheckDTO> checkDtos = baseMapper.listCheckExport(req);


        return checkDtos
                .stream()
                .map(
                        dto ->
                                CheckExportRow.builder()
                                        .storeName(dto.getStoreName())
                                        .companyName(dto.getCompanyName())
                                        .amount(dto.getTotalDealAmonut().toString())
                                        .applyTime(dto.getApplyTime())
                                        .approvalTime(dto.getApprovalTime())
                                        .confirmTime(dto.getConfirmTime())
                                        .denyTime(dto.getDenyTime())
                                        .payType(dto.getPayType().getDesc())
                                        .checkStatus(dto.getStatus().getDesc())
                                        .build()
                )
                .collect(Collectors.toList());
    }


    @Data
    @Builder
    @HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
    @HeadFontStyle(fontHeightInPoints = 15)
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER)
    public static class CheckExportRow {
        @ExcelProperty("企业名称")
        @ColumnWidth(25)
        String companyName;

        @ExcelProperty("商家名称")
        @ColumnWidth(25)
        String storeName;

        @ExcelProperty("申请结算总金额")
        @ColumnWidth(25)
        String amount;

        @ExcelProperty("申请结算时间")
        @DateTimeFormat()
        @ColumnWidth(40)
        LocalDateTime applyTime;

        @ExcelProperty("审核时间")
        @ColumnWidth(40)
        @DateTimeFormat()
        LocalDateTime approvalTime;

        @ExcelProperty("商家确认时间")
        @ColumnWidth(40)
        @DateTimeFormat()
        LocalDateTime confirmTime;

        @ExcelProperty("驳回时间")
        @ColumnWidth(40)
        @DateTimeFormat()
        LocalDateTime denyTime;

        @ExcelProperty("当前结算状态")
        @ColumnWidth(25)
        String checkStatus;

        @ExcelProperty("结算交易类型")
        @ColumnWidth(25)
        String payType;


        public enum SheetFormat {
            待审核结算(ImmutableList.of(APPLY), ImmutableList.of("approvalTime", "confirmTime", "denyTime")),
            待确认结算(ImmutableList.of(APPROVAL), ImmutableList.of("confirmTime")),
            已结算(ImmutableList.of(CONFIRM), ImmutableList.of("denyTime")),
            撤销结算(ImmutableList.of(REJECT, DENY), ImmutableList.of("confirmTime")),
            ;
            private List<Checks.Status> statues;
            private List<String> excludeColumn;

            SheetFormat(List<Checks.Status> statues, List<String> excludeColumn) {
                this.statues = statues;
                this.excludeColumn = excludeColumn;
            }

            public List<Checks.Status> getStatues() {
                return statues;
            }

            public List<String> getExcludeColumn(boolean companyOrStore) {
                ArrayList<String> strings = Lists.newArrayList(excludeColumn);
                if (companyOrStore) {
                    strings.add("companyName");
                } else {
                    strings.add("storeName");
                }
                return strings;
            }

            public static SheetFormat of(Checks.Status status) {
                for (SheetFormat checkExportColumn : values()) {
                    if (checkExportColumn.getStatues().contains(status)) {
                        return checkExportColumn;
                    }
                }
                throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
            }
        }
    }

}
