package com.stasdev.backend.model.entitys;

public class Push {
    private String msg;

    public Push() {
    }

    public String getMsg() {

        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Push(String msg) {

        this.msg = msg;
    }
}
