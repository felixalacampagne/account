package com.felixalacampagne.account.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


// The tests don't find AccountService, even when package is included in TestApplication
// scanBasePackages. AccountService IS found by the real application, probably (who can tell
// how the magic works) because of the magic in SpringbootInTomcatApplicationConfig.
// It would make more sense to me have have the magic find AccountService in both tests
// and real app without having to put more magic in two different places...
// So, does having some magic here help?
@Configuration
@ComponentScan(basePackages = {"com.felixalacampagne.account.service"})
public class ServiceConfig
{

}
