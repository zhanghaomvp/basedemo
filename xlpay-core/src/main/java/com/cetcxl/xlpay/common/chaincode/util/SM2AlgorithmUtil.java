package com.cetcxl.xlpay.common.chaincode.util;

import com.zxl.bc.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import com.zxl.bc.jcajce.provider.asymmetric.ec.BCECPublicKey;
import com.zxl.bc.jce.ECNamedCurveTable;
import com.zxl.bc.jce.provider.BouncyCastleProvider;
import com.zxl.bc.jce.spec.ECParameterSpec;
import com.zxl.bc.jce.spec.ECPrivateKeySpec;
import com.zxl.bc.jce.spec.ECPublicKeySpec;
import com.zxl.bc.math.ec.ECCurve;
import com.zxl.bc.util.encoders.Hex;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * @author:
 * @date: 2019/7/11
 **/
public class SM2AlgorithmUtil {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成公私钥对
     *
     * @return
     * @throws Exception
     */
    public static com.zxl.sdk.algorithm.KeyPair generateKeyPair() throws Exception {
        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("sm2p256v1");
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        g.initialize(ecGenSpec, new SecureRandom());
        KeyPair k = g.generateKeyPair();
        BCECPrivateKey ecSk = (BCECPrivateKey) k.getPrivate();
        String sk = ecSk.getS().toString(16);
        for (int i = sk.length(); i < 64; i++) {
            sk = "0" + sk;
        }
        String pk = "04";
        BCECPublicKey ecPk = (BCECPublicKey) k.getPublic();
        String x = ecPk.getQ().getAffineXCoord().toBigInteger().toString(16);
        String y = ecPk.getQ().getAffineYCoord().toBigInteger().toString(16);
        for (int i = x.length(); i < 64; i++) {
            pk = pk + "0";
        }
        pk = pk + x;
        for (int i = y.length(); i < 64; i++) {
            pk = pk + "0";
        }
        pk = pk + y;
        com.zxl.sdk.algorithm.KeyPair pairKey = new com.zxl.sdk.algorithm.KeyPair();
        pairKey.setPublicKey(pk);
        pairKey.setPrivateKey(sk);
        return pairKey;
    }


    /**
     * 签名
     *
     * @param sk   私钥
     * @param data 签名字符串
     * @return
     * @throws Exception
     */
    public static String sign(String sk, String data) throws Exception {
        if (sk.length() != 64) {
            throw new RuntimeException("private key length is not equal to 64");
        }
        Signature s = Signature.getInstance("SM3WITHSM2", BouncyCastleProvider.PROVIDER_NAME);
        PrivateKey privateKey = loadPrivateKey(sk);
        s.initSign(privateKey);
        s.update(data.getBytes());
        byte[] sigBytes = s.sign();
        return bytesToHex(sigBytes);
    }

    /**
     * 验签
     *
     * @param signStr 签名字符串
     * @param pk      公钥
     * @param srcStr  原数据
     * @return
     */
    public static boolean verify(String pk, String signStr, String srcStr) throws Exception {
        Signature s = Signature.getInstance("SM3WITHSM2", BouncyCastleProvider.PROVIDER_NAME);
        PublicKey publicKey = loadPublicKey(pk);
        s.initVerify(publicKey);
        s.update(srcStr.getBytes());
        return s.verify(hexToBytes(signStr));
    }

    /**
     * 获取私钥
     *
     * @param hexString
     * @return
     * @throws Exception
     */
    public static PrivateKey loadPrivateKey(String hexString) throws Exception {
        //00防止第一位数据是负数（表现为大于8）时出错
        byte[] pkBytes = hexToBytes("00" + hexString);
        ECParameterSpec params = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        ECPrivateKeySpec sk = new ECPrivateKeySpec(new BigInteger(pkBytes), params);
        KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return kf.generatePrivate(sk);
    }

    /**
     * 获取公钥
     *
     * @param hexString
     * @return
     * @throws Exception
     */
    public static PublicKey loadPublicKey(String hexString) throws Exception {
        ECParameterSpec params = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        ECCurve curve = params.getCurve();
        ECPublicKeySpec pk = new ECPublicKeySpec(curve.decodePoint(Hex.decode(hexString)), params);
        KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return kf.generatePublic(pk);
    }

    /**
     * byte转string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 字符串转byte数组
     *
     * @param hexString
     * @return
     */
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null || "".equals(hexString)) {
            return null;
        }
        hexString = hexString.toLowerCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * char转byte
     *
     * @param c
     * @return
     */
    public static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }

}
