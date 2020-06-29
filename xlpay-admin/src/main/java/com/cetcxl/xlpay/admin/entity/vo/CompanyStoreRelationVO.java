package com.cetcxl.xlpay.admin.entity.vo;

import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.common.entity.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class CompanyStoreRelationVO extends BaseVO {
    private Integer id;
    private Integer relation;
    private Integer applyReleation;

    private CompanyStoreRelation.RelationStatus status;

    public static CompanyStoreRelationVO of(CompanyStoreRelation relation) {
        CompanyStoreRelationVO vo = of(relation, CompanyStoreRelationVO.class);
        return vo;
    }
}
