package com.felixalacampagne.account.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.felixalacampagne.account.model.Version;
import com.felixalacampagne.account.persistence.repository.RepositoryConfig;
import com.felixalacampagne.account.service.ServiceConfig;
import com.felixalacampagne.account.standingorder.StandingOrderConfig;

@Configuration
@Import({RepositoryConfig.class, ServiceConfig.class, StandingOrderConfig.class})

public class AccountApplicationConfig
{

@Bean
public Version version()
{
   return new Version();
}
}
