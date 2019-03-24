package com.stasdev.backend.model.entitys;


import com.stasdev.backend.errors.NotImplementedYet;

import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

@Embeddable
public class Amount implements Comparable<Amount>{

    private String currency = "RUR";
    private BigDecimal sum = new BigDecimal(new BigInteger("1000"), 2);

    public Amount(String currency, BigDecimal sum) {
        this.currency = currency;
        this.sum = sum;
    }

    public Amount() {
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    @Override
    public int compareTo(Amount o) {
        if (!this.currency.equals(o.currency)){
            throw new NotImplementedYet("Comparison between different currency not implemented yet");
        }
        return this.sum.compareTo(o.sum);
    }

}
