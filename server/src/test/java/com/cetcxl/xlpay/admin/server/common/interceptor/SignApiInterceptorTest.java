package com.cetcxl.xlpay.admin.server.common.interceptor;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Base64;

class SignApiInterceptorTest {

    @Test
    @Disabled
    void key() throws Exception {
        KeyPair keyPair = SecureUtil.generateKeyPair("RSA");
        System.out.println(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    }

    @Test
    @Disabled
    void sign_veryfy_test() throws Exception {
        String value = "kobe";
        Sign sign = SecureUtil.sign(
                SignAlgorithm.SHA1withRSA,
                "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAK2i1AweKSaexWi2iRW+fJky0joDeWALWufNb5XpJac1QxDUxl7xHO6g2EQ3irvmM3borZLRALkuB09tMxmPfpzeztjH8OwvUh7CXOOzNR9wXt+tH5ft0O0A/56XcMd8Y4dwNChXhdwc8fuNWevBvX9Dqw7oEK7BoQrGf3EgsJwFAgMBAAECgYALzohNW/j/skjJdQK0os5FezTbqFl8KWmAskcbJ3kIT1EvIiNsWJ2CBcKO6O6bFkJ7nG4TufiASbFKOlUA25wH9qFoumxFDirmw6Cs77FpOJ5guG28kbW0gO2ZwIr5q/+C+GVWowvqIfUIG+UvnKFiDAWRQ5UNw+EnW68XvT8OhQJBAOENuf+FM/6CFT8OVD4yBCYfdf4f36NMzjNokKhHKqEhuMtn3alIwp9qmQsk0mFkHc+dkRuH9PDkbGAdA+khj6cCQQDFgx1zvribIQ8/DfVTmMJchP/IZvhgI3HuCo8fbuFy7IodlwSX11oRQJGeanAI/iLaFJRFYgZ9rI70yYS7PMxzAkEArRVkfisQwOWEt5kqmybWYAeENKyIz8vLLmh2EKWjGIeZ2v4H0SD/ZaGTEKoCDxrzfnA9YIIglH/pBcZq8op4MwJBAIH9+ltcUemfh4ZLbIQ5jOoRirrdsmiry2cMwgfBFVZrAbfZ1ecNkDS8l1p42QXCJTP8yV0k1/rMoEXRf68vo6sCQCLNINa4EzIa8FFZ3qccUPeZK3Atbpli7BXOR9G/rFqVF90uRnCwwKmiyXj6OufjaKk3ddN0wTIWbsjJrORias4=",
                null
        );

        byte[] bytes = sign.sign(value.getBytes(StandardCharsets.UTF_8));
        String signValue = Base64.getEncoder().encodeToString(bytes);

        Sign verify = SecureUtil.sign(
                SignAlgorithm.SHA1withRSA,
                null,
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCtotQMHikmnsVotokVvnyZMtI6A3lgC1rnzW+V6SWnNUMQ1MZe8RzuoNhEN4q75jN26K2S0QC5LgdPbTMZj36c3s7Yx/DsL1IewlzjszUfcF7frR+X7dDtAP+el3DHfGOHcDQoV4XcHPH7jVnrwb1/Q6sO6BCuwaEKxn9xILCcBQIDAQAB"
        );

        boolean flag = verify.verify(
                "kobe".getBytes(StandardCharsets.UTF_8),
                Base64.getDecoder().decode(signValue)
        );

        Assertions.assertTrue(flag);
    }
}