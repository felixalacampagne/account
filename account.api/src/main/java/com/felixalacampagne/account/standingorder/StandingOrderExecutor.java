package com.felixalacampagne.account.standingorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StandingOrderExecutor
{
   Logger log = LoggerFactory.getLogger(this.getClass());

   private final StandingOrderProcessor standingOrderProcessor;

   @Autowired
   public StandingOrderExecutor (StandingOrderProcessor standingOrderProcessor)
   {
      this.standingOrderProcessor = standingOrderProcessor;
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
   @Scheduled(cron = "*/60 * * * * *")
   public void standingOrderDailyTask()
   {
      log.info("standingOrderDailyTask: processing standing orders: start");
      standingOrderProcessor.processStandingOrders();
      log.info("standingOrderDailyTask: processing standing orders: finished");
   }

}
