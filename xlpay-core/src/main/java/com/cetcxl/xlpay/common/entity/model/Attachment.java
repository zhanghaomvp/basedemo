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

/**b
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
        PICTURE(0, MediaType.ANY_IMAGE_TYPE, new String[]{"jpg", "jpeg", "png"}, 1024 * 500),
        EXCEL(1, MediaType.MICROSOFT_EXCEL, new String[]{"xlsx"}, -1),
        WORD(2, MediaType.MICROSOFT_WORD, new String[]{}, -1),
        Other(99, MediaType.OCTET_STREAM, new String[]{}, -1),
        ;

        static int DEFAULT_MAX_SIZE = 1024 * 1024 * 20;

        private Integer value;
        private MediaType mediaType;
        private String[] suffixs;
        //单位 字节
        private Integer maxSize;

        FileType(Integer value, MediaType mediaType, String[] suffixs, Integer maxSize) {
            this.value = value;
            this.mediaType = mediaType;
            this.suffixs = suffixs;
            this.maxSize = maxSize;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public Integer getMaxSize() {
            if (this.maxSize < 0) {
                return DEFAULT_MAX_SIZE;
            }
            return this.maxSize;
        }

        @Override
        public Integer getValue() {
            return this.value;
        }

        public static FileType of(String suffix) {
            for (FileType fileType : values()) {
                String[] suffixs = fileType.suffixs;
                for (String s : suffixs) {
                    if (s.contains(suffix)) {
                        return fileType;
                    }
                }
            }
            return Other;
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
