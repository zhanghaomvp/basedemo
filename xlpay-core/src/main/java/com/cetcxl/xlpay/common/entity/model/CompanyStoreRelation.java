package com.cetcxl.xlpay.common.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
import com.cetcxl.xlpay.common.entity.plugin.IBitEnum;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CompanyStoreRelation对象", description = "")
public class CompanyStoreRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer company;

    private Integer store;

    private Integer relation;

    private Integer applyReleation;

    private RelationStatus status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum Relation implements IBitEnum {
        CASH_PAY(1),
        CREDIT_PAY(2),
        ;

        private Integer value;

        Relation(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return this.value;
        }

        @Override
        public int getBitPos() {
            return this.value;
        }
    }

    public enum RelationStatus implements IEnum<Integer> {
        APPROVAL(0),
        WORKING(1),
        ;
        private Integer status;

        RelationStatus(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }
}
