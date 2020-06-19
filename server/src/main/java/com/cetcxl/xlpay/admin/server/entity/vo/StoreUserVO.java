package com.cetcxl.xlpay.admin.server.entity.vo;

import com.cetcxl.xlpay.admin.server.entity.model.Store;
import com.cetcxl.xlpay.admin.server.entity.model.StoreUser;
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
