package com.cetcxl.xlpay.payuser.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

@Component
public class UserDetailService implements UserDetailsService {
    @Autowired
    private PayUserService payUserService;

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        if (StringUtils.isBlank(loginName)) {
            throw new UsernameNotFoundException(loginName);
        }

        PayUser payUser = payUserService
                .getOne(
                        Wrappers.lambdaQuery(PayUser.class)
                                .eq(PayUser::getIcNo, loginName)
                );

        if (Objects.isNull(payUser)) {
            throw new UsernameNotFoundException("用户不存在");
        }

        return new PayUserInfo(
                payUser.getIcNo(),
                payUser.getPassword(),
                AuthorityUtils.createAuthorityList("All"),
                payUser
        );
    }

    @Getter
    @Setter
    @ToString
    public static class PayUserInfo extends User {
        private PayUser payUser;

        public PayUserInfo(String username,
                           String password,
                           Collection<? extends GrantedAuthority> authorities,
                           PayUser payUser) {
            super(username, password, authorities);
            this.payUser = payUser;
        }
    }
}
