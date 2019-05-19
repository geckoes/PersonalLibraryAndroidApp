package com.taiuti.personallibrary.model;

/**
 * Created by filippo on 05/01/18.
 */

public class Library {

    private Long _id;
    private String library;
    private Long shelf;

    public Library(Long _id, String library, Long shelf) {
        this._id = _id;
        this.library = library;
        this.shelf = shelf;

    }

    public Library() {

    }

    public Library(long _id, String library) {
        this(_id, library, 0L);
    }

    public String toString() {
        String ret = library;
        if (shelf>0) {
            ret += " " + shelf;
        }
        return ret;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public Long getShelf() {
        return shelf;
    }

    public void setShelf(Long shelf) {
        this.shelf = shelf;
    }

}


