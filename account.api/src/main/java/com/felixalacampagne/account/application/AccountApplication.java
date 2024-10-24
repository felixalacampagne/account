package com.felixalacampagne.account.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;


// NB https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html#using.structuring-your-code.locating-the-main-class
// The Application should go in the root - which is where I wanted it to go but it clashed with the TestApplication.
// From the root Spring will detect Controller in the sub-packages but because the Application
// is in a sub-package nothing is found. No error is given until attempting to access the controllers URL
// and then a white error label page is returned, ie. 404. Obviously it takes forking ages to figure out
// why nothing is being loaded. Luckily I'm not the only one to fall into this trap and I eventually found
// the right way to ask Google for the answer... which is to put the controller package in scanBasePackages.
// Also has to explicitly import AccountApplicationConfig, which is strange given that it is in the
// same package as Application - that's SPriong for you, constantly finding way to fork you over and
// suck up available time on idiocies.
// 
// So now need to figure out how to override AccountApplication with TestApplication for the tests
@SpringBootApplication(scanBasePackages = {
"com.felixalacampagne.account.controller"
//"com.scu.account.persistence.entities"
})
@Import(AccountApplicationConfig.class)
public class AccountApplication extends SpringBootServletInitializer {

   public static void main(String[] args) {
      SpringApplication.run(AccountApplication.class, args);
   }

}
