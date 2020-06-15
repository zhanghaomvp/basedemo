package com.cetcxl.xlpay.admin.server.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.enums.IEnum;
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
 * @since 2020-06-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Company对象", description = "")
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String loginName;

    private String password;

    private String contact;

    private String phone;

    private String email;

    private String socialCreditCode;

    private Integer functions;

    private CompanyStatusEnum status;

    private LocalDateTime created;

    private LocalDateTime updated;

    public enum CompanyStatusEnum implements IEnum<Integer> {
        DISABLE(0),
        ACTIVE(1),
        ;
        private Integer status;

        CompanyStatusEnum(Integer status) {
            this.status = status;
        }

        @Override
        public Integer getValue() {
            return this.status;
        }
    }

    public enum CompanyFuntionEnum {
        MEMBER_PAY(1),
        ;
        private Integer value;

        CompanyFuntionEnum(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return this.value;
        }

        public boolean isOpen(Integer functions) {
            return (this.value & functions) > 0;
        }

        public Integer addFuntion(Integer functions) {
            return this.value | functions;
        }
    }
}
