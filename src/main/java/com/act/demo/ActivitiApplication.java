package com.act.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(exclude = {
        org.activiti.spring.boot.SecurityAutoConfiguration.class,               
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@MapperScan("com.act.demo.**.dao*")
public class ActivitiApplication {

	public static void main(String[] args) {
        SpringApplication.run(ActivitiApplication.class, args);
    }
}
