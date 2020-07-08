package com.cetcxl.xlpay.admin.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.dao.ChecksMapper;
import com.cetcxl.xlpay.common.constants.CommonResultCode;
import com.cetcxl.xlpay.common.entity.model.Checks;
import com.cetcxl.xlpay.common.entity.model.ChecksRecord;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.cetcxl.xlpay.common.entity.model.Checks.Status.APPLY;

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

    @Transactional
    public void process(Integer operator, int checkBatch, Checks.Status status, String info) {
        Checks checks = getById(checkBatch);
        checks.setStatus(status);
        checks.appendInfo(info);
        updateById(checks);

        checksRecordService.save(
                ChecksRecord.builder()
                        .checkBatch(checks.getBatch())
                        .action(status)
                        .operator(operator)
                        .build()
        );

        switch (status) {
            case APPROVAL:
                break;

            case REJECT:
                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.PAID)
                        .set(Deal::getCheckBatch, null)
                        .eq(Deal::getCheckBatch, checkBatch)
                        .update();
                break;

            case DENY:
                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.PAID)
                        .set(Deal::getCheckBatch, null)
                        .eq(Deal::getCheckBatch, checkBatch)
                        .update();
                break;

            case COFIRM:
                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.CHECK_FINISH)
                        .eq(Deal::getCheckBatch, checkBatch)
                        .update();

                if (checks.getPayType() == Deal.PayType.CREDIT) {
                    List<Deal> deals = dealService
                            .lambdaQuery()
                            .eq(Deal::getCheckBatch, checkBatch)
                            .list();

                    deals.forEach(deal -> walletCreditService.payment(deal));
                }

                break;

            default:

        }
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

}
