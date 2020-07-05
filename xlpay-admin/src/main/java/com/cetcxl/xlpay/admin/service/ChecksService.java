package com.cetcxl.xlpay.admin.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.dao.ChecksMapper;
import com.cetcxl.xlpay.common.entity.model.Checks;
import com.cetcxl.xlpay.common.entity.model.Deal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    DealService dealService;
    @Autowired
    WalletCreditService walletCreditService;

    @Transactional
    public void process(int checkBatch, Checks.Status status) {
        switch (status) {
            case CONFIRM:
                lambdaUpdate()
                        .set(Checks::getStatus, Checks.Status.CONFIRM)
                        .eq(Checks::getBatch, checkBatch)
                        .update();
                break;

            case REJECT:
                lambdaUpdate()
                        .set(Checks::getStatus, Checks.Status.REJECT)
                        .eq(Checks::getBatch, checkBatch)
                        .update();

                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.PAID)
                        .set(Deal::getCheckBatch, null)
                        .update();
                break;

            case DENY:
                lambdaUpdate()
                        .set(Checks::getStatus, Checks.Status.DENY)
                        .eq(Checks::getBatch, checkBatch)
                        .update();

                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.PAID)
                        .set(Deal::getCheckBatch, null)
                        .update();
                break;

            case FINISH:
                Checks checks = getById(checkBatch);

                lambdaUpdate()
                        .set(Checks::getStatus, Checks.Status.FINISH)
                        .eq(Checks::getBatch, checkBatch)
                        .update();
                dealService.lambdaUpdate()
                        .set(Deal::getStatus, Deal.Status.CHECK_FINISH)
                        .update();

                if (checks.getPayType() == Deal.PayType.CREDIT) {
                    List<Deal> deals = dealService.lambdaQuery()
                            .eq(Deal::getCheckBatch, checkBatch)
                            .list();

                    deals.forEach(deal -> walletCreditService.payment(deal));
                }

                break;

            default:

        }
    }

}
