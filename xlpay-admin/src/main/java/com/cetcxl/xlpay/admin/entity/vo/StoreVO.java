package com.cetcxl.xlpay.admin.entity.vo;

import com.cetcxl.xlpay.common.entity.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class StoreVO extends BaseVO {
    private Integer id;
    private String name;
}
