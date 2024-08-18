package com.felixalacampagne.account.application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.felixalacampagne.account.persistence.repository.RepositoryConfig;
import com.felixalacampagne.account.service.ServiceConfig;

@Configuration
@Import({RepositoryConfig.class, ServiceConfig.class})

public class AccountApplicationConfig
{

}
