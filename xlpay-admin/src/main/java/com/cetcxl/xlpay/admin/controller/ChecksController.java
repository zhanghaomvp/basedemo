package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.dao.ChecksMapper;
import com.cetcxl.xlpay.admin.dao.ChecksRecordMapper;
import com.cetcxl.xlpay.admin.entity.vo.ChecksVO;
import com.cetcxl.xlpay.admin.service.ChecksService;
import com.cetcxl.xlpay.admin.service.DealService;
import com.cetcxl.xlpay.admin.util.ContextUtil;
import com.cetcxl.xlpay.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Checks;
import com.cetcxl.xlpay.common.entity.model.Deal;
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

@Validated
@RestController
@Api(tags = "企业与商家结算相关相关接口")
public class ChecksController extends BaseController {
    @Autowired
    DealService dealService;
    @Autowired
    ChecksService checksService;
    @Autowired
    ChecksRecordMapper checksRecordMapper;
    @Autowired
    ChecksMapper checksMapper;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class ListCheckReq extends MybatisPlusConfig.PageReq {
        @NotNull(groups = ListCompanyCheckGroup.class)
        Integer companyId;
        @NotNull(groups = ListStoreCheckGroup.class)
        Integer storeId;

        String companyName;
        String storeName;

        Deal.PayType payType;
        Checks.Status[] statues;
    }

    interface ListCompanyCheckGroup {
    }

    @GetMapping("/companys/{companyId}/checks")
    @ApiOperation("企业结算列表查询")
    public ResBody<IPage<ChecksMapper.CheckDTO>> ListCompanyCheck(@Validated(ListCompanyCheckGroup.class) ListCheckReq req) {
        return ResBody
                .success(
                        checksMapper.listCheck(
                                new Page(req.getPageNo(), req.getPageSize()),
                                req
                        )
                );
    }

    interface ListStoreCheckGroup {
    }

    @GetMapping("/stores/{storeId}/checks")
    @ApiOperation("企业结算列表查询")
    public ResBody<IPage<ChecksMapper.CheckDTO>> ListStoreCheck(@Validated(ListStoreCheckGroup.class) ListCheckReq req) {
        return ResBody
                .success(
                        checksMapper.listCheck(
                                new Page(req.getPageNo(), req.getPageSize()),
                                req
                        )
                );
    }

    @GetMapping("/checks/{checkId}")
    @ApiOperation("结算单详情查询")
    public ResBody<ChecksVO> getCheckDetail(@PathVariable Integer checkId) {
        Checks checks = checksService.getById(checkId);
        ChecksVO checksVO = ChecksVO.of(checks, ChecksVO.class);

        checksVO.setChecksRecords(
                checksRecordMapper.listCheckRecordDTO(checks.getBatch())
        );
        return ResBody.success(checksVO.resolveInfos());
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
        @NotNull(groups = ApprovalCheckGroup.class)
        Boolean approvalOrReject;
        @NotNull(groups = ConfirmCheckGroup.class)
        Boolean confirmOrDeny;
        String info;
    }

    interface ApprovalCheckGroup {
    }

    @PatchMapping("/companys/{companyId}/deals/checks/{checkBatch}")
    @ApiOperation("结算单企业审慎")
    public ResBody approvalCheck(
            @PathVariable
                    Integer checkBatch,
            @Validated(ApprovalCheckGroup.class)
            @RequestBody
                    AuditCheckReq req
    ) {
        Integer operator = Integer.valueOf(ContextUtil.getUserInfo().getUsername());

        if (req.getApprovalOrReject()) {
            checksService.process(operator, checkBatch, Checks.Status.APPROVAL, req.getInfo());
        } else {
            checksService.process(operator, checkBatch, Checks.Status.REJECT, req.getInfo());
        }

        return ResBody.success();
    }

    interface ConfirmCheckGroup {
    }

    @PatchMapping("/stores/{storeId}/deals/checks/{checkBatch}")
    @ApiOperation("结算单商家确认")
    public ResBody confirmCheck(
            @PathVariable
                    Integer checkBatch,
            @Validated(ConfirmCheckGroup.class)
            @RequestBody
                    AuditCheckReq req
    ) {
        Integer operator = Integer.valueOf(ContextUtil.getUserInfo().getUsername());

        if (req.getConfirmOrDeny()) {
            checksService.process(operator, checkBatch, Checks.Status.COFIRM, req.getInfo());
        } else {
            checksService.process(operator, checkBatch, Checks.Status.DENY, req.getInfo());
        }

        return ResBody.success();
    }

}
