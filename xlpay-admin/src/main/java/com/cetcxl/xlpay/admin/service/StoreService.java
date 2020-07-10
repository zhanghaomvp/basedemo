package com.cetcxl.xlpay.admin.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.dao.StoreMapper;
import com.cetcxl.xlpay.common.entity.model.Store;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Service
public class StoreService extends ServiceImpl<StoreMapper, Store> {
    private static final String PREFIX_QR_CODE = "xlpay.store";

    public String getQrCodeContent(Integer storeId) {
        return Base64.getEncoder().encodeToString(
                (PREFIX_QR_CODE + storeId).getBytes()
        );
    }
}
