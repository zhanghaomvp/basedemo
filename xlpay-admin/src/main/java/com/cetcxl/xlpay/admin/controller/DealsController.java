package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.dao.DealMapper;
import com.cetcxl.xlpay.admin.entity.vo.DealVO;
import com.cetcxl.xlpay.admin.service.DealService;
import com.cetcxl.xlpay.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.rpc.ResBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.cetcxl.xlpay.common.constants.PatternConstants.DATE_TIME;

@Validated
@RestController
@Api(tags = "账单相关接口")
public class DealsController extends BaseController {
    @Autowired
    DealService dealService;
    @Autowired
    DealMapper dealMapper;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class ListDealReq extends MybatisPlusConfig.PageReq {
        Integer companyId;
        Integer storeId;

        String companyName;
        String storeName;

        String name;
        String department;
        Deal.PayType payType;
        Deal.Status status;

        @NotNull
        @DateTimeFormat(pattern = DATE_TIME)
        LocalDateTime begin;
        @NotNull
        @DateTimeFormat(pattern = DATE_TIME)
        LocalDateTime end;
    }

    @GetMapping("/companys/{companyId}/deals")
    @ApiOperation("企业账单查询")
    public ResBody<IPage<DealMapper.DealDTO>> ListCompanyDeal(@Validated ListDealReq req) {
        return ResBody
                .success(
                        dealMapper.listDeal(
                                new Page(req.getPageNo(), req.getPageSize()),
                                req
                        )
                );
    }

    @GetMapping("/stores/{storeId}/deals")
    @ApiOperation("商家账单查询")
    public ResBody<IPage<DealMapper.DealDTO>> ListStoreDeal(@Validated ListDealReq req) {
        return ResBody
                .success(
                        dealMapper.listDeal(
                                new Page(req.getPageNo(), req.getPageSize()),
                                req
                        )
                );
    }

    @GetMapping("/deals/{dealId}")
    @ApiOperation("账单详情查询")
    public ResBody getDealDetail(@PathVariable Integer dealId) {
        DealVO dealVO = DealVO.of(dealService.getById(dealId), DealVO.class);
        return ResBody.success(dealVO);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class DashboardReq {
        Integer companyId;
        Integer storeId;

        String department;
        String companyName;

        @NotNull
        @DateTimeFormat(pattern = DATE_TIME)
        LocalDateTime begin;
        @NotNull
        @DateTimeFormat(pattern = DATE_TIME)
        LocalDateTime end;
    }

    @GetMapping("/companys/{companyId}/deals/dashboard")
    @ApiOperation("企业数据看板")
    public ResBody<DealMapper.DashboardDTO> companyDashboard(@Validated DashboardReq req, @PathVariable Integer companyId) {
        if (StringUtils.isNotBlank(req.getDepartment())) {
            return ResBody
                    .success(
                            dealMapper.companyDashboardWithDepartment(req)
                    );
        } else {
            return ResBody
                    .success(
                            dealMapper.companyDashboardWithOutDepartment(req)
                    );
        }
    }
}
