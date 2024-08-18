package com.felixalacampagne.account.persistence.repository;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"com.felixalacampagne.account.persistence.repository"})
@EntityScan(basePackages = {"com.felixalacampagne.account.persistence"})
public class RepositoryConfig
{

}
