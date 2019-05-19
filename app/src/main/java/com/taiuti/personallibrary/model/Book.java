package com.taiuti.personallibrary.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by filippo on 05/01/18.
 */

public class Book {

    private String authors;
    private String categories;
    private String isbn10;
    private String isbn13;
    private String language;
    private String title;
    private String publishedDate;
    private String publisher;

    private String linkImage;

    public Book() {

    }

    public Book(String title, String authors, String isbn10, String isbn13, String published_date, String publisher, String language, String categories, String linkImage) {
        this.title = title;
        this.authors = authors;
        this.isbn10 = isbn10;
        this.isbn13 =  isbn13;
        this.publishedDate = published_date;
        this.publisher = publisher;
        this.language = language;
        this.categories = categories;
        this.linkImage = linkImage;
    }

    public Book(JSONObject jsonObject) {

        JSONObject volumeInfo = jsonObject.optJSONObject("volumeInfo");
        if (volumeInfo != null) {
            String title = volumeInfo.optString("title");
            this.title = title;

            this.authors = getAuthors(volumeInfo);

            this.publisher = volumeInfo.optString("publisher");
            this.publishedDate = volumeInfo.optString("publishedDate");

            this.language = volumeInfo.optString("language");

            if (!volumeInfo.isNull("industryIdentifiers")) {
                for (int i = 0; i < volumeInfo.optJSONArray("industryIdentifiers").length(); i++) {
                    JSONObject jsonobject = volumeInfo.optJSONArray("industryIdentifiers").optJSONObject(i);
                    if (jsonobject.optString("type").equals("ISBN_13")) {
                        this.isbn13 = jsonobject.optString("identifier");
                    } else if (jsonobject.optString("type").equals("ISBN_10")) {
                        this.isbn10 = jsonobject.optString("identifier");
                    }
                }
            }

            this.categories = getCategories(volumeInfo);

            JSONObject imageLinks = volumeInfo.optJSONObject("imageLinks");
            if (imageLinks != null) {
                this.linkImage = imageLinks.optString("smallThumbnail");
            } else {
                this.linkImage = "";
            }

        }
    }

    private String getAuthors(JSONObject jsonObject) {
        try {
            JSONArray authors = jsonObject.optJSONArray("authors");
            int numAuthors = authors.length();
            final String[] authorStrings = new String[numAuthors];
            for (int i = 0; i < numAuthors; ++i) {
                authorStrings[i] = authors.getString(i);
            }
            return TextUtils.join(", ", authorStrings);
        } catch (JSONException e) {
            return "";
        } catch (NullPointerException e) {
            return "";
        }
    }

    private String getCategories(JSONObject jsonObject) {
        try {
            JSONArray categories = jsonObject.optJSONArray("categories");
            int numCategories = categories.length();
            final String[] categoryStrings = new String[numCategories];
            for (int i = 0; i < numCategories; ++i) {
                categoryStrings[i] = categories.getString(i);
            }
            return TextUtils.join(", ", categoryStrings);
        } catch (JSONException e) {
            return "";
        } catch (NullPointerException e) {
            return "";
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String published_date) {
        this.publishedDate = published_date;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getLinkImage() {
        return linkImage;
    }

    public void setLinkImage(String linkImage) {
        this.linkImage = linkImage;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean check(String query) {
        if (title.contains(query.toLowerCase()) || authors.toLowerCase().contains(query.toLowerCase())) {
            return true;
        }
        return false;
    }
}
