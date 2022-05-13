package com.example.yygh.cmn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
public class ServiceCmnApplication {
public static void main(String[] args) {
      SpringApplication.run(ServiceCmnApplication.class, args);
   }
}
