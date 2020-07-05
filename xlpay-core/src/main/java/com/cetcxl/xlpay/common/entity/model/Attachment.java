package com.cetcxl.xlpay.common.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
import com.google.common.net.MediaType;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

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
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Attachment对象", description = "")
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Category category;

    private String fileName;

    private FileType fileType;

    private String resoure;

    private Status status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum Category implements IEnum<Integer> {
        XSTORE(0),
        ;
        private Integer value;

        Category(Integer value) {
            this.value = value;
        }

        @Override
        public Integer getValue() {
            return this.value;
        }
    }

    public enum FileType implements IEnum<Integer> {
        PICTURE(0, MediaType.ANY_IMAGE_TYPE, new String[]{}),
        EXCEL(1, MediaType.MICROSOFT_EXCEL, new String[]{"xlsx"}),
        WORD(2, MediaType.MICROSOFT_WORD, new String[]{}),
        ;
        private Integer value;
        private MediaType mediaType;
        private String[] suffixs;

        FileType(Integer value, MediaType mediaType, String[] suffixs) {
            this.value = value;
            this.mediaType = mediaType;
            this.suffixs = suffixs;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        @Override
        public Integer getValue() {
            return this.value;
        }

        public static Optional<FileType> of(String suffix) {
            for (FileType fileType : values()) {
                String[] suffixs = fileType.suffixs;
                for (String s : suffixs) {
                    if (s.contains(suffix)) {
                        return Optional.of(fileType);
                    }
                }
            }
            return Optional.empty();
        }
    }

    public enum Status implements IEnum<Integer> {
        AVALALIABLE(0),
        DELETE(1),
        ;
        private Integer status;

        Status(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }

}
