package com.felixalacampagne.account.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

   List<Transaction> findByAccountIdOrderBySequenceAsc(long accountId); // sequence (normal) balance

   // transaction previous to the given transaction (for balance calculation post transaction delete
   Optional<Transaction> findFirstByAccountIdAndSequenceIsLessThanOrderBySequenceAsc(long accountId, long sequence);

   List<Transaction> findByAccountIdAndCheckedIsTrueOrderByDateAscSequenceAsc(long accountId); // checked balance calc
   List<Transaction> findByAccountIdOrderByDateAscSequenceAsc(long accountId); // sorted balance

   List<Transaction> findByAccountIdAndCheckedIsTrueOrderByDateDescSequenceDesc(long accountId, Pageable pageable);
   Optional<Transaction> findFirstByAccountIdAndCheckedIsTrueAndCheckedBalanceIsNotNullOrderByDateDescSequenceDesc(long accountId);
   
   
   long deleteByAccountId(Long accId);
}
