package com.taiuti.personallibrary.model;

import org.json.JSONObject;

/**
 * Created by filippo on 21/01/18.
 */

public class BookOnLibrary extends Book {

    private String key;
    private String library;
    private Long shelf;
    private Long shelfId;
    private String status;
    private Integer statusId;
    private String note;

    public BookOnLibrary() {
        super();
        key = "";
        shelfId = 0L;
        statusId = 0;
    }

    public BookOnLibrary(String title, String authors, String isbn10, String isbn13, String published_date, String publisher, String language, String categories, String linkImage,
                         String key, String library, Long shelf, Long shefId, String status, Integer statusId, String note) {
        super( title,  authors, isbn10, isbn13, published_date,  publisher, language, categories, linkImage);
        this.key = key;
        this.library = library;
        this.shelf = shelf;
        this.shelfId = shefId;
        this.status = status;
        this.statusId = statusId;
        this.note = note;
    }

    public BookOnLibrary(JSONObject jsonObject) {
        super(jsonObject);

        JSONObject volumeInfo = jsonObject.optJSONObject("volumeInfo");
        if (volumeInfo != null) {
            this.library = volumeInfo.optString("library");
            this.key = volumeInfo.optString("key");
            this.shelf = Long.getLong(volumeInfo.optString("shelf"));
            this.shelfId = Long.getLong(volumeInfo.optString("shelfId"));
            this.status = volumeInfo.optString("status");
            this.statusId = Integer.getInteger(volumeInfo.optString("statusId"));

        }
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public Long getShelfId() {
        return shelfId;
    }

    public void setShelfId(Long shelfId) {
        this.shelfId = shelfId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusiId(Integer statusId) {
        this.statusId = statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
