package com.cetcxl.xlpay.common.entity.vo;

import com.cetcxl.xlpay.common.entity.model.Attachment;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author ${author}
 * @since 2020-07-05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Attachment对象", description = "")
public class AttachmentVO extends BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String fileName;

    private Attachment.Status status;

    private LocalDateTime created;

    private LocalDateTime updated;

}
