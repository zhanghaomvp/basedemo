package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.dao.ChecksMapper;
import com.cetcxl.xlpay.admin.entity.vo.ChecksVO;
import com.cetcxl.xlpay.admin.service.ChecksService;
import com.cetcxl.xlpay.admin.service.DealService;
import com.cetcxl.xlpay.admin.util.ContextUtil;
import com.cetcxl.xlpay.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.common.constants.CommonResultCode;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Checks;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.common.rpc.ResBody;
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
import java.util.List;
import java.util.Objects;

@Validated
@RestController
@Api(tags = "企业与商家结算相关相关接口")
public class ChecksController extends BaseController {
    @Autowired
    DealService dealService;
    @Autowired
    ChecksService checksService;
    @Autowired
    ChecksMapper checksMapper;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class ListCheckReq extends MybatisPlusConfig.PageReq {
        Integer companyId;
        Integer storeId;
        Deal.PayType payType;
        Checks.Status status;
    }

    @GetMapping("/companys/{companyId}/checks")
    @ApiOperation("企业结算列表查询")
    public ResBody<IPage<ChecksMapper.CheckDTO>> ListCompanyCheck(ListCheckReq req) {
        return ResBody
                .success(
                        checksMapper.listCheck(
                                new Page(req.getPageNo(), req.getPageSize()),
                                req
                        )
                );
    }

    @GetMapping("/stores/{storeId}/checks")
    @ApiOperation("企业结算列表查询")
    public ResBody<IPage<ChecksMapper.CheckDTO>> ListStoreCheck(ListCheckReq req) {
        return ResBody
                .success(
                        checksMapper.listCheck(
                                new Page(req.getPageNo(), req.getPageSize()),
                                req
                        )
                );
    }

    @GetMapping("/checks/{checkId}")
    @ApiOperation("企业账单详情查询")
    public ResBody getCheckDetail(@PathVariable Integer checkId) {
        ChecksVO checksVO = ChecksVO.of(checksService.getById(checkId), ChecksVO.class);
        return ResBody.success(checksVO);
    }

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

        return ResBody
                .success(
                        ChecksVO.of(
                                checksService.addCheck(
                                        Integer.valueOf(ContextUtil.getUserInfo().getUsername()),
                                        companyId,
                                        req.getStoreId(),
                                        req.getPayType(),
                                        req.getDealIds(),
                                        req.getAttachments(),
                                        req.getInfo()
                                ),
                                ChecksVO.class
                        )
                );
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

        Integer operator = Integer.valueOf(ContextUtil.getUserInfo().getUsername());

        if (req.getApprovalOrReject()) {
            checksService.process(operator, checkBatch, Checks.Status.APPROVAL, req.getInfo());
        } else {
            checksService.process(operator, checkBatch, Checks.Status.REJECT, req.getInfo());
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

        Integer operator = Integer.valueOf(ContextUtil.getUserInfo().getUsername());

        if (req.getConfirmOrDeny()) {
            checksService.process(operator, checkBatch, Checks.Status.COFIRM, req.getInfo());
        } else {
            checksService.process(operator, checkBatch, Checks.Status.DENY, req.getInfo());
        }

        return ResBody.success();
    }

}
