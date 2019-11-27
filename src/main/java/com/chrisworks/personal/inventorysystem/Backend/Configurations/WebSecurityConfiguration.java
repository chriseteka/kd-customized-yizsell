package com.chrisworks.personal.inventorysystem.Backend.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
//                .authorizeRequests()
//                .antMatchers(HttpMethod.POST,"/api/inventory/BO/createAccount").permitAll()
//                .antMatchers(HttpMethod.GET,"/api/inventory/BO").permitAll()
//                .antMatchers(HttpMethod.POST, "/login").permitAll()
//                .antMatchers(HttpMethod.POST,"/newuser/*").permitAll()
//                .antMatchers(HttpMethod.GET,"/master/*").permitAll()
//                .antMatchers(HttpMethod.GET,"/exploreCourse").permitAll()
//                .anyRequest().authenticated();
    }
}
