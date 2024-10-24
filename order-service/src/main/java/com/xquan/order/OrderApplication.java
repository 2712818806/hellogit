package com.xquan.order;

import com.xquan.api.client.CartClient;
import com.xquan.api.client.ItemClient;
import com.xquan.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {ItemClient.class, CartClient.class}, defaultConfiguration = DefaultFeignConfig.class)
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }
}