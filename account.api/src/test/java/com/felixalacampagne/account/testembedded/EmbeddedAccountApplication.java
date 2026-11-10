package com.felixalacampagne.account.testembedded;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.felixalacampagne.account.application.AccountApplicationConfig;


@PropertySource(value = "classpath:accountmysql-local-test.properties", ignoreResourceNotFound = false)
@SpringBootApplication(scanBasePackages = {
      "com.felixalacampagne.account.controller"
      })
@Import(AccountApplicationConfig.class)
public class EmbeddedAccountApplication extends SpringBootServletInitializer {

   public static void main(String[] args) {
      SpringApplication.run(EmbeddedAccountApplication.class, args);
   }
}