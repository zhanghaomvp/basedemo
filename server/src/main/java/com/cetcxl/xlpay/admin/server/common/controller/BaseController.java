package com.cetcxl.xlpay.admin.server.common.controller;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

@Slf4j
public abstract class BaseController {

    public void resolveExcelResponseHeader(HttpServletResponse response, String fileName) throws Exception {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader(
                "Content-disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
    }
}
