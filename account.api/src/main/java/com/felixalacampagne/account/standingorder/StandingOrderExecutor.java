package com.felixalacampagne.account.standingorder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.service.StandingOrderService;

@Component
public class StandingOrderExecutor
{
   Logger log = LoggerFactory.getLogger(this.getClass());

   final StandingOrderService standingOrderService;

   @Autowired
   public StandingOrderExecutor (StandingOrderService standingOrderService)
   {
      this.standingOrderService = standingOrderService;
   }


   // see https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html#parse(java.lang.String)
   //   The fields read from left to right are interpreted as follows.
   //
   //   second
   //   minute
   //   hour
   //   day of month
   //   month
   //   day of week
   // "0 10 08 * * ?"  08:10 every day??
   // "*/10 * * * * *" every 10 seconds
   //@Scheduled(cron = "0 10 08 * * ?")
   @Scheduled(cron = "*/10 * * * * *")
   public void standingOrderDailyTask()
   {
      log.info("standingOrderDailyTask: processing standing orders");
      List<StandingOrders> sopend = standingOrderService.getPendingStandingOrders();
      log.info("standingOrderDailyTask: Sorder count: {}", sopend.size());
   }

}
