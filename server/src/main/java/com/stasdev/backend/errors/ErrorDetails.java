package com.stasdev.backend.errors;

import java.util.Date;

public class ErrorDetails {
    private Date date;
    private String code;
    private String message;

    public ErrorDetails(Date date, String code, String message) {
        this.date = date;
        this.code = code;
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
