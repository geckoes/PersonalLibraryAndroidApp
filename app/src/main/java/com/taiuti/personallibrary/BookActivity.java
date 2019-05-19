package com.taiuti.personallibrary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.taiuti.personallibrary.core.Libraries;
import com.taiuti.personallibrary.model.BookOnLibrary;
import com.taiuti.personallibrary.model.Library;
import com.taiuti.personallibrary.model.Status;

import java.util.ArrayList;
import java.util.List;


public class BookActivity extends AppCompatActivity {

    // Remote Config keys
    private final String ADVERTISMENT_FREQUENCE = "frequence_of_advertisement";

    final Context context = this;
    private DatabaseReference dbRef;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Long advertisementClick;
    private Long frequence_of_advertisement;
    private ValueEventListener valueEventListener;
    private FirebaseUser user;
    private ArrayAdapter<Library> adLibrary;
    private Spinner spLibrary;
    private Spinner spStatus;
    private List<Library> libraryList;
    private Long indexLibrary;
    private BookOnLibrary book;

    private EditText edTitle;
    private EditText edAuthors;
    private EditText edPub;
    private EditText edPubDate;
    private EditText edLanguage;
    private EditText edCategories;
    private ImageView ivBookCover;
    private String urlBook;
    private EditText edIsbn10;
    private EditText edIsbn13;
    private EditText edNote;

