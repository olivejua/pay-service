package com.olivejua.payservice.config;

import com.olivejua.payservice.filter.TimeoutFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public FilterRegistrationBean<TimeoutFilter> timeoutFilter() {
        FilterRegistrationBean<TimeoutFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TimeoutFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
