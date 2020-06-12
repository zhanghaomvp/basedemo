package com.cetcxl.usercenter.server.common.service;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
public class SmsService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SmsBaoConfigrution smsBaoConfigrution;

    public boolean sendSmsBySmsBao(String phone, String content) {
        Map<String, String> map = Maps.newHashMap();
        map.put("u", smsBaoConfigrution.getName());
        map.put(
                "p", DigestUtils.md5DigestAsHex(
                        smsBaoConfigrution.getPassword().getBytes(StandardCharsets.UTF_8)
                )
        );
        map.put("m", phone);
        map.put("c", smsBaoConfigrution.getSign() + content);

        ResponseEntity<Integer> entity = restTemplate.getForEntity(smsBaoConfigrution.getSign() + "?u={u}&p={p}&m={m}&c={c}", Integer.class, map);
        if (!HttpStatus.OK.equals(entity.getStatusCode())) {
            return false;
        }

        if (entity.getBody() != 0) {
            return false;
        }

        return true;
    }

    @Component
    @ConfigurationProperties(prefix = "sms.sms-bao")
    @Data
    public static class SmsBaoConfigrution {
        private String name;
        private String password;
        private String url;
        private String sign;
    }
}
