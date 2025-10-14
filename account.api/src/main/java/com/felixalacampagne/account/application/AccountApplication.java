package com.felixalacampagne.account.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;


// NB https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html#using.structuring-your-code.locating-the-main-class
// The Application should go in the root - which is where I wanted it to go but it clashed with the TestApplication.
// From the root Spring will detect Controller in the sub-packages but because the Application
// is in a sub-package nothing is found. No error is given until attempting to access the controllers URL
// and then a white error label page is returned, ie. 404. Obviously it takes forking ages to figure out
// why nothing is being loaded. Luckily I'm not the only one to fall into this trap and I eventually found
// the right way to ask Google for the answer... which is to put the controller package in scanBasePackages.
// Also has to explicitly import AccountApplicationConfig, which is strange given that it is in the
// same package as Application - that's Spring for you, constantly finding ways to fork you over and
// suck up available time on idiocies.

// Want to provide the DB location/credentials in an installation dependent properties file but
// this is far from straight forward to do in tomcat.
// So start with try putting it in the tomcat conf directory and see if it is picked up
// via the 'classpath' prefix.

// In yet another typical bit of Spring Boot Shirt Think the values loaded via PropertySource 
// do NOT override the default values in application.properties.
// Therefore the values in application.properties must be derived from properties with a different name
// which ONLY appear in the propertysource file or a default value so absence of the
// propertysource value results in the desired default value being used. I use the 'local' prefix for all
// properties in the porpertysource file. Obviously the absence of the propertysource file MUST NOT
// cause the application to crash (which is the default behaviour!!).
// Note that 'catalina.home', the JVM property used in the tomcat startup command line, is used as this
// is unlikely to be available unless the application is running in the tomcat container
@PropertySource(value = "file:${catalina.home}/conf/accountmysql-local.properties", ignoreResourceNotFound = true)
@SpringBootApplication(scanBasePackages = {
		"com.felixalacampagne.account.controller"
		})
@Import(AccountApplicationConfig.class)
public class AccountApplication extends SpringBootServletInitializer {

   public static void main(String[] args) {
      SpringApplication.run(AccountApplication.class, args);
   }

// Potential way to determine if running in container or embedded mode.
//   @Override
//   protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//       return application.sources(ExampleApplication.class).properties(
//               "com.example.mode:servlet-container");
//   }
//
//   public static void main(String[] args) throws Exception {
//       new SpringApplicationBuilder(ExampleApplication.class).properties(
//               "com.example.mode:standalone").run(args);
//   }
}
