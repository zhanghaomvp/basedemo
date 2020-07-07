package com.cetcxl.xlpay.common.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
@ApiModel(value = "Checks对象", description = "")
public class Checks implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "batch", type = IdType.AUTO)
    private Integer batch;

    private Integer company;

    private Integer store;

    private Deal.PayType payType;

    private Integer totalDealCount;

    private BigDecimal totalDealAmonut;

    private String attachments;

    private String info;

    private Status status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum Status implements IEnum<Integer> {
        APPLY(0),
        REJECT(1),
        APPROVAL(2),
        DENY(3),
        COFIRM(4),
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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InfoRecord {
        Status status;
        String info;
    }

    public Checks appendInfo(String info) {
        try {
            List<InfoRecord> list;

            if (StringUtils.isNotBlank(this.info)) {
                list = new ObjectMapper()
                        .readValue(
                                this.info,
                                new TypeReference<List<InfoRecord>>() {
                                }
                        );
            } else {
                list = Lists.newArrayList();
            }

            list.add(
                    InfoRecord.builder()
                            .status(this.status)
                            .info(info)
                            .build()
            );
            this.info = new ObjectMapper().writeValueAsString(list);
            return this;
        } catch (JsonProcessingException e) {
            throw new BaseRuntimeException(e);
        }
    }
}
