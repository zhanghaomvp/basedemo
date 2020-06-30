package com.cetcxl.xlpay.admin.service;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.dao.DealMapper;
import com.cetcxl.xlpay.admin.exception.DealCashImportException;
import com.cetcxl.xlpay.admin.util.ContextUtil;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCash;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.cetcxl.xlpay.common.entity.model.Deal.PayType.CASH;
import static com.cetcxl.xlpay.common.entity.model.Deal.PayType.CREDIT;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Service
public class DealService extends ServiceImpl<DealMapper, Deal> {
    @Autowired
    CompanyMemberService companyMemberService;
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @Data
    @Builder
    public static class DealForAdminParam {
        Integer walletId;
        Integer company;
        Integer companyMember;
        Deal.DealType dealType;
        BigDecimal amount;
        BigDecimal quota;
    }

    /*
     **********************余额交易 start**********************
     */

    @Transactional
    public void batchDealCashForAdmin(List<DealForAdminParam> params) {
        params.forEach(
                param -> dealCashForAdmin(param)
        );
    }

    @Transactional
    public void dealCashForAdmin(DealForAdminParam param) {
        //todo 分布式锁待添加
        Deal deal = Deal.builder()
                .company(param.getCompany())
                .companyMember(param.getCompanyMember())
                .amount(param.getAmount())
                .type(param.getDealType())
                .payType(CASH)
                .build();
        save(deal);

        walletCashService.process(deal, param.getWalletId());
    }

    @Data
    @Builder
    @HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
    @HeadFontStyle(fontHeightInPoints = 15)
    public static class DealCashImportRow {
        @ExcelProperty("姓名")
        String name;
        @ExcelProperty("身份证号")
        @ColumnWidth(50)
        String icNo;
        @ExcelProperty("变更类型(充值/扣减)")
        @ColumnWidth(30)
        String type;
        @ExcelProperty("金额")
        String amount;

        public static List<DealCashImportRow> data() {
            return Lists
                    .newArrayList(
                            DealCashImportRow
                                    .builder()
                                    .name("张三")
                                    .icNo("511528209909010018")
                                    .type("充值")
                                    .amount("100")
                                    .build(),
                            DealCashImportRow
                                    .builder()
                                    .name("李四")
                                    .icNo("511528209909010020")
                                    .type("扣减")
                                    .amount("50")
                                    .build()
                    );
        }
    }

    @Slf4j
    @Getter
    public static class DealCashImportListener extends AnalysisEventListener<DealCashImportRow> {
        private int total = 0;
        private List<DealCashImportRow> failRows = Lists.newArrayList();

        private DealService dealService;

        public DealCashImportListener(DealService dealService) {
            this.dealService = dealService;
        }

        @Override
        public void invoke(DealCashImportRow dealCashImportBean, AnalysisContext analysisContext) {
            try {
                dealService.dealCashImport(dealCashImportBean);
            } catch (DealCashImportException e) {
                log.error(" DealCashImportListener invoke error -> {}", analysisContext.readRowHolder().getRowIndex());
                failRows.add(dealCashImportBean);
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            total = analysisContext.readRowHolder().getRowIndex();
        }
    }

    @Transactional
    public void dealCashImport(DealCashImportRow row) throws DealCashImportException {
        //todo 分布式锁待添加
        CompanyMember companyMember = companyMemberService.getOne(Wrappers.lambdaQuery(CompanyMember.class)
                .eq(CompanyMember::getIcNo, row.getIcNo()));
        if (Objects.isNull(companyMember)) {
            throw new DealCashImportException();
        }

        Deal.DealType dealType = null;
        if ("充值".equals(row.getType())) {
            dealType = Deal.DealType.ADMIN_RECHARGE;
        }
        if ("扣减".equals(row.getType())) {
            dealType = Deal.DealType.ADMIN_REDUCE;
        }

        if (Objects.isNull(dealType)) {
            throw new DealCashImportException();
        }

        WalletCash walletCash = walletCashService.getOne(
                Wrappers.lambdaQuery(WalletCash.class)
                        .eq(WalletCash::getCompanyMember, companyMember.getId())
        );
        if (Objects.isNull(walletCash)) {
            throw new DealCashImportException();
        }

        try {
            Deal deal = Deal.builder()
                    .company(ContextUtil.getUserInfo().getCompany().getId())
                    .companyMember(companyMember.getId())
                    .amount(new BigDecimal(row.getAmount()))
                    .type(dealType)
                    .payType(CASH)
                    .build();
            save(deal);

            walletCashService.process(deal, walletCash.getId());
        } catch (Exception e) {
            throw new DealCashImportException(e);
        }
    }

    /*
     **********************余额交易 end**********************
     */

    /*
     **********************信用交易 start**********************
     */

    @Transactional
    public void batchDealCreditForAdmin(List<DealForAdminParam> params) {
        params.forEach(
                param -> dealCreditForAdmin(param)
        );
    }

    @Transactional
    public void dealCreditForAdmin(DealForAdminParam param) {
        //todo 分布式锁待添加
        Deal deal = Deal.builder()
                .company(param.getCompany())
                .companyMember(param.getCompanyMember())
                .amount(param.getAmount())
                .type(param.getDealType())
                .payType(CREDIT)
                .build();
        save(deal);

        walletCreditService.process(deal, param.getWalletId());
    }

    /*
     **********************信用交易 end**********************
     */
}
