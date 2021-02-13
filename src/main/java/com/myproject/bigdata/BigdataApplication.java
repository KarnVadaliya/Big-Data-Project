package com.myproject.bigdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.Filter;

@SpringBootApplication
@RestController
public class BigdataApplication {

    public static void main(String[] args) {
        SpringApplication.run(BigdataApplication.class, args);
    }

//    @Bean
//    public Filter filter(){
//        ShallowEtagHeaderFilter filter=new ShallowEtagHeaderFilter();
//        return filter;
//    }


}
