package com.cetcxl.xlpay.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyUser;
import com.cetcxl.xlpay.common.entity.model.Store;
import com.cetcxl.xlpay.common.entity.model.StoreUser;
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
    private CompanyUserService companyUserService;
    @Autowired
    private CompanyService companyService;

    @Autowired
    private StoreUserService storeUserService;
    @Autowired
    private StoreService storeService;

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        if (StringUtils.isBlank(loginName)) {
            throw new UsernameNotFoundException(loginName);
        }

        String[] strings = StringUtils.split(loginName, ".");
        String phone = strings[0];
        boolean isCompany = "c".equals(strings[1]);
        boolean isStore = "s".equals(strings[1]);

        if (isCompany) {
            return loginForCompany(phone);
        }

        if (isStore) {
            return loginForStore(phone);
        }

        throw new UsernameNotFoundException("无法确定登录类型");
    }

    private UserInfo loginForCompany(String phone) {
        CompanyUser companyUser = companyUserService.getOne(
                Wrappers.lambdaQuery(CompanyUser.class)
                        .eq(CompanyUser::getPhone, phone)
                        .eq(CompanyUser::getStatus, CompanyUser.CompanyUserStatus.ACTIVE)
        );

        if (Objects.isNull(companyUser)) {
            throw new UsernameNotFoundException("该企业用户不存在");
        }

        Company company = companyService.getById(companyUser.getCompany());
        if (Objects.isNull(company)) {
            throw new UsernameNotFoundException("该企业不存在");
        }

        return new UserInfo(
                companyUser.getPhone(),
                companyUser.getPassword(),
                AuthorityUtils.createAuthorityList("All"),
                company
        );
    }

    private UserInfo loginForStore(String phone) {
        StoreUser storeUser = storeUserService.getOne(
                Wrappers.lambdaQuery(StoreUser.class)
                        .eq(StoreUser::getPhone, phone)
                        .eq(StoreUser::getStatus, StoreUser.StoreUserStatus.ACTIVE)
        );
        Store store = storeService.getById(storeUser.getStore());

        return new UserInfo(
                storeUser.getPhone(),
                storeUser.getPassword(),
                AuthorityUtils.createAuthorityList("All"),
                store
        );
    }

    @Getter
    @Setter
    @ToString
    public static class UserInfo extends User {
        private Integer companyId;
        private Integer storeId;

        public UserInfo(String username,
                        String password,
                        Collection<? extends GrantedAuthority> authorities,
                        Company company) {
            super(username, password, authorities);
            this.companyId = company.getId();
        }

        public UserInfo(String username,
                        String password,
                        Collection<? extends GrantedAuthority> authorities,
                        Store store) {
            super(username, password, authorities);
            this.storeId = store.getId();
        }
    }
}
