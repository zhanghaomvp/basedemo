package com.cetcxl.xlpay.admin.config;

import com.cetcxl.xlpay.admin.entity.vo.CompanyVO;
import com.cetcxl.xlpay.admin.entity.vo.StoreVO;
import com.cetcxl.xlpay.admin.service.UserDetailServiceImpl;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.*;

@Slf4j
@EnableWebSecurity
@Configuration
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    SessionRegistry sessionRegistry;

    @Autowired
    private ObjectMapper mapper;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/js/**", "/css/**", "/images/**")
                .antMatchers("/swagger-ui.html")
                .antMatchers("/webjars/**")
                .antMatchers("/v2/**")
                .antMatchers("/swagger-resources/**")
                .and();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/companys/register", "/companys/company-user/password").permitAll()
                .antMatchers("/stores/register", "/stores/store-user/password").permitAll()
                .antMatchers("/util/**").permitAll()
                .antMatchers("/synchronize/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .successHandler(
                        (req, res, auth) -> {

                            UserDetailServiceImpl.UserInfo userInfo = (UserDetailServiceImpl.UserInfo) auth.getPrincipal();

                            if (Objects.nonNull(userInfo.getCompany())) {
                                resolveResponse(res, ResBody.success(CompanyVO.of(userInfo.getCompany(), CompanyVO.class)));
                            }

                            if (Objects.nonNull(userInfo.getStore())) {
                                resolveResponse(res, ResBody.success(StoreVO.of(userInfo.getStore(), StoreVO.class)));
                            }
                        }
                )
                .failureHandler(
                        (req, res, e) -> {
                            log.error("formLogin failureHandler error : {} ", e);
                            resolveResponse(res, ResBody.error(LOGIN_FAIL));
                        }
                )
                .permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true).clearAuthentication(true)
                .logoutSuccessHandler(
                        (req, res, auth) -> {
                            resolveResponse(res, ResBody.success());
                        }
                )
                .permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(
                        (req, res, e) -> {
                            log.error("authenticationEntryPoint error : {} ", e);
                            resolveResponse(res, ResBody.error(AUTHENTICATION_ERROR));
                        }
                )
                .and()
                .sessionManagement()
                .maximumSessions(1)
                .sessionRegistry(sessionRegistry)
                .expiredSessionStrategy(
                        event -> {
                            log.error("sessionAuthenticationFailureHandler error : {} ", event.getSessionInformation());
                            resolveResponse(event.getResponse(), ResBody.error(SESSION_INVALID));
                        }
                )
                .and()
                .and()
        ;
    }

    private void resolveResponse(HttpServletResponse res, Object o) throws IOException {
        res.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        PrintWriter out = res.getWriter();
        out.write(mapper.writeValueAsString(o));
        out.flush();
        out.close();
    }
}
