package com.taiuti.personallibrary.core;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by filippo on 15/01/18.
 */

public class PersonalLibrary extends Application {
    final int ACTION_FOR_ADVERTISEMENT = 5;
    private int actionForAdvertisement = ACTION_FOR_ADVERTISEMENT;

    public int getActionForAdvertisement() {
        return actionForAdvertisement;
    }

    public void setAction_for_advertisement(int action_for_advertisement) {
        actionForAdvertisement = action_for_advertisement;
    }

    public void onCreate() {
        super.onCreate();
        // inizializza il database
        FirebaseDatabase mFirebaseDatabase;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);

    }
}
