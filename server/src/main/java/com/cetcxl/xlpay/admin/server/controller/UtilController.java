package com.cetcxl.xlpay.admin.server.controller;

import com.cetcxl.xlpay.admin.server.common.constants.PatternConstants;
import com.cetcxl.xlpay.admin.server.common.controller.BaseController;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyVO;
import com.cetcxl.xlpay.admin.server.service.VerifyCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;

@Validated
@RestController
@Api(tags = "工具接口")
public class UtilController extends BaseController {
    @Autowired
    private VerifyCodeService verifyCodeService;

    @PostMapping("/util/verify-code")
    @ApiOperation("发送验证码")
    public ResBody<CompanyVO> register(
            @Pattern(regexp = PatternConstants.REGEX_PHONE)
            @RequestParam("phone") String phone
    ) {
        boolean flag = verifyCodeService.sendVerifyCode(phone);
        if (!flag) {
            return ResBody.error();
        }

        return ResBody.success();
    }
}
