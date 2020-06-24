package com.cetcxl.xlpay.admin.server.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.server.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.admin.server.common.controller.BaseController;
import com.cetcxl.xlpay.admin.server.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import com.cetcxl.xlpay.admin.server.dao.WalletCashMapper;
import com.cetcxl.xlpay.admin.server.dao.WalletCreditMapper;
import com.cetcxl.xlpay.admin.server.entity.model.Deal;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCash;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCredit;
import com.cetcxl.xlpay.admin.server.service.DealService;
import com.cetcxl.xlpay.admin.server.service.WalletCashService;
import com.cetcxl.xlpay.admin.server.service.WalletCreditService;
import com.cetcxl.xlpay.admin.server.util.ContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.cetcxl.xlpay.admin.server.constants.ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST;
import static com.cetcxl.xlpay.admin.server.constants.ResultCode.SYSTEM_LOGIC_ERROR;

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
    public ResBody<IPage> listWalletCash(@Validated ListWalletReq req) {
        IPage<WalletCashMapper.WalletCashDTO> iPage = walletCashMapper.listWalletCash(
                new Page(req.getPageNo(), req.getPageSize()),
                ContextUtil.getUserInfo().getCompanyId(),
                req.getDepartment(),
                req.getName());

        return ResBody.success(iPage);
    }

    @PatchMapping("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{id}/status")
    @ApiOperation("企业成员钱包状态修改")
    public ResBody updateWalletCashStatus(@PathVariable Integer id, @NotNull WalletCash.WalletCashStaus status) {
        //todo 分布式锁待添加
        WalletCash walletCash = walletCashService.getById(id);
        if (Objects.isNull(walletCash)) {
            return ResBody.error(COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        walletCashService.update(
                Wrappers
                        .lambdaUpdate(WalletCash.class)
                        .set(WalletCash::getStatus, status)
                        .eq(WalletCash::getId, walletCash.getId())
        );
        return ResBody.success();
    }

    @PostMapping("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{id}/balance")
    @ApiOperation("企业成员余额修改")
    public ResBody updateWalletCashAmount(
            @PathVariable Integer id,
            @PathVariable Integer companyMemberId,
            @NotNull Deal.DealType dealType,
            @NotBlank String amount) {
        DealService.DealForAdminParam param = DealService.DealForAdminParam
                .builder()
                .company(ContextUtil.getUserInfo().getCompanyId())
                .companyMember(companyMemberId)
                .walletId(id)
                .dealType(dealType)
                .amount(new BigDecimal(amount))
                .build();

        dealService.dealCashForAdmin(param);
        return ResBody.success();
    }

    @PostMapping("/companys/{companyId}/members/wallet/cashs/balance")
    @ApiOperation("企业成员余额修改")
    public ResBody batchUpdateWalletCashAmount(
            @RequestParam(value = "walletIds[]") @NotEmpty Integer[] walletIds,
            @RequestParam(value = "companyMemberIds[]") @NotEmpty Integer[] companyMemberIds,
            @NotNull Deal.DealType dealType,
            @NotBlank String amount) {
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
    @ApiModel("")
    public static class ImportUpdateWalletCashAmountRes {
        int totalCount;
        List<DealService.DealCashImportRow> failRows;
    }

    @PostMapping("/companys/{companyId}/members/wallet/cashs/balance/excel")
    @ApiOperation("企业成员余额批量导入")
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

    /*
     *******************余额账户相关 end**********************
     */

    /*
     **********************信用账户相关 start**********************
     */
    @GetMapping("/companys/{companyId}/members/wallet/credit")
    @ApiOperation("企业成员信用账户查询")
    public ResBody<IPage> listWalletCredit(@Validated ListWalletReq req) {
        IPage<WalletCreditMapper.WalletCreditDTO> iPage = walletCreditMapper.listWalletCredit(
                new Page(req.getPageNo(), req.getPageSize()),
                ContextUtil.getUserInfo().getCompanyId(),
                req.getDepartment(),
                req.getName()
        );

        return ResBody.success(iPage);
    }

    @PatchMapping("/companys/{companyId}/members/{companyMemberId}/wallet/credit/{id}/status")
    @ApiOperation("企业成员信用钱包状态修改")
    public ResBody updateWalletCreditStatus(@PathVariable Integer id, @NotNull WalletCredit.WalletCreditStaus status) {
        //todo 分布式锁待添加
        WalletCredit walletCredit = walletCreditService.getById(id);
        if (Objects.isNull(walletCredit)) {
            return ResBody.error(COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        walletCreditService.update(
                Wrappers
                        .lambdaUpdate(WalletCredit.class)
                        .set(WalletCredit::getStatus, status)
                        .eq(WalletCredit::getId, walletCredit.getId())
        );
        return ResBody.success();
    }

    @PostMapping("/companys/{companyId}/members/{companyMemberId}/wallet/credit/{id}/quota")
    @ApiOperation("企业成员信用额度修改")
    public ResBody updateWalletCreditQuota(@PathVariable Integer id, @PathVariable Integer companyMemberId, @NotBlank String quota) {
        DealService.DealForAdminParam param = DealService.DealForAdminParam
                .builder()
                .company(ContextUtil.getUserInfo().getCompanyId())
                .companyMember(companyMemberId)
                .walletId(id)
                .dealType(Deal.DealType.ADMIN_QUOTA)
                .amount(new BigDecimal(quota))
                .build();

        dealService.dealCreditForAdmin(param);
        return ResBody.success();
    }

    @PostMapping("/companys/{companyId}/members/wallet/credits/balance")
    @ApiOperation("批量企业成员信用额度修改")
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
