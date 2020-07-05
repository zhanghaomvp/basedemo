package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.admin.entity.vo.ChecksVO;
import com.cetcxl.xlpay.admin.service.ChecksService;
import com.cetcxl.xlpay.admin.service.DealService;
import com.cetcxl.xlpay.common.constants.CommonResultCode;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Checks;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.google.common.base.Joiner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Validated
@RestController
@Api(tags = "企业与商家结算相关相关接口")
public class ChecksController extends BaseController {
    @Autowired
    DealService dealService;
    @Autowired
    ChecksService checksService;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class AddCheckReq {
        @NotNull
        Integer storeId;
        @NotEmpty
        List<Integer> dealIds;
        @NotNull
        Deal.PayType payType;

        List<String> attachments;

        String info;
    }

    @PostMapping("/companys/{companyId}/deals/checks")
    @ApiOperation("企业新增结算单")
    @Transactional
    public ResBody<ChecksVO> addCheck(
            @PathVariable Integer companyId,
            @Validated @RequestBody AddCheckReq req
    ) {
        List<Deal> deals = dealService.list(
                Wrappers.lambdaQuery(Deal.class)
                        .in(Deal::getId, req.getDealIds())
        );

        if (deals.size() != req.getDealIds().size()) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
        }

        Optional<Deal> otherStoreDeal = deals.stream()
                .filter(
                        deal -> !deal.getStore().equals(req.getStoreId()) || deal.getPayType() != req.getPayType()
                )
                .findAny();

        if (otherStoreDeal.isPresent()) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);

        }

        Checks checks = Checks.builder()
                .company(companyId)
                .store(req.getStoreId())
                .payType(req.getPayType())
                .info(req.getInfo())
                .status(Checks.Status.APPROVAL)
                .totalDealCount(deals.size())
                .totalDealAmonut(new BigDecimal("0"))
                .attachments(
                        Joiner.on(",").skipNulls().join(req.getAttachments())
                )
                .build();

        deals.stream()
                .forEach(
                        deal ->
                                checks.setTotalDealAmonut(
                                        checks.getTotalDealAmonut().add(deal.getAmount())
                                )

                );

        checksService.save(checks);

        dealService.lambdaUpdate()
                .in(Deal::getId, req.getDealIds())
                .set(Deal::getStatus, Deal.Status.CHECKING)
                .set(Deal::getCheckBatch, checks.getBatch())
                .update();

        return ResBody.success(ChecksVO.of(checks, ChecksVO.class));
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class AuditCheckReq {
        Boolean approvalOrReject;
        Boolean confirmOrDeny;
        String info;
    }

    @PatchMapping("/companys/{companyId}/deals/checks/{checkBatch}")
    @ApiOperation("结算单企业审慎")
    public ResBody auditCheck(
            @PathVariable Integer checkBatch,
            @Validated @RequestBody AuditCheckReq req
    ) {

        if (Objects.isNull(req.getApprovalOrReject())) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
        }

        if (req.getApprovalOrReject()) {
            checksService.process(checkBatch, Checks.Status.CONFIRM);
        } else {
            checksService.process(checkBatch, Checks.Status.REJECT);
        }

        return ResBody.success();
    }

    @PatchMapping("/stores/{storeId}/deals/checks/{checkBatch}")
    @ApiOperation("结算单商家确认")
    public ResBody confirmCheck(
            @PathVariable Integer checkBatch,
            @Validated @RequestBody AuditCheckReq req
    ) {
        if (Objects.isNull(req.getConfirmOrDeny())) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
        }

        if (req.getConfirmOrDeny()) {
            checksService.process(checkBatch, Checks.Status.FINISH);
        } else {
            checksService.process(checkBatch, Checks.Status.DENY);
        }

        return ResBody.success();
    }

}
