package com.felixalacampagne.account;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.felixalacampagne.account.persistence.repository.RepositoryConfig;
import com.felixalacampagne.account.service.ServiceConfig;
import com.felixalacampagne.account.standingorder.StandingOrderConfig;

@DataJpaTest

// It appears that TestPropertySource REPLACES application.properties, ie. is NOT loaded.
// At least when TestPropertySource only contains the ucanaccess jdbc url weird errors about
// Driver org.hsqldb.jdbc.JDBCDriver claims to not accept jdbcUrl, jdbc:ucanaccess://C:/Development/accountDB/acc2003.mdb
// which sounds like the line application.properties
// spring.datasource.driver-class-name=net.ucanaccess.jdbc.UcanaccessDriver
// has been ignored. Of course it is impossible to confirm this behaviour from the Spring documentation.
// Hmm, after changing to use ActiveProfiles the test still produced the weird error. The test was only
// successful after performing a clean and rebuild.
// So @TestPropertySource was restored and a clean/rebuild performed and the test worked. So now not really
// sure WTF is supposed to happen.
@TestPropertySource(locations = "classpath:application-test.properties")
//@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RepositoryConfig.class, ServiceConfig.class, StandingOrderConfig.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) 
public @interface AccountTest
{
 
}
