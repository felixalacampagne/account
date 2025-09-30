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

@PropertySource(value = "classpath:account.properties", ignoreResourceNotFound = true)

// If that doesn't work then try an environment variable, eg.
//@PropertySource(value = "file:${CATALINA_BASE}/conf/account.properties", ignoreResourceNotFound = true)
// This will require adding the TOMCAT_CONFIG value to the tomcat 'setenv.bat', which might not
// work if the windows service is being used, in which case the service command must be updated with
// something like
// Tomcat9.exe //US//Tomcat9 ++JvmOptions="-Dconfigpath=D:/properties"
// NB Maybe this config difference can be avoided by using CATALINA_BASE which should be defined for
// both service and normal environments
//
// A potentially more generic way is to use a 'JNDI' variable. Don't really know how this
// works but the propertysource might like
//@PropertySource(value = "file:${java:comp/env/springbootconfig}/account.properties", ignoreResourceNotFound = true)
// with something like the following added to the tomcat config (in $CATALINA_BASE/conf/context.xml Context block)
// <Environment name="springbootconfig" value="path_to_app_configs" type="java.lang.String" override="false"/>
// The classpath way is preferable followed by the JNDI way (if it can be made to work).
// Multiple @PropertySource are allowed so why not use them all!
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
