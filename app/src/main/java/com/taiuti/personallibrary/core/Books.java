package com.taiuti.personallibrary.core;

import com.taiuti.personallibrary.model.Book;
import com.taiuti.personallibrary.model.BookOnLibrary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filippo on 14/01/18.
 */

public class Books {

    // Decodes array of book json results into business model objects
    public static ArrayList<BookOnLibrary> fromJson(JSONArray jsonArray) {
        ArrayList<BookOnLibrary> books = new ArrayList<>(jsonArray.length());
        // Process each result in json array, decode and convert to business
        // object
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject bookJson = null;
            try {
                bookJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            BookOnLibrary book = new BookOnLibrary(bookJson);
            if (book != null) {
                books.add(book);
            }
        }
        return books;
    }

    public static List<BookOnLibrary> SearchInLocal(List<BookOnLibrary> books, String query) {

        List<BookOnLibrary> newBooks = new ArrayList<>();
        for (BookOnLibrary book : books) {
            if (book.check(query)) {
                newBooks.add(book);
            }
        }
        return newBooks;
    }

    public static Boolean checkBookInLibrary(List<BookOnLibrary> books, String title, String authors) {

        Boolean check = false;

        List<BookOnLibrary> newBooks = new ArrayList<>();
        for (BookOnLibrary book : books) {
            if (book.check(title) && book.check(authors)) {
                check = true;
                break;
            }
        }
        return check;
    }

    public static ArrayList<Book> fromJsonArray(JSONObject json) {
        ArrayList<Book> books = new ArrayList();
        try {
//            JSONObject object = new JSONObject(json);
            JSONArray array = json.getJSONArray("items");

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Book book = new Book(item);

                books.add(book);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return books;
    }
}
