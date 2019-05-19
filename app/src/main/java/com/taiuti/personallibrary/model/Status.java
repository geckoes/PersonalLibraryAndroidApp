package com.taiuti.personallibrary.model;

/**
 * Created by filippo on 21/01/18.
 */

public class Status {
    private int id;
    private String status;

    public Status(int id, String status) {
        this.id = id;
        this.status = status;
    }

    public String toString() {
        return status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
