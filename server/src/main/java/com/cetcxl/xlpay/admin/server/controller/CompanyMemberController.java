package com.cetcxl.xlpay.admin.server.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.server.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.admin.server.common.controller.BaseController;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import com.cetcxl.xlpay.admin.server.dao.WalletCashMapper;
import com.cetcxl.xlpay.admin.server.dao.WalletCreditMapper;
import com.cetcxl.xlpay.admin.server.entity.model.Deal;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCash;
import com.cetcxl.xlpay.admin.server.service.DealService;
import com.cetcxl.xlpay.admin.server.service.WalletCashService;
import com.cetcxl.xlpay.admin.server.util.ContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

import static com.cetcxl.xlpay.admin.server.constants.ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST;

@Validated
@RestController
@Api(tags = "企业成员相关接口")
public class CompanyMemberController extends BaseController {
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCashMapper walletCashMapper;
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

    @PatchMapping("/companys/{companyId}/members/wallet/cash/{id}/status")
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

    @PostMapping("/companys/{companyId}/members/wallet/cash/{id}/balance")
    @ApiOperation("企业成员余额修改")
    public ResBody updateWalletCashAmount(@PathVariable Integer id, @NotNull Deal.DealType dealType, @NotBlank String amount) {
        DealService.DealForAdminParam param = DealService.DealForAdminParam
                .builder()
                .company(ContextUtil.getUserInfo().getCompanyId())
                .walletId(id)
                .dealType(dealType)
                .amount(new BigDecimal(amount))
                .build();

        dealService.dealCashForAdmin(param);
        return ResBody.success();
    }

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

    @PostMapping("/companys/{companyId}/members/wallet/credit/{id}/quota")
    @ApiOperation("企业成员余额修改")
    public ResBody updateWalletCashQuota(@PathVariable Integer id, @NotBlank String quota) {
        DealService.DealForAdminParam param = DealService.DealForAdminParam
                .builder()
                .company(ContextUtil.getUserInfo().getCompanyId())
                .walletId(id)
                .dealType(Deal.DealType.ADMIN_QUOTA)
                .amount(new BigDecimal(quota))
                .build();

        dealService.dealCreditForAdmin(param);
        return ResBody.success();
    }
}
