package com.cetcxl.xlpay.admin.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.constants.ResultCode;
import com.cetcxl.xlpay.admin.dao.WalletCashMapper;
import com.cetcxl.xlpay.admin.dao.WalletCreditMapper;
import com.cetcxl.xlpay.admin.service.CompanyMemberService;
import com.cetcxl.xlpay.admin.service.DealService;
import com.cetcxl.xlpay.admin.service.WalletCashService;
import com.cetcxl.xlpay.admin.service.WalletCreditService;
import com.cetcxl.xlpay.admin.util.ContextUtil;
import com.cetcxl.xlpay.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCash;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.SYSTEM_LOGIC_ERROR;

@Validated
@RestController
@Api(tags = "企业成员相关接口")
public class CompanyMemberController extends BaseController {
    @Autowired
    CompanyMemberService companyMemberService;

    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCashMapper walletCashMapper;

    @Autowired
    WalletCreditService walletCreditService;
    @Autowired
    WalletCreditMapper walletCreditMapper;

    @Autowired
    DealService dealService;

    @Data
    @ApiModel("")
    public static class ListWalletReq extends MybatisPlusConfig.PageReq {
        String name;
        String department;
    }

    /*
     *******************余额账户相关 start**********************
     */
    @GetMapping("/companys/{companyId}/members/wallet/cash")
    @ApiOperation("企业成员余额账户查询")
    public IPage<WalletCashMapper.WalletCashDTO> listWalletCash(@Validated ListWalletReq req) {
        return walletCashMapper.listWalletCash(
                new Page(req.getPageNo(), req.getPageSize()),
                ContextUtil.getUserInfo().getCompany().getId(),
                req.getDepartment(),
                req.getName());
    }