    private CheckBox cbAddACopy;
    private AppLovinAd loadedAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        dbRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_settings);

        frequence_of_advertisement = mFirebaseRemoteConfig.getLong(ADVERTISMENT_FREQUENCE);

        long cacheExpiration = 3600;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        }
                        frequence_of_advertisement = mFirebaseRemoteConfig.getLong(ADVERTISMENT_FREQUENCE);
                    }
                });

        advertisementClick = 0L;
        dbRef.child(user.getUid()).child("indexes").child("actions_for_advertisement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                advertisementClick = (Long) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Gson bookGson = new Gson();
        book = bookGson.fromJson(getIntent().getStringExtra("book"), BookOnLibrary.class);

        List<Status> statusList = new ArrayList<>();

        ArrayAdapter<Status> adStatus = new ArrayAdapter<Status>(this,
                android.R.layout.simple_spinner_item,
                statusList);

        Button btnDeleteBook = findViewById(R.id.btnDeleteBook);
        Button btnAddBook = findViewById(R.id.btnAddBook);

        Resources res = getResources();
        String[] status_strings = res.getStringArray(R.array.status);

        int counter = 0;
        for (String strStatus : status_strings) {
            Status status = new Status(counter, strStatus);
            statusList.add(status);

            counter++;
        }

        edTitle = findViewById(R.id.editTitle);
        edAuthors = findViewById(R.id.editAuthors);
        edPub = findViewById(R.id.editPublisher);
        edPubDate = findViewById(R.id.editPublishedDate);
        edLanguage = findViewById(R.id.editLanguage);
        edCategories = findViewById(R.id.editCategories);
        ivBookCover = findViewById(R.id.ivBookCover);
        edIsbn10 = findViewById(R.id.editIsbn10);
        edIsbn13 = findViewById(R.id.editIsbn13);
        edNote = findViewById(R.id.editNote);

        cbAddACopy = findViewById(R.id.cbAddACopy);

        spStatus = findViewById(R.id.spStatus);
        spStatus.setAdapter(adStatus);

        String title = "";
        if (book != null) {
            title = book.getKey();
            this.setTitle(book.getTitle());

            edTitle.setText(book.getTitle());
            edAuthors.setText(book.getAuthors());
            edPub.setText(book.getPublisher());
            edPubDate.setText(book.getPublishedDate());
            edLanguage.setText(book.getLanguage());
            edCategories.setText(book.getCategories());
            edIsbn10.setText(book.getIsbn10());
            edIsbn13.setText(book.getIsbn13());
            edNote.setText(book.getNote());

            urlBook = book.getLinkImage();
            Picasso.with(this).load(Uri.parse(book.getLinkImage())).error(R.drawable.no_cover).into(ivBookCover);

            spStatus.setSelection(book.getStatusId() > 0 ? book.getStatusId() : 0);

            btnDeleteBook.setVisibility(View.VISIBLE);

            cbAddACopy.setVisibility(View.VISIBLE);

        } else {
            ivBookCover.setVisibility(View.GONE);
        }

        if (title.equals("")) {
            btnDeleteBook.setVisibility(View.GONE);
            btnAddBook.setText(R.string.add_book);

            cbAddACopy.setVisibility(View.GONE);
        }

        libraryList = new ArrayList<>();
        adLibrary = new ArrayAdapter<Library>(context,
                android.R.layout.simple_spinner_item,
                libraryList);

        spLibrary = findViewById(R.id.spLibrary);
        spLibrary.setAdapter(adLibrary);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                populateLibray(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        btnDeleteBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cancella il libro dalla libreria

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(getResources().getString(R.string.delete_book));
                alertDialogBuilder
                        .setMessage(getResources().getString(R.string.ask_delete_book))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                Toast.makeText(context, getResources().getString(R.string.book_deleted), Toast.LENGTH_LONG).show();
                                dbRef.child(user.getUid()).child("books").child(book.getKey()).setValue(null);
                                dbRef.child(user.getUid()).child("books_on_shelves").child(Long.toString(book.getShelfId())).child(book.getKey()).setValue(null);
                                BookActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        final Button btnShowAddLibrary = findViewById(R.id.btnShowAddLibrary);
        btnShowAddLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layoutAddLibrary = findViewById(R.id.layoutAddLibrary);
                layoutAddLibrary.setVisibility(View.VISIBLE);
                btnShowAddLibrary.setVisibility(View.GONE);
            }
        });

        Button btnAddLibrary = findViewById(R.id.btnAddLibrary);
        btnAddLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // aggiunge la libreria
                addLibraryInDB(view);
            }
        });

        btnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // aggiunge o modifica il libro in libreria
                if (edTitle.getText().toString().equals("")) {
                    Snackbar.make(view, getResources().getString(R.string.insert_a_title), Snackbar.LENGTH_LONG).show();
                    edTitle.requestFocus();
                    return;
                }
                // controlla se la libreria è stata scelta
                Library lib = (Library) spLibrary.getSelectedItem();
                if (lib.get_id() == 0) {
                    Snackbar.make(view, getResources().getString(R.string.select_shelf), Snackbar.LENGTH_LONG).show();;
                    spLibrary.requestFocus();
                    return;
                }
                // controlla se lo stato è stato scelto
                Status status = (Status) spStatus.getSelectedItem();
                if (status.getId() == 0) {
                    Snackbar.make(view, getResources().getString(R.string.select_status), Snackbar.LENGTH_LONG).show();;
                    spStatus.requestFocus();
                    return;
                }
                if (book.getKey().equals("")) {

                    checkBookInLibrary(edTitle.getText().toString(), edAuthors.getText().toString());
                } else {
                    if (cbAddACopy.isChecked()) {
                        addBookInLibrary();
                    } else {
                        modifyBookInLibrary();
                    }
                }
                BookActivity.this.finish();
            }
        });

        // Load an Interstitial Ad
        AppLovinSdk.getInstance( this ).getAdService().loadNextAd( AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener()
        {
            @Override
            public void adReceived(AppLovinAd ad)
            {
                loadedAd = ad;
            }

            @Override
            public void failedToReceiveAd(int errorCode)
            {
                // Look at AppLovinErrorCodes.java for list of error codes.
            }
        } );
    }

    private void modifyBookInLibrary() {
        Library lib = (Library) spLibrary.getSelectedItem();
        Status status = (Status) spStatus.getSelectedItem();

        BookOnLibrary newBook = new BookOnLibrary(
                edTitle.getText().toString(),
                edAuthors.getText().toString(),
                edIsbn10.getText().toString(),
                edIsbn13.getText().toString(),
                edPubDate.getText().toString(),
                edPub.getText().toString(),
                edLanguage.getText().toString(),
                edCategories.getText().toString(),
                urlBook,
                book.getKey(),
                lib.getLibrary(),
                lib.getShelf(),
                lib.get_id(),
                status.getStatus(),
                status.getId(),
                edNote.getText().toString()
        );

        // cancella il libro dalla libreria
        dbRef.child(user.getUid()).child("books_on_shelves")
                .child(book.getShelfId().toString())
                .child(book.getKey()).setValue(null);
        // e lo reinserisce nel nuvo scaffale
        dbRef.child(user.getUid()).child("books_on_shelves")
                .child(lib.get_id().toString())
                .child(book.getKey()).setValue(newBook);
        // e modifica i dati del libro
        dbRef.child(user.getUid()).child("books")
                .child(book.getKey().toString())
                .setValue(newBook);
        makeAdvertisement();
    }

    private void addBookInLibrary() {
        Library lib = (Library) spLibrary.getSelectedItem();
        Status status = (Status) spStatus.getSelectedItem();

        String key = dbRef.child(user.getUid()).child("books").push().getKey();
        BookOnLibrary newBook = new BookOnLibrary(
                edTitle.getText().toString(),
                edAuthors.getText().toString(),
                edIsbn10.getText().toString(),
                edIsbn13.getText().toString(),
                edPubDate.getText().toString(),
                edPub.getText().toString(),
                edLanguage.getText().toString(),
                edCategories.getText().toString(),
                urlBook,
                key,
                lib.getLibrary().toString(),
                lib.getShelf(),
                lib.get_id(),
                status.getStatus(),
                status.getId(),
                edNote.getText().toString()
        );
        dbRef.child(user.getUid()).child("books/" + key).setValue(newBook);
        dbRef.child(user.getUid()).child("books_on_shelves")
                .child(lib.get_id().toString())
                .child(key).setValue(newBook);
        makeAdvertisement();
    }

    private void checkBookInLibrary(final String title, final String authors) {
        dbRef.child(user.getUid()).child("books").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isACopy = false;
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // read libraries in db and add in adapter
                    BookOnLibrary book = postSnapshot.getValue(BookOnLibrary.class);

                    if (book.check(title) && book.check(authors)) {
                        isACopy = true;
                        break;
                    }
                }
                if (isACopy) {
                    // libro già presente in libreria chiede di inserire una copia
                    // chiede conferma per inserire una copia
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle("Add book?");
                    alertDialogBuilder
                            .setMessage(getResources().getString(R.string.book_in_library))
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                    addBookInLibrary();
                                    BookActivity.this.finish();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else {
                    // inserisce il libro
                    addBookInLibrary();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addLibraryInDB(View view) {

        EditText edAddLibrary = findViewById(R.id.edAddLibrary);
        EditText edAddShelves = findViewById(R.id.edAddShelves);

        /*
         * TODO leggere da libraryList se esiste già la libreria edAAddLibrary e prendere l'ultimo scaffale
         */
        Long lastShelf = Libraries.getLastShelf(libraryList, edAddLibrary.getText().toString());

        for (int i = 1; i <= Integer.parseInt(edAddShelves.getText().toString()); i++) {
            Library newLibrary = new Library(++indexLibrary, edAddLibrary.getText().toString(), lastShelf + i);

            dbRef.child(user.getUid()).child("libraries").child(Long.toString(indexLibrary)).setValue(newLibrary);
        }

        Snackbar.make(view, getResources().getString(R.string.shelves_added), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        LinearLayout layoutAddLibrary = findViewById(R.id.layoutAddLibrary);
        layoutAddLibrary.setVisibility(View.GONE);
    }

    private void populateLibray(DataSnapshot dataSnapshot) {
        adLibrary.clear();

        adLibrary.add(new Library(0L, getResources().getString(R.string.select_shelf)));

        indexLibrary = 0L;
        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            // read libraries in db and add in adapter
            Library lib = postSnapshot.getValue(Library.class);
            adLibrary.add(lib);

            if (lib.get_id() > indexLibrary) {
                indexLibrary = lib.get_id();
            }
            if (book != null) {
                if (book.getShelfId().equals(lib.get_id())) {
                    spLibrary.setSelection(adLibrary.getCount() - 1);
                }
            }
        }
        adLibrary.notifyDataSetChanged();
    }

    private void makeAdvertisement() {
        ++advertisementClick;
        if (advertisementClick % frequence_of_advertisement == 0) {
            // go to adv
            AppLovinInterstitialAdDialog interstitialAd = AppLovinInterstitialAd.create( AppLovinSdk.getInstance( this ), this );

            interstitialAd.showAndRender( loadedAd );
        }
        dbRef.child(user.getUid()).child("indexes").child("actions_for_advertisement").setValue(advertisementClick);
    }

    public void onResume() {
        super.onResume();
        dbRef.child(user.getUid()).child("libraries").orderByChild("library").addValueEventListener(valueEventListener);
    }

    public void onPause() {
        super.onPause();
        dbRef.removeEventListener(valueEventListener);
    }

}
