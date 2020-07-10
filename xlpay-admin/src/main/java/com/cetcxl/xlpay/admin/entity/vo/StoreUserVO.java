package com.cetcxl.xlpay.admin.entity.vo;

import com.cetcxl.xlpay.common.entity.model.Store;
import com.cetcxl.xlpay.admin.entity.model.StoreUser;
import com.cetcxl.xlpay.common.entity.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel
public class StoreUserVO extends BaseVO {
    private Integer id;
    private String phone;
    private String storeName;

    private LocalDateTime created;

    public static StoreUserVO of(StoreUser storeUser, Store store) {
        StoreUserVO storeUserVO = of(storeUser, StoreUserVO.class);
        storeUserVO.setStoreName(store.getName());
        return storeUserVO;
    }
}