    @GetMapping("/companys/{companyId}/members/wallet/cash/export")
    @ApiOperation("企业成员余额账户明细导出")
    public void listWalletCashExport(@Validated ListWalletReq req, HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(
                response,
                "余额管理明细" + DateTimeFormatter.ISO_DATE.format(LocalDateTime.now())
        );

        EasyExcel
                .write(
                        response.getOutputStream(),
                        WalletCashService.WalletCashExportRow.class
                )
                .sheet("sheet")
                .doWrite(
                        walletCashService.listWalletCashExport(
                                ContextUtil.getUserInfo().getCompany().getId(),
                                req.getDepartment(),
                                req.getName()
                        )
                );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class UpdateWalletCashStatusReq {
        @NotNull WalletCash.WalletCashStaus status;
    }

    @PatchMapping(value = "/companys/{companyId}/members/{companyMemberId}/wallet/cash/{walletId}/status")
    @ApiOperation("企业成员余额账户状态修改")
    public void updateWalletCashStatus(@PathVariable Integer walletId, @Validated @RequestBody UpdateWalletCashStatusReq req) {
        WalletCash walletCash = walletCashService.getById(walletId);
        if (Objects.isNull(walletCash)) {
            throw new BaseRuntimeException(ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        walletCashService.update(
                Wrappers
                        .lambdaUpdate(WalletCash.class)
                        .set(WalletCash::getStatus, req.getStatus())
                        .eq(WalletCash::getId, walletCash.getId())
        );
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class UpdateWalletCashAmountReq {
        @NotNull
        Deal.DealType dealType;
        @NotBlank
        @ApiModelProperty(required = true, value = "充值金额 或者 扣减金额", example = "50.0")
        String amount;
    }

    @PostMapping("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{walletId}/balance")
    @ApiOperation("企业成员余额账户余额修改")
    public void updateWalletCashAmount(
            @PathVariable Integer walletId,
            @PathVariable Integer companyMemberId,
            @Validated @RequestBody UpdateWalletCashAmountReq req
    ) {
        DealService.DealForAdminParam param = DealService.DealForAdminParam
                .builder()
                .company(ContextUtil.getUserInfo().getCompany())
                .companyMember(companyMemberService.getById(companyMemberId))
                .walletId(walletId)
                .dealType(req.getDealType())
                .amount(new BigDecimal(req.getAmount()))
                .build();

        dealService.dealCashForAdmin(param);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class BatchUpdateWalletCashAmountReq {
        @NotEmpty
        List<Integer> walletIds;
        @NotEmpty
        List<Integer> companyMemberIds;
        @NotNull
        Deal.DealType dealType;
        @ApiModelProperty(required = true, value = "充值金额 或者 扣减金额", example = "50.0")
        String amount;
    }

    @PostMapping("/companys/{companyId}/members/wallet/cashs/balance")
    @ApiOperation("批量企业成员余额账户余额修改")
    public void batchUpdateWalletCashAmount(@Validated @RequestBody BatchUpdateWalletCashAmountReq req) {
        List<Integer> walletIds = req.getWalletIds();
        List<Integer> companyMemberIds = req.getCompanyMemberIds();
        if (walletIds.size() != companyMemberIds.size()) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }

        List<DealService.DealForAdminParam> list = IntStream.range(0, walletIds.size())
                .mapToObj(
                        i ->
                                DealService.DealForAdminParam
                                        .builder()
                                        .company(ContextUtil.getUserInfo().getCompany())
                                        .companyMember(companyMemberService.getById(companyMemberIds.get(i)))
                                        .walletId(walletIds.get(i))
                                        .dealType(req.getDealType())
                                        .amount(new BigDecimal(req.getAmount()))
                                        .build()
                )
                .collect(Collectors.toList());

        dealService.batchDealCashForAdmin(list);
    }

    @Data
    @Builder
    @ApiModel("企业成员余额账户余额修改批量导入返回结果")
    public static class ImportUpdateWalletCashAmountRes {
        int totalCount;
        List<DealService.DealCashImportRow> failRows;
    }

    @PostMapping(value = "/companys/{companyId}/members/wallet/cashs/balance/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("批量企业成员余额账户余额修改导入")
    public ImportUpdateWalletCashAmountRes importUpdateWalletCashAmount(@RequestParam("file") MultipartFile file) throws IOException {
        DealService.DealCashImportListener listener = new DealService.DealCashImportListener(dealService);

        EasyExcel
                .read(
                        file.getInputStream(),
                        DealService.DealCashImportRow.class,
                        listener
                )
                .sheet()
                .doRead();

        return ImportUpdateWalletCashAmountRes
                .builder()
                .totalCount(listener.getTotal())
                .failRows(listener.getFailRows())
                .build();
    }

    @GetMapping("/companys/{companyId}/members/wallet/cashs/balance/excel/template/export")
    @ApiOperation("批量企业成员余额账户余额修改导入模板下载")
    public void importUpdateWalletCashAmountTemplate(HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(response, "余额账户余额修改批量导入模板");

        EasyExcel
                .write(
                        response.getOutputStream(),
                        DealService.DealCashImportRow.class
                )
                .sheet("模板")
                .doWrite(DealService.DealCashImportRow.data());
    }

    /*
     *******************余额账户相关 end**********************
     */

    /*
     **********************信用账户相关 start**********************
     */
    @GetMapping("/companys/{companyId}/members/wallet/credit")
    @ApiOperation("企业成员信用账户查询")
    public IPage<WalletCreditMapper.WalletCreditDTO> listWalletCredit(@Validated ListWalletReq req) {
        return walletCreditMapper.listWalletCredit(
                new Page(req.getPageNo(), req.getPageSize()),
                ContextUtil.getUserInfo().getCompany().getId(),
                req.getDepartment(),
                req.getName()
        );
    }

    @GetMapping("/companys/{companyId}/members/wallet/credit/export")
    @ApiOperation("企业成员信用账户明细导出")
    public void listWalletCreditExport(@Validated ListWalletReq req, HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(
                response,
                "信用管理明细" + DateTimeFormatter.ISO_DATE.format(LocalDateTime.now())
        );

        EasyExcel
                .write(
                        response.getOutputStream(),
                        WalletCreditService.WalletCreditExportRow.class
                )
                .sheet("sheet")
                .doWrite(
                        walletCreditService.listWalletCreditExport(
                                ContextUtil.getUserInfo().getCompany().getId(),
                                req.getDepartment(),
                                req.getName()
                        )
                );

    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class UpdateWalletCreditStatusReq {
        @NotNull WalletCredit.WalletCreditStaus status;
    }

    @PatchMapping("/companys/{companyId}/members/{companyMemberId}/wallet/credit/{walletId}/status")
    @ApiOperation("企业成员信用钱包状态修改")
    public void updateWalletCreditStatus(@PathVariable Integer walletId, @Validated @RequestBody UpdateWalletCreditStatusReq req) {
        WalletCredit walletCredit = walletCreditService.getById(walletId);
        if (Objects.isNull(walletCredit)) {
            throw new BaseRuntimeException(ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        walletCreditService.update(
                Wrappers
                        .lambdaUpdate(WalletCredit.class)
                        .set(WalletCredit::getStatus, req.getStatus())
                        .eq(WalletCredit::getId, walletCredit.getId())
        );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class UpdateWalletCreditQuotaReq {
        @NotBlank
        @ApiModelProperty(required = true, value = "修改后的额度", example = "2000")
        String quota;
    }

    @PostMapping("/companys/{companyId}/members/{companyMemberId}/wallet/credit/{walletId}/quota")
    @ApiOperation("企业成员信用额度修改")
    public void updateWalletCreditQuota(
            @PathVariable Integer walletId,
            @PathVariable Integer companyMemberId,
            @Validated @RequestBody UpdateWalletCreditQuotaReq req
    ) {
        DealService.DealForAdminParam param = DealService.DealForAdminParam
                .builder()
                .company(ContextUtil.getUserInfo().getCompany())
                .companyMember(companyMemberService.getById(companyMemberId))
                .walletId(walletId)
                .dealType(Deal.DealType.ADMIN_QUOTA)
                .amount(new BigDecimal(req.getQuota()))
                .build();

        dealService.dealCreditForAdmin(param);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class BatchUpdateWalletCreditQuotaReq {
        @NotEmpty List<Integer> walletIds;
        @NotEmpty List<Integer> companyMemberIds;
        @NotBlank String quota;
    }

    @PostMapping("/companys/{companyId}/members/wallet/credits/balance")
    @ApiOperation("批量企业成员信用账户额度修改")
    public void batchUpdateWalletCreditQuota(@Validated @RequestBody BatchUpdateWalletCreditQuotaReq req) {
        List<Integer> walletIds = req.getWalletIds();
        List<Integer> companyMemberIds = req.getCompanyMemberIds();

        if (walletIds.size() != companyMemberIds.size()) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }

        List<DealService.DealForAdminParam> list = IntStream.range(0, walletIds.size())
                .mapToObj(
                        i ->
                                DealService.DealForAdminParam
                                        .builder()
                                        .company(ContextUtil.getUserInfo().getCompany())
                                        .companyMember(companyMemberService.getById(companyMemberIds.get(i)))
                                        .walletId(walletIds.get(i))
                                        .dealType(Deal.DealType.ADMIN_QUOTA)
                                        .amount(new BigDecimal(req.getQuota()))
                                        .build()
                )
                .collect(Collectors.toList());

        dealService.batchDealCreditForAdmin(list);
    }

    /*
     **********************信用账户相关 end**********************
     */
}
