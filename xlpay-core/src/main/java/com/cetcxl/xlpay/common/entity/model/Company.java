package com.cetcxl.xlpay.common.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@ApiModel(value = "Company对象", description = "")
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String contact;

    private String phone;

    private String email;

    private String socialCreditCode;

    private Integer functions;

    @TableField("STATUS")
    private CompanyStatus status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum CompanyStatus implements IEnum<Integer> {
        DISABLE(0),
        ACTIVE(1),
        ;
        private Integer status;

        CompanyStatus(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }

    public enum CompanyFuntion implements IBitEnum {
        MEMBER_PAY(1),
        ;
        private Integer bigPos;

        CompanyFuntion(Integer bigPos) {
            this.bigPos = bigPos;
        }

        @Override
        public int getBitPos() {
            return this.bigPos;
        }
    }

}
