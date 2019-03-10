package com.stasdev.backend.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.HashSet;


/**
 * В данном классе мы прописываем конфигурацию для доступа к непосредственно ресурсам
 * все роли и прочее
 * */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    private static final String RESOURCE_ID = "resource-id";

    private final TokenStore tokenStore;

    @Autowired
    public ResourceServerConfig(final TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public void configure(final ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(true).tokenStore(tokenStore);
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http.headers().frameOptions().sameOrigin(); // Это нужно что бы открыть h2 консоль
        http
                .csrf().disable()
                .anonymous().disable()
                .httpBasic().disable()
                .authorizeRequests()
                .antMatchers("/users/all").hasRole("ADMIN")
                .antMatchers("/users/create").hasRole("ADMIN")
                .antMatchers("/users/delete/**").hasRole("ADMIN")
                .antMatchers("/accounts/all").hasRole("ADMIN")
                .antMatchers("/accounts/create").hasRole("ADMIN")
                .antMatchers("/accounts/my").hasAnyRole("ADMIN", "USER")
                .antMatchers("/accounts/transaction").hasAnyRole("ADMIN", "USER")
//                    .anyRequest().authenticated() //все что не указано явным образом - требует авторизации - почему то закрывает ресурсы с сайтом тоже
                .and()
                .exceptionHandling()
                .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }

}
