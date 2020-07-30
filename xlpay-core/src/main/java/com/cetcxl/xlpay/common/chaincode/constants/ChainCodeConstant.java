package com.cetcxl.xlpay.common.chaincode.constants;

import com.cetcxl.xlpay.common.chaincode.util.SM2AlgorithmUtil;
import com.zxl.sdk.algorithm.KeyPair;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName ClientCodeConstant
 * @Description 区块链平台常量
 * @Author liuanyicun
 * @Date 2019/3/7 13:36
 * @Version 1.0
 **/
@Slf4j
public class ChainCodeConstant {
    public static String pk;
    public static String sk;

    static {
        try {
            KeyPair keyPair = SM2AlgorithmUtil.generateKeyPair();
            pk = keyPair.getPublicKey();
            sk = keyPair.getPrivateKey();
        } catch (Exception e) {
            log.error("ChainCodeService initial error {}", e);
            System.exit(1);
        }
    }

    /**
     * 区块高度
     */
    public static final String BLOCK_HEIGHT = "height";

    /**
     * 交易ID
     */
    public static final String TRANSACTION_ID = "transactionId";

    /**
     * luna平台
     */
    public static final String APPLICATION_READADDRESS_LUNA = "luna";

    /**
     * invoke
     */
    public static final String FABRIC_INVOKE = "/fabric/invoke";

    /**
     * query
     */
    public static final String FABRIC_QUERY = "/fabric/query";

    /**
     * JAVA SDK平台
     */
    public static final String APPLICATION_READADDRESS_CHAIN = "chaincode";

    /**
     * 保存交易记录函数名
     */
    public static final String SAVE_DEALING_RECORD_FUNC = "saveDealingRecord";

    /**
     * 保存结算记录函数名
     */
    public static final String SAVE_CHECK_SLIP_FUNC = "saveCheckSlipInfo";

    /**
     * 查询交易单信息函数名
     */
    public static final String QUERY_DEALING_RECORD_FUNC = "queryDealingRecord";

    /**
     * 查询个人钱包信息函数名
     */
    public static final String QUERY_PERSONAL_WALLET_FUNC = "queryPersonalWallet";

    /**
     * 查询商家钱包信息函数名
     */
    public static final String QUERY_BUSINESS_WALLET_FUNC = "queryBusinessWallet";

    /**
     * 查询结算单信息函数名
     */
    public static final String QUERY_CHECK_SLIP_FUNC = "queryCheckSlip";

}
