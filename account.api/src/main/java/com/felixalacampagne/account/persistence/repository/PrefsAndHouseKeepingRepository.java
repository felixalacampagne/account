package com.felixalacampagne.account.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.felixalacampagne.account.persistence.entities.Prefs;



// Impossible to have a @Repository without a reference to an Entity
// Thus the Prefs entity acts as a dummy entity which may eventually be used
// to query/update the Prefs table and for miscellaneous actions which
// do not relate to entities or tables, eg. backup.
// I guess the backup location could be added to prefs...

@Repository
public interface PrefsAndHouseKeepingRepository extends JpaRepository<Prefs,Long>
{
   @Modifying
   @Transactional
   @Query(value = "BACKUP TO :path", nativeQuery = true)
   int backupDB(@Param("path") String path);
}
