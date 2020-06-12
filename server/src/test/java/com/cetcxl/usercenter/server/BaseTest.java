package com.cetcxl.usercenter.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = UCServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseTest {
    @BeforeAll
    public static void setup() {

    }

    @AfterAll
    public static void after() {

    }
}