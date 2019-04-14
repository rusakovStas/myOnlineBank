package com.stasdev.backend.model.repos;

import com.stasdev.backend.model.entitys.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


}
