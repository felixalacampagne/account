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

   // Obviously it would be too easy for top to simply work...
   // @Query("SELECT top :count t FROM transaction t where t.AccountId = :accountId order by t.sequence desc")
   // Could maybe do it with a derived query method but inexplicably it is not possible using @Query and
   // the DQM way fixes the number of rows, which I would like to avoid.
   // So must use a Pageable, which might be useful in the future when I want to be able to scroll through the transactions
   //
   // Woooaaaahhhh! The forking names are case-sensitive, ie column names must match the field names in
   // the entity, ie. 'accountId' and not the column name 'AccountId'. Similarly the table name
   // must case-sensitively match the class name!
   // It looked like using PageRequest with Sort might be usable to select the last N transactions
   // using a reversed query and then sort the selected rows to be oldest first. Unfortunately it
   // doesn't seem to work, the Sort in the PageRequest is simply ignored - actually in the generated SQL
   // the Sort clauses are appended to the existing order by clause in the query.
   // The only way to get the last page and sort it in the normal order is either to sort the results
   // from the reversed query in code, or to use Sort and first query the record count and calculate the
   // page range to request. The former method is the one used by the PHP code but I'd hoped that Java would be
   // more capable - should have know better.
   // Using Count and calculated Pageable means there is no need for the @Query version, can use simple
   // DQMs
   // Fork! it doesn't work. The pageable takes page number and page size and returns an incomplete page for the
   // last page. So back to using the reverse search query
   // This doesn't work: NULL in statement for 'limit 25 offset null'
   // In otherwords it appears to append 'offset null' to the generated sql which is wrong, the offset must be omitted,
   // and I am not aware of any workaround.
   // List<Transaction> findFirst25ByAccountIdOrderBySequenceDesc(long accountId);

   // So, after another couple of wasted hours, I once again discover that Spring seems to be intended as an
   // enormous joke where nothing actually forking works. To get my last n records in the correct order
   // I'll have to do it all manually in the code.
   List<Transaction> findByAccountId(long accountId, Pageable pageable);
   long countByAccountId(long accountId);

   Optional<Transaction> findFirstByAccountIdOrderBySequenceDesc(long accountId); // latest

   Optional<Transaction> findFirstByAccountIdAndSequenceLessThanOrderBySequenceDesc(long accountId, long sequence); // previous

   List<Transaction> findByAccountIdAndSequenceGreaterThanOrderBySequenceAsc(long accountId, long sequence); // following

//   List<Transaction> findByAccountIdAndCheckedOrderBySequenceAsc(long accountId, boolean checked);
   List<Transaction> findByAccountIdAndCheckedOrderBySequenceAsc(long accountId, boolean checked, Pageable pageable);
}
