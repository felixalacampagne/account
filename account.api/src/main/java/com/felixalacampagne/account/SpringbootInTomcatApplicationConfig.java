package com.felixalacampagne.account;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.felixalacampagne.account.persistence.repository.RepositoryConfig;
import com.felixalacampagne.account.service.ServiceConfig;

@Configuration

// Don't think this is required since ServiceConfig is explicitly reference
//@ComponentScan(basePackages = {"com.felixalacampagne.account.service"})
@Import({RepositoryConfig.class, ServiceConfig.class})

public class SpringbootInTomcatApplicationConfig
{

}
