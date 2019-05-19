package com.taiuti.personallibrary.core;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by filippo on 13/01/18.
 */

public class BookClient {
    private static final String API_BASE_OPEN_LIBRARY_URL = "http://openlibrary.org/";
    private static final String API_BASE_GOOGLE_URL = "https://www.googleapis.com/books/v1/";
    private AsyncHttpClient client;

    public BookClient() {
        this.client = new AsyncHttpClient();
    }

    private String getApiOpenLibraryUrl(String relativeUrl) {
        return API_BASE_OPEN_LIBRARY_URL +  relativeUrl;
    }

    private String getApiGoogleUrl(String relativeUrl) {
        return API_BASE_GOOGLE_URL +  relativeUrl;
    }

    // Method fo accessing the search API
    public void getBooksOnOpenLibrary(final String query, JsonHttpResponseHandler handler) {
        try {
            String url = getApiOpenLibraryUrl("search.json?q=");
            client.get(url + URLEncoder.encode(query, "utf-8"), handler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void getBooksOnGoogleBooks(String query, JsonHttpResponseHandler handler) {
        try {
            String url = getApiGoogleUrl("volumes?q=");
            client.get(url + URLEncoder.encode(query, "utf-8"), handler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
