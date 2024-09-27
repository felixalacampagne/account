package com.felixalacampagne.account.standingorder;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = {"com.felixalacampagne.account.standingorder"})
@EnableScheduling
@EnableTransactionManagement
public class StandingOrderConfig
{

}
