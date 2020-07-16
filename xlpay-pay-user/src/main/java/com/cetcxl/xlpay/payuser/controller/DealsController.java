package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.cetcxl.xlpay.payuser.dao.DealMapper;
import com.cetcxl.xlpay.payuser.entity.vo.DealVO;
import com.cetcxl.xlpay.payuser.service.CompanyService;
import com.cetcxl.xlpay.payuser.service.DealService;
import com.cetcxl.xlpay.payuser.util.ContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.cetcxl.xlpay.common.constants.PatternConstants.DATE_TIME;

@Validated
@RestController
@Api(tags = "账单查询接口")
public class DealsController extends BaseController {

    @Autowired
    DealMapper dealMapper;
    @Autowired
    DealService dealService;
    @Autowired
    CompanyService companyService;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class ListDealReq extends MybatisPlusConfig.PageReq {
        @NotBlank
        String socialCreditCode;
        String storeName;

        Deal.PayType payType;

        @NotNull
        @DateTimeFormat(pattern = DATE_TIME)
        LocalDateTime begin;
        @NotNull
        @DateTimeFormat(pattern = DATE_TIME)
        LocalDateTime end;
    }

    @GetMapping("/pay-user/deals")
    @ApiOperation("企业账单查询")
    public ResBody<IPage<DealMapper.DealDTO>> listDeal(@Validated ListDealReq req) {

        return ResBody
                .success(
                        dealMapper.listDeal(
                                new Page(req.getPageNo(), req.getPageSize()),
                                req,
                                ContextUtil.getUserInfo().getPayUser().getIcNo()
                        )
                );
    }

    @GetMapping("/pay-user/deals/{dealId}")
    @ApiOperation("企业账单详情查询")
    public ResBody<DealVO> deal(@PathVariable Integer dealId) {
        Deal deal = dealService.getById(dealId);
        DealVO dealVO = DealVO.of(deal, DealVO.class);

        dealVO.setCompanyName(companyService.getById(deal.getCompany()).getName());
        if (Deal.Status.CHECK_FINISH == deal.getStatus()) {
            dealVO.setCheckFinishTime(deal.getUpdated());
        }
        return ResBody.success(dealVO);
    }
}
