package com.felixalacampagne.account.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.felixalacampagne.account.persistence.entities.PhoneAccount;

@Repository
public interface PhoneAccountJpaRepository extends JpaRepository<PhoneAccount, Long>
{

}
