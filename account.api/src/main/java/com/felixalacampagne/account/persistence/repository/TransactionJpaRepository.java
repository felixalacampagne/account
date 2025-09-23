package com.felixalacampagne.account.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.felixalacampagne.account.persistence.entities.Transaction;
@Repository
public interface TransactionJpaRepository extends JpaRepository<Transaction, Long>
{

   // Woooaaaahhhh! The forking names are case-sensitive, ie column names must match the field names in
   // the entity, ie. 'accountId' and not the column name 'AccountId'. Similarly the table name
   // must case-sensitively match the class name!

   // So, after another couple of wasted hours, I once again discover that Spring seems to be intended as an
   // enormous joke where nothing actually forking works. To get my last n records in the correct order
   // I'll have to do it all manually in the code.
   List<Transaction> findByAccountId(long accountId, Pageable pageable);
   long countByAccountId(long accountId);

   Optional<Transaction> findFirstByAccountIdOrderBySequenceDesc(long accountId); // latest
   Optional<Transaction> findFirstByAccountIdAndSequenceIsLessThanOrderByDateAscSequenceAsc(long accountId, long sequence);

   List<Transaction> findByAccountIdOrderBySequenceAsc(long accountId); // sequence (normal) balance

   // transaction previous to the given transaction (for balance calculation post transaction delete
   Optional<Transaction> findFirstByAccountIdAndSequenceIsLessThanOrderBySequenceAsc(long accountId, long sequence);

   List<Transaction> findByAccountIdAndCheckedIsTrueOrderByDateAscSequenceAsc(long accountId); // checked balance calc
   List<Transaction> findByAccountIdOrderByDateAscSequenceAsc(long accountId); // sorted balance

   List<Transaction> findByAccountIdAndCheckedIsTrueOrderByDateDescSequenceDesc(long accountId, Pageable pageable);
   Optional<Transaction> findFirstByAccountIdAndCheckedIsTrueAndCheckedBalanceIsNotNullOrderByDateDescSequenceDesc(long accountId);
   
   
   long deleteByAccountId(Long accId);
   
   // Queries to determine the previous checked balance when the transaction itself has been deleted/removed
   // Not used in the end as checking whether the balance is changed should be enough to avoid length multi-record
   // updates which take time. 
   // sort date desc, seq desc
   // date <= st_date
   // id< st_id
   // checkbal != null
   // first entry is the start of the list: fe
   //@Query("SELECT t FROM Transaction  t "
   //      + "where t.accountId = :accid " 
   //      + "and t.date <= :dt "
   //      + "and t.sequence <= :seq "
   //      + "and t.checkedBalance is not null "
   //      + "order by t.date DESC, t.sequence DESC" )   
   //List<Transaction> findPrevCheckedBal(@Param("accid") long accountId, @Param("seq") long sequence, @Param("dt") LocalDate date);
   // recalc list
   // sort date asc, seq asc
   // date >= fe_date
   // id >= fe_id   
   // List<Transaction> findCheckedFromTransaction(long accountId, long sequence, LocalDate date);
   
   
}
