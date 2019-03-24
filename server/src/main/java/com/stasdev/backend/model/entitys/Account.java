package com.stasdev.backend.model.entitys;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Amount amount;
    private String number; /*TODO сделать автогенерацию */
    private String name;

    @ManyToOne
    @JoinColumn(name="user_id")
    private ApplicationUser user;

    public Account(Amount amount, String number, String accountName, ApplicationUser user) {
        this.amount = amount;
        this.number = number;
        this.name = accountName;
        this.user = user;
    }

    public Account() {
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public void setUser(ApplicationUser user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(getId(), account.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }

    public void setId(Long id) {
        this.id = id;
    }
}
