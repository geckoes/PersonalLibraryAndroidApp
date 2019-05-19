package com.taiuti.personallibrary.core;

import com.taiuti.personallibrary.model.Library;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filippo on 21/01/18.
 */

public class Libraries {

    public static ArrayList<Library> getLibraries() {
        ArrayList<Library> libraries = new ArrayList<>();

        return libraries;
    }

    public static Long getLastShelf(List<Library> listLibrary, String library) {
        Long lastShelf = 0L;
        for (Library lib : listLibrary) {
            if (lib.getLibrary().equals(library)) {
                if (lib.getShelf() > lastShelf) {
                    lastShelf = lib.getShelf();
                }
            }
        }
        return lastShelf;
    }

}
