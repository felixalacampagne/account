package com.felixalacampagne.account.service;

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.persistence.entities.Prefs;
import com.felixalacampagne.account.persistence.repository.PrefsAndHouseKeepingRepository;

@Service
public class HouseKeepingService
{
private Logger log = LoggerFactory.getLogger(this.getClass());

private final PrefsAndHouseKeepingRepository prefsAndHouseKeepingRepository;

@Value("${falc.account.db.backupdir}") private String defaultbackuplocation;
@Value("${falc.account.db.name}")     private String dbname;
@Value("${falc.account.db.cron}")     private String cronstr;

   @Autowired
   public HouseKeepingService(PrefsAndHouseKeepingRepository prefsAndHouseKeepingRepository)
   {
      this.prefsAndHouseKeepingRepository = prefsAndHouseKeepingRepository;
   }


   // Maybe not the PC place to put this but seems pointless to create yet another almost empty class
   @Scheduled(cron = "${falc.account.db.cron}")
   public void housekeepingCron()
   {
      log.info("housekeepingCron: housekeeping: cronstr:{}", cronstr);
      doHouseKeeping();
      log.info("housekeepingCron: housekeeping: finished");
   }

   public void doHouseKeeping()
   {
//      String backdir = this.defaultbackuplocation;
//      File path = new File(backdir, this.dbname + "_backup_00.zip");
//      Utils.rotateFiles(path.getAbsolutePath(), 14);
//      log.info("doHouseKeeping: Backup database: {}", path.getAbsolutePath());
//      this.prefsAndHouseKeepingRepository.backupDB(path.getAbsolutePath());
   }

}
