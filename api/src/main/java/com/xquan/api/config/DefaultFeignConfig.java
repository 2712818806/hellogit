
package com.xquan.api.config;

import com.xquan.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return requestTemplate -> {
            Long userId = UserContext.getUser();
            if (userId != null){
                requestTemplate.header("user-info", userId.toString());
            }

        };
    }
}