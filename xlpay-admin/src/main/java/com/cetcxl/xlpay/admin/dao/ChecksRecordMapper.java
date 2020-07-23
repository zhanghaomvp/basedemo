package com.cetcxl.xlpay.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cetcxl.xlpay.admin.entity.model.Checks;
import com.cetcxl.xlpay.admin.entity.model.ChecksRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

import static com.cetcxl.xlpay.common.constants.PatternConstants.DATE_TIME;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-07-07
 */
public interface ChecksRecordMapper extends BaseMapper<ChecksRecord> {
    @Data
    @NoArgsConstructor
    @ApiModel
    class CheckRecordDTO {
        private Integer checkBatch;
        private String phone;
        private Checks.Status action;
        private Integer operator;
        private String info;
        @JsonFormat(pattern = DATE_TIME)
        private LocalDateTime created;
    }

    @Select("SELECT\n" +
            "	cr.*,\n" +
            "	cu.phone \n" +
            "FROM\n" +
            "	checks_record cr,\n" +
            "	company_user cu \n" +
            "WHERE\n" +
            "	cr.check_batch = #{value} \n" +
            "	AND cr.operator = cu.id")
    List<CheckRecordDTO> listCheckRecordDTO(Integer checkBatch);
}
