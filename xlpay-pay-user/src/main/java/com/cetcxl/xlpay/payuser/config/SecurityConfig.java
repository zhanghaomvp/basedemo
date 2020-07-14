package com.cetcxl.xlpay.payuser.config;

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

import static com.cetcxl.xlpay.common.constants.CommonResultCode.AUTHENTICATION_ERROR;
import static com.cetcxl.xlpay.common.constants.CommonResultCode.SESSION_INVALID;

@EnableWebSecurity
@Configuration
@Order(1)
@Slf4j
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
                .antMatchers("/pay-user").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .successHandler(
                        (req, res, auth) -> {
                            resolveResponse(res, ResBody.success());
                        }
                )
                .failureHandler(
                        (req, res, e) -> resolveResponse(res, ResBody.error(e.getMessage()))
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
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter out = res.getWriter();
        out.write(mapper.writeValueAsString(o));
        out.flush();
        out.close();
    }
}
