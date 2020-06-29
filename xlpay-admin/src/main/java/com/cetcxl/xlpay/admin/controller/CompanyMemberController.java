package com.cetcxl.xlpay.admin.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.constants.ResultCode;
import com.cetcxl.xlpay.admin.dao.WalletCashMapper;
import com.cetcxl.xlpay.admin.dao.WalletCreditMapper;
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
import com.cetcxl.xlpay.common.rpc.ResBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResBody<IPage<WalletCashMapper.WalletCashDTO>> listWalletCash(@Validated ListWalletReq req) {
        IPage<WalletCashMapper.WalletCashDTO> iPage = walletCashMapper.listWalletCash(
                new Page(req.getPageNo(), req.getPageSize()),
                ContextUtil.getUserInfo().getCompanyId(),
                req.getDepartment(),
                req.getName());

        return ResBody.success(iPage);
    }

    @GetMapping("/companys/{companyId}/members/wallet/cash/export")
    @ApiOperation("企业成员余额账户明细导出")
    public void listWalletCashExport(@Validated ListWalletReq req, HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(
                response,
                "余额管理明细" + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
        );

        EasyExcel
                .write(
                        response.getOutputStream(),
                        WalletCashService.WalletCashExportRow.class
                )
                .sheet("sheet")
                .doWrite(
                        walletCashService.listWalletCashExport(
                                ContextUtil.getUserInfo().getCompanyId(),
                                req.getDepartment(),
                                req.getName()
                        )
                );
    }

    @PatchMapping("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{walletId}/status")
    @ApiOperation("企业成员余额账户状态修改")
    public ResBody updateWalletCashStatus(@PathVariable Integer walletId, @NotNull WalletCash.WalletCashStaus status) {
        //todo 分布式锁待添加
        WalletCash walletCash = walletCashService.getById(walletId);
        if (Objects.isNull(walletCash)) {
            return ResBody.error(ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        walletCashService.update(
                Wrappers
                        .lambdaUpdate(WalletCash.class)
                        .set(WalletCash::getStatus, status)
                        .eq(WalletCash::getId, walletCash.getId())
        );
        return ResBody.success();
    }

    @PostMapping("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{walletId}/balance")
    @ApiOperation("企业成员余额账户余额修改")
    public ResBody updateWalletCashAmount(
            @PathVariable Integer walletId,
            @PathVariable Integer companyMemberId,
            @NotNull Deal.DealType dealType,
            @NotBlank
            @ApiParam(required = true, value = "充值金额 或者 扣减金额", example = "50.0")
                    String amount
    ) {
        DealService.DealForAdminParam param = DealService.DealForAdminParam
                .builder()
                .company(ContextUtil.getUserInfo().getCompanyId())
                .companyMember(companyMemberId)
                .walletId(walletId)
                .dealType(dealType)
                .amount(new BigDecimal(amount))
                .build();

        dealService.dealCashForAdmin(param);
        return ResBody.success();
    }

    @PostMapping("/companys/{companyId}/members/wallet/cashs/balance")
    @ApiOperation("批量企业成员余额账户余额修改")
    public ResBody batchUpdateWalletCashAmount(
            @RequestParam(value = "walletIds[]") @NotEmpty Integer[] walletIds,
            @RequestParam(value = "companyMemberIds[]") @NotEmpty Integer[] companyMemberIds,
            @NotNull Deal.DealType dealType,
            @NotBlank
            @ApiParam(required = true, value = "充值金额 或者 扣减金额", example = "50.0")
                    String amount) {
        if (walletIds.length != companyMemberIds.length) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }

        List<DealService.DealForAdminParam> list = IntStream.range(0, walletIds.length)
                .mapToObj(
                        value ->
                                DealService.DealForAdminParam
                                        .builder()
                                        .company(ContextUtil.getUserInfo().getCompanyId())
                                        .companyMember(companyMemberIds[value])
                                        .walletId(walletIds[value])
                                        .dealType(dealType)
                                        .amount(new BigDecimal(amount))
                                        .build()
                )
                .collect(Collectors.toList());

        dealService.batchDealCashForAdmin(list);
        return ResBody.success();
    }

    @Data
    @Builder
    @ApiModel("企业成员余额账户余额修改批量导入返回结果")
    public static class ImportUpdateWalletCashAmountRes {
        int totalCount;
        List<DealService.DealCashImportRow> failRows;
    }

    @PostMapping("/companys/{companyId}/members/wallet/cashs/balance/import")
    @ApiOperation("批量企业成员余额账户余额修改导入")
    public ResBody importUpdateWalletCashAmount(@RequestParam("file") MultipartFile file) throws IOException {
        DealService.DealCashImportListener listener = new DealService.DealCashImportListener(dealService);

        EasyExcel
                .read(
                        file.getInputStream(),
                        DealService.DealCashImportRow.class,
                        listener
                )
                .sheet()
                .doRead();

        return ResBody
                .success(
                        ImportUpdateWalletCashAmountRes
                                .builder()
                                .totalCount(listener.getTotal())
                                .failRows(listener.getFailRows())
                                .build()
                );
    }

    @GetMapping("/companys/{companyId}/members/wallet/cashs/balance/excel/template")
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
    public ResBody<IPage<WalletCreditMapper.WalletCreditDTO>> listWalletCredit(@Validated ListWalletReq req) {
        IPage<WalletCreditMapper.WalletCreditDTO> iPage = walletCreditMapper.listWalletCredit(
                new Page(req.getPageNo(), req.getPageSize()),
                ContextUtil.getUserInfo().getCompanyId(),
                req.getDepartment(),
                req.getName()
        );

        return ResBody.success(iPage);
    }

    @GetMapping("/companys/{companyId}/members/wallet/credit/export")
    @ApiOperation("企业成员信用账户明细导出")
    public void listWalletCreditExport(@Validated ListWalletReq req, HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(
                response,
                "信用管理明细" + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
        );

        EasyExcel
                .write(
                        response.getOutputStream(),
                        WalletCreditService.WalletCreditExportRow.class
                )
                .sheet("sheet")
                .doWrite(
                        walletCreditService.listWalletCreditExport(
                                ContextUtil.getUserInfo().getCompanyId(),
                                req.getDepartment(),
                                req.getName()
                        )
                );

    }

    @PatchMapping("/companys/{companyId}/members/{companyMemberId}/wallet/credit/{walletId}/status")
    @ApiOperation("企业成员信用钱包状态修改")
    public ResBody updateWalletCreditStatus(@PathVariable Integer walletId, @NotNull WalletCredit.WalletCreditStaus status) {
        //todo 分布式锁待添加
        WalletCredit walletCredit = walletCreditService.getById(walletId);
        if (Objects.isNull(walletCredit)) {
            return ResBody.error(ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        walletCreditService.update(
                Wrappers
                        .lambdaUpdate(WalletCredit.class)
                        .set(WalletCredit::getStatus, status)
                        .eq(WalletCredit::getId, walletCredit.getId())
        );
        return ResBody.success();
    }

    @PostMapping("/companys/{companyId}/members/{companyMemberId}/wallet/credit/{walletId}/quota")
    @ApiOperation("企业成员信用额度修改")
    public ResBody updateWalletCreditQuota(
            @PathVariable Integer walletId,
            @PathVariable Integer companyMemberId,
            @NotBlank
            @ApiParam(required = true, value = "修改后的额度", example = "2000")
                    String quota
    ) {
        DealService.DealForAdminParam param = DealService.DealForAdminParam
                .builder()
                .company(ContextUtil.getUserInfo().getCompanyId())
                .companyMember(companyMemberId)
                .walletId(walletId)
                .dealType(Deal.DealType.ADMIN_QUOTA)
                .amount(new BigDecimal(quota))
                .build();

        dealService.dealCreditForAdmin(param);
        return ResBody.success();
    }

    @PostMapping("/companys/{companyId}/members/wallet/credits/balance")
    @ApiOperation("批量企业成员信用账户额度修改")
    public ResBody batchUpdateWalletCreditQuota(
            @RequestParam(value = "walletIds[]") @NotEmpty Integer[] walletIds,
            @RequestParam(value = "companyMemberIds[]") @NotEmpty Integer[] companyMemberIds,
            @NotBlank String quota) {
        if (walletIds.length != companyMemberIds.length) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }

        List<DealService.DealForAdminParam> list = IntStream.range(0, walletIds.length)
                .mapToObj(
                        value ->
                                DealService.DealForAdminParam
                                        .builder()
                                        .company(ContextUtil.getUserInfo().getCompanyId())
                                        .companyMember(companyMemberIds[value])
                                        .walletId(walletIds[value])
                                        .dealType(Deal.DealType.ADMIN_QUOTA)
                                        .amount(new BigDecimal(quota))
                                        .build()
                )
                .collect(Collectors.toList());

        dealService.batchDealCreditForAdmin(list);
        return ResBody.success();
    }

    /*
     **********************信用账户相关 end**********************
     */
}
