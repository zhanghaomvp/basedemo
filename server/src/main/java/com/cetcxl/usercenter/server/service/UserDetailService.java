package com.cetcxl.usercenter.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.usercenter.server.entity.model.Company;
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

@Component
public class UserDetailService implements UserDetailsService {
    @Autowired
    private CompanyService companyService;

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        if (StringUtils.isBlank(loginName)) {
            throw new UsernameNotFoundException(loginName);
        }

        Company one = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getLoginName, loginName)
                        .ne(Company::getStatus, Company.CompanyStatusEnum.DISABLE)
        );

        return new UserInfo(
                one.getName(),
                one.getPassword(),
                AuthorityUtils.createAuthorityList("All"),
                one
        );
    }

    @Getter
    @Setter
    @ToString
    public static class UserInfo extends User {
        private Company company;

        public UserInfo(String username,
                        String password,
                        Collection<? extends GrantedAuthority> authorities,
                        Company company) {
            super(username, password, authorities);
            this.company = company;
        }
    }
}
