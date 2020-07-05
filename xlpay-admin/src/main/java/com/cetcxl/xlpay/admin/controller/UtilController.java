package com.cetcxl.xlpay.admin.controller;

import com.cetcxl.xlpay.admin.entity.vo.CompanyVO;
import com.cetcxl.xlpay.admin.service.VerifyCodeService;
import com.cetcxl.xlpay.common.constants.PatternConstants;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Attachment;
import com.cetcxl.xlpay.common.entity.vo.AttachmentVO;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.cetcxl.xlpay.common.service.AttachmentService;
import com.cetcxl.xlpay.common.service.XstoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;

@Validated
@RestController
@Api(tags = "工具接口")
public class UtilController extends BaseController {
    @Autowired
    private VerifyCodeService verifyCodeService;

    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private XstoreService xstoreService;

    @PostMapping(value = "/util/verify-code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiOperation("发送验证码")
    @ApiImplicitParam(name = "phone", paramType = "form")
    public ResBody<CompanyVO> sendSmsVerifyCode(
            @Pattern(regexp = PatternConstants.PHONE)
            @RequestParam("phone")
                    String phone
    ) {
        boolean flag = verifyCodeService.sendVerifyCode(phone);
        if (!flag) {
            return ResBody.error();
        }

        return ResBody.success();
    }

    @PostMapping("/util/upload/xstore")
    @ApiOperation("文件上传")
    public ResBody<AttachmentVO> upload(
            @RequestParam("file") MultipartFile file
    ) {
        AttachmentVO attachmentVO = AttachmentVO.of(xstoreService.uploadFile(file), AttachmentVO.class);
        return ResBody.success(attachmentVO);
    }

    @GetMapping("/util/download/xstore/{attachId}")
    @ApiOperation("文件下载")
    public void download(
            @PathVariable("attachId") Integer attachId,
            HttpServletResponse response
    ) throws Exception {
        Attachment attachment = attachmentService.getById(attachId);
        resolveAttachmentResponseHeader(response, attachment);

        byte[] bytes = xstoreService.downloadFile(Long.valueOf(attachment.getResoure()));
        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.flush();
        out.close();
    }
}
