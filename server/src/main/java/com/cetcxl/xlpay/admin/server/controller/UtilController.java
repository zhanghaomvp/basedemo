package com.cetcxl.xlpay.admin.server.controller;

import com.cetcxl.xlpay.admin.server.common.constants.PatternConstants;
import com.cetcxl.xlpay.admin.server.common.controller.BaseController;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import com.cetcxl.xlpay.admin.server.common.service.XstoreService;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyVO;
import com.cetcxl.xlpay.admin.server.service.VerifyCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import java.io.IOException;

@Validated
@RestController
@Api(tags = "工具接口")
public class UtilController extends BaseController {
    @Autowired
    private VerifyCodeService verifyCodeService;
    @Autowired
    private XstoreService xstoreService;

    @PostMapping("/util/verify-code")
    @ApiOperation("发送验证码")
    public ResBody<CompanyVO> register(
            @Pattern(regexp = PatternConstants.PHONE)
            @RequestParam("phone") String phone
    ) {
        boolean flag = verifyCodeService.sendVerifyCode(phone);
        if (!flag) {
            return ResBody.error();
        }

        return ResBody.success();
    }

    @PostMapping("/util/upload")
    @ApiOperation("文件上传")
    public ResBody<Long> upload(
            @RequestParam("file") MultipartFile file
    ) {
        return ResBody.success(xstoreService.uploadFile(file));
    }

    @GetMapping("/util/download/{resoureceId}")
    @ApiOperation("文件下载")
    public void download(
            @PathVariable("resoureceId") long resoureceId,
            HttpServletResponse response
    ) throws IOException {
        byte[] bytes = xstoreService.downloadFile(resoureceId);

        ServletOutputStream out = response.getOutputStream();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        out.write(bytes);
        out.flush();
        out.close();
    }
}
