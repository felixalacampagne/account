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
//

// Want to provide the DB location/credentials in an installation dependent properties file but
// this is far from straight forward to do in tomcat.
// So start with try putting it in the tomcat conf directory and see if it is picked up
// via the 'classpath' prefix.

// This manages to access the account.properties but, in yet another typical bit of Spring Boot Shirt Think
// the values loaded via PropertySource do NOT override the default values in application.properties - once
// again the Spring Boot forkers are having a giant laugh at the expense of poor, time constrained developers.
// This effectively makes the @PropertySource option completely useless unless default values are NOT
// provided in application.properties
// spring.config.additional-location is supposed to allow application.properties to be overridden but
// it is only effective if provided on the command line or in an environment variable - no use for
// providing properties to the app running in tomcat

// So for now any properties that are specific to tomcat must be either hard-coded in the application.properties
// or COMPLETELY OMMITTED from application.properties and MUST appear in account.properties
// Maybe a profile specific to the tomcat installation could be used but not sure where the file would
// need to go unless a config directory is specified on the tomcat command line.
// Something to try:
//    Define an active profile in the account.properties, eg. tomcat
//    Figure out what classpath is and put an application-{profile}.properties containing the real DB settings on the classpath.
//    It should be picked up and override any values in application.properties
// Nope - can't set active profile from PropertySource file.
// So looks like we stuck with the borked PropertySource way and then not being able
// to use default values
@PropertySource(value = "file:${catalina.home}/conf/account.properties", ignoreResourceNotFound = true)


@SpringBootApplication(scanBasePackages = {
"com.felixalacampagne.account.controller"
//"com.scu.account.persistence.entities"
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
