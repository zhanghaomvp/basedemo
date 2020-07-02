package com.cetcxl.xlpay;

import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.admin.service.UserDetailServiceImpl;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@SpringBootTest(classes = XlpayAdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BaseTest {
    public static final String S_TEST = "test";
    public static final String S_TEMP = "temp";
    public static final String S_VERIFY_CODE = "123456";
    public static final String S_CETCXL = "cetcxl";
    public static final String S_SHOP = "shop";
    public static final String S_PHONE = "19999999999";

    public static WireMockServer wireMockServer;

    @Autowired
    private WebApplicationContext context;

    public MockMvc mockMvc;

    @BeforeAll
    public static void setup() throws IOException {
        wireMockServer = new WireMockServer(
                options()
                        .port(8089)
        );
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
    }

    @AfterAll
    public static void after() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @AfterEach
    public void tearDown() {
    }

    public void setAuthentication(Company company) {
        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                new UserDetailServiceImpl.UserInfo(
                                        S_TEMP,
                                        S_TEMP,
                                        AuthorityUtils.createAuthorityList("All"),
                                        company
                                ),
                                null,
                                AuthorityUtils.createAuthorityList("All")));
    }
}