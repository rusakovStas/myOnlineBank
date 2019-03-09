package com.stasdev.backend.model.entitys;


import java.math.BigDecimal;

public class Amount {

    private String currency;
    private BigDecimal summ;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }
}
