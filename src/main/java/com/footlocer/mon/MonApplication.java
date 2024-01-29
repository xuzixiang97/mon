package com.footlocer.mon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.footlocer.mon.mapper")
public class MonApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonApplication.class, args);
    }

}
