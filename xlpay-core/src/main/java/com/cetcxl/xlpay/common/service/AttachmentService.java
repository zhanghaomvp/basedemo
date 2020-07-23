package com.cetcxl.xlpay.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.common.dao.AttachmentMapper;
import com.cetcxl.xlpay.common.entity.model.Attachment;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.FILE_OVERSIZE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-07-05
 */
@Service
public class AttachmentService extends ServiceImpl<AttachmentMapper, Attachment> {
    @Autowired
    XstoreService xstoreService;

    public Attachment addXstoreFile(MultipartFile file) {
        Attachment.FileType fileType = Attachment.FileType.of(
                StringUtils.substringAfterLast(file.getOriginalFilename(), ".")
        );

        if (file.getSize() > fileType.getMaxSize()) {
            throw new BaseRuntimeException(FILE_OVERSIZE);
        }

        Attachment attachment = Attachment.builder()
                .category(Attachment.Category.XSTORE)
                .fileName(file.getOriginalFilename())
                .fileType(fileType)
                .resoure(String.valueOf(xstoreService.uploadFile(file)))
                .status(Attachment.Status.AVALALIABLE)
                .build();

        save(attachment);
        return attachment;
    }
}
