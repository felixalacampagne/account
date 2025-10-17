package com.felixalacampagne.account.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.persistence.repository.PrefsAndHouseKeepingRepository;
import com.smattme.MysqlExportService;

@Service
public class HouseKeepingService
{
private Logger log = LoggerFactory.getLogger(this.getClass());

private final PrefsAndHouseKeepingRepository prefsAndHouseKeepingRepository;

@Value("${falc.account.db.backupdir}") private String defaultbackuplocation;
@Value("${falc.account.db.name}")     private String dbname;
@Value("${falc.account.db.cron}")     private String cronstr;
@Value("${spring.datasource.username}") private String dbuser;
@Value("${spring.datasource.password}") private String dbpwd; // Probably not PC to keep in memory
@Value("${spring.datasource.url}") private String dburl;


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
      String backdir = this.defaultbackuplocation;
      File backup00 = new File(backdir, this.dbname + "_backup_00.zip");

      log.info("doHouseKeeping: Backup database: {}", backup00.getAbsolutePath());
      Properties properties = new Properties();
      properties.setProperty(MysqlExportService.DB_NAME, dbname);
      properties.setProperty(MysqlExportService.DB_USERNAME, dbuser);
      properties.setProperty(MysqlExportService.DB_PASSWORD, dbpwd);
      properties.setProperty(MysqlExportService.JDBC_CONNECTION_STRING, dburl);

      File tmpdir = new File(defaultbackuplocation + "/tmp");
      //set the outputs temp dir - this is on the application system, not the database systemr
      properties.setProperty(MysqlExportService.TEMP_DIR, tmpdir.getPath());
      properties.setProperty(MysqlExportService.PRESERVE_GENERATED_ZIP, "true");

      MysqlExportService mysqlExportService = new MysqlExportService(properties);
      try
      {
         mysqlExportService.export();
         File file = mysqlExportService.getGeneratedZipFile();
         Utils.rotateFiles(backup00.getAbsolutePath(), 14);
         // Move/rename the file to the standard location/name
         Files.move( file.toPath(), backup00.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
      catch (Exception e)
      {
         log.error("doHouseKeeping: backup failed:", e);
      }

      // clear the temp files
      mysqlExportService.clearTempFiles();
   }

}