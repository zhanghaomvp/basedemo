package com.cetcxl.xlpay.common.controller;

import com.cetcxl.xlpay.common.entity.model.Attachment;
import com.google.common.net.MediaType;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

@Slf4j
public abstract class BaseController {

    public void resolvePicResponseHeader(HttpServletResponse response, String fileName) throws Exception {
        response.setContentType(MediaType.ANY_IMAGE_TYPE.toString());
        response.setCharacterEncoding("utf-8");
        response.setHeader(
                "Content-disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".jpg");
    }

    public void resolveExcelResponseHeader(HttpServletResponse response, String fileName) throws Exception {
        response.setContentType(MediaType.MICROSOFT_EXCEL.toString());
        response.setCharacterEncoding("utf-8");
        response.setHeader(
                "Content-disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
    }

    public void resolveAttachmentResponseHeader(HttpServletResponse response, Attachment attachment) throws Exception {
        response.setContentType(attachment.getFileType().getMediaType().toString());
        response.setCharacterEncoding("utf-8");
        response.setHeader(
                "Content-disposition",
                "attachment;filename=" + URLEncoder.encode(attachment.getFileName(), "UTF-8"));
    }
}
