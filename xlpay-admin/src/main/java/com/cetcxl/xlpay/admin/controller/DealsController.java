package com.cetcxl.xlpay.admin.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.dao.DealMapper;
import com.cetcxl.xlpay.admin.entity.vo.DealVO;
import com.cetcxl.xlpay.admin.service.DealService;
import com.cetcxl.xlpay.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.plugins.easyexcel.LocalDateTimeConverter;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cetcxl.xlpay.admin.service.DealService.DealExportRow.EXCLUDE_COMPANY_NAME;
import static com.cetcxl.xlpay.admin.service.DealService.DealExportRow.EXCLUDE_STORE_NAME;
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

        Integer checkBatch;

        @NotNull
        @DateTimeFormat(pattern = DATE_TIME)
        LocalDateTime begin;
        @NotNull
        @DateTimeFormat(pattern = DATE_TIME)
        LocalDateTime end;
    }

    @GetMapping("/companys/{companyId}/deals")
    @ApiOperation("企业账单查询")
    public IPage<DealMapper.DealDTO> listCompanyDeal(@Validated ListDealReq req) {
        return dealMapper.listDeal(
                new Page(req.getPageNo(), req.getPageSize()),
                req
        );
    }

    @GetMapping("/stores/{storeId}/deals")
    @ApiOperation("商家账单查询")
    public IPage<DealMapper.DealDTO> listStoreDeal(@Validated ListDealReq req) {
        return dealMapper.listDeal(
                new Page(req.getPageNo(), req.getPageSize()),
                req
        );
    }

    @GetMapping("/deals/{dealId}")
    @ApiOperation("账单详情查询")
    public DealVO getDealDetail(@PathVariable Integer dealId) {
        return DealVO.of(dealService.getById(dealId), DealVO.class);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class DashboardReq extends MybatisPlusConfig {

        @NotNull(groups = DashboardCompanyGroup.class)
        Integer companyId;

        @NotNull(groups = DashboardStoreGroup.class)
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

    interface DashboardCompanyGroup {
    }

    @GetMapping("/companys/{companyId}/deals/dashboard")
    @ApiOperation("企业数据看板")
    public DealMapper.DashboardDTO companyDashboard(@Validated(DashboardCompanyGroup.class) DashboardReq req) {

        if (StringUtils.isNotBlank(req.getDepartment())) {
            return dealService.calculationAmount(
                    dealMapper.companyDashboardWithDepartment(req)
            );
        } else {
            return dealService.calculationAmount(
                    dealMapper.companyDashboardWithOutDepartment(req)
            );
        }
    }

    interface DashboardStoreGroup {
    }

    @GetMapping("/stores/{storeId}/deals/dashboard")
    @ApiOperation("商家数据看板")
    public DealMapper.DashboardDTO storeDashboard(@Validated(DashboardStoreGroup.class) DashboardReq req) {
        if (StringUtils.isNotBlank(req.getCompanyName())) {
            return dealService.calculationAmount(
                    dealMapper.storeDashboardWithCompany(req)
            );
        } else {
            return dealService.calculationAmount(
                    dealMapper.storeDashboardWithOutCompany(req)
            );
        }
    }


    @GetMapping("/store/{storeId}/deal/export")
    @ApiOperation("商家账单明细导出")
    public void listStoreDealExport(@Validated DealsController.ListDealReq req, HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(
                response,
                "商家账单明细" + DateTimeFormatter.ISO_DATE.format(LocalDateTime.now())
        );

        EasyExcel
                .write(
                        response.getOutputStream(),
                        DealService.DealExportRow.class
                )
                .excludeColumnFiledNames(EXCLUDE_STORE_NAME)
                .registerConverter(new LocalDateTimeConverter())
                .sheet("sheet")
                .doWrite(
                        dealService.listDealExport(req)
                );
    }


    @GetMapping("/company/{companyId}/deal/export")
    @ApiOperation("企业账单明细导出")
    public void listCompanyDealExport(@Validated DealsController.ListDealReq req, HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(
                response,
                "企业账单明细" + DateTimeFormatter.ISO_DATE.format(LocalDateTime.now())
        );

        EasyExcel
                .write(
                        response.getOutputStream(),
                        DealService.DealExportRow.class
                )
                .sheet("sheet")
                .excludeColumnFiledNames(EXCLUDE_COMPANY_NAME)
                .registerConverter(new LocalDateTimeConverter())
                .doWrite(
                        dealService.listDealExport(req)
                );
    }


    @GetMapping("/company/check/{batch}/deals/export")
    @ApiOperation("企业结算详情明细导出")
    public void listCompanyCheckExport(@PathVariable Integer batch, HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(
                response,
                "企业结算详情明细" + DateTimeFormatter.ISO_DATE.format(LocalDateTime.now())
        );

        EasyExcel
                .write(
                        response.getOutputStream(),
                        DealService.DealExportRow.class
                )
                .sheet("sheet")
                .excludeColumnFiledNames(EXCLUDE_COMPANY_NAME)
                .registerConverter(new LocalDateTimeConverter())
                .doWrite(
                        dealService.listDetailExport(batch)
                );
    }

    @GetMapping("/store/check/{batch}/deals/export")
    @ApiOperation("商家结算详情明细导出")
    public void listStoreCheckExport(@PathVariable Integer batch, HttpServletResponse response) throws Exception {
        resolveExcelResponseHeader(
                response,
                "商家结算详情明细" + DateTimeFormatter.ISO_DATE.format(LocalDateTime.now())
        );

        EasyExcel
                .write(
                        response.getOutputStream(),
                        DealService.DealExportRow.class
                )
                .sheet("sheet")
                .excludeColumnFiledNames(EXCLUDE_STORE_NAME)
                .registerConverter(new LocalDateTimeConverter())
                .doWrite(
                        dealService.listDetailExport(batch)
                );
    }
}
