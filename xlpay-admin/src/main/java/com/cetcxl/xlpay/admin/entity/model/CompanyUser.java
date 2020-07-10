package com.cetcxl.xlpay.admin.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
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
@ApiModel(value = "CompanyUser对象", description = "")
public class CompanyUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer company;

    private String phone;

    private String password;

    @TableField("STATUS")
    private CompanyUserStatus status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum CompanyUserStatus implements IEnum<Integer> {
        DISABLE(0),
        ACTIVE(1),
        ;
        private Integer status;

        CompanyUserStatus(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }
}
