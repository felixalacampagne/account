package com.felixalacampagne.account.persistence.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.felixalacampagne.account.persistence.entities.StandingOrders;

@Repository
public interface StandingOrdersJpaRepository extends JpaRepository<StandingOrders, Long>
{


   public List<StandingOrders> findBySOEntryDateLessThanOrderBySOEntryDateAsc(Timestamp date);
}
