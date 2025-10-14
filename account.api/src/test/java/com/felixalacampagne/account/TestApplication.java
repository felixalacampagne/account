package com.felixalacampagne.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.felixalacampagne.account.persistence.repository.RepositoryConfig;
import com.felixalacampagne.account.service.ServiceConfig;

/**
 * Dummy SpringBootApplication for Spring to detect the test configurations.
 */
@SpringBootApplication(
//      scanBasePackages = {
//      "com.scu.account.persistence.repository",
//      "com.scu.account.persistence.entities"
//}
      )

public class TestApplication
{
   public static void main(String[] args) {
       SpringApplication.run(TestApplication.class, args);
   }
}

