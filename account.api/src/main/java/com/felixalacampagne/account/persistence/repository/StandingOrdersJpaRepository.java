package com.felixalacampagne.account.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.felixalacampagne.account.persistence.entities.StandingOrders;

@Repository
public interface StandingOrdersJpaRepository extends JpaRepository<StandingOrders, Long>
{

   public Optional<StandingOrders> findFirstBySOEntryDateLessThanEqualOrderBySONextPayDateAsc(LocalDate soEntryDate);
   public List<StandingOrders> findBySOEntryDateLessThanEqualOrderBySOEntryDateAsc(LocalDate soEntryDate);
}
