package com.cetcxl.xlpay.common.service;

import com.cetcxl.xlpay.common.entity.model.Attachment;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.FILE_TYPE_NOT_SUPPORT;

@Slf4j
@Component
public class XstoreService implements InitializingBean {

    @Autowired
    private XstorConfigrution xstorConfigrution;
    @Autowired
    AttachmentService attachmentService;

    public Attachment uploadFile(MultipartFile file) {
        Optional<Attachment.FileType> fileType = Attachment.FileType.of(
                StringUtils.substringAfterLast(file.getOriginalFilename(), ".")
        );
        if (!fileType.isPresent()) {
            throw new BaseRuntimeException(FILE_TYPE_NOT_SUPPORT);
        }
        /*try {
            return xstorSdk.upload(
                    UUID.randomUUID().toString().replaceAll("-", ""),
                    file.getInputStream(),
                    file.getSize(),
                    file.getOriginalFilename(),
                    StringUtils.substringAfterLast(file.getOriginalFilename(), "."),
                    xstorConfigrution.getSk(),
                    xstorConfigrution.getPk());
        } catch (Exception e) {
            throw new BaseRuntimeException(e, XSTORE_UPLOAD_FAIL);
        }*/

        Attachment attachment = Attachment.builder()
                .category(Attachment.Category.XSTORE)
                .fileName(file.getOriginalFilename())
                .fileType(fileType.get())
                .resoure("0L")
                .status(Attachment.Status.AVALALIABLE)
                .build();
        attachmentService.save(attachment);
        return attachment;
    }

    public byte[] downloadFile(Long resourceId) {
        /*try {
            return xstorSdk.download(resourceId, xstorConfigrution.getSk());
        } catch (Exception e) {
            throw new BaseRuntimeException(e, XSTORE_UPLOAD_FAIL);
        }*/
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /*xstorSdk = new XstorSdk(
                xstorConfigrution.getAppId(),
                xstorConfigrution.getAppKey(),
                StringUtils.EMPTY);*/
    }

    @Component
    @ConfigurationProperties(prefix = "xstor")
    @Data
    public static class XstorConfigrution {
        private String appId;
        private String appKey;
        private String sk;
        private String pk;
    }
}