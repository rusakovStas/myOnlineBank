package com.stasdev.backend.model.entitys;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private ApplicationUser userFrom;
    private Long idFrom;
    private Long idTo;
    private Amount amount;
    private ApplicationUser userTo;

    public Transaction() {
    }

    public ApplicationUser getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(ApplicationUser userFrom) {
        this.userFrom = userFrom;
    }

    public Long getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(Long idFrom) {
        this.idFrom = idFrom;
    }

    public Long getIdTo() {
        return idTo;
    }

    public void setIdTo(Long idTo) {
        this.idTo = idTo;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public ApplicationUser getUserTo() {
        return userTo;
    }

    public void setUserTo(ApplicationUser userTo) {
        this.userTo = userTo;
    }
}
