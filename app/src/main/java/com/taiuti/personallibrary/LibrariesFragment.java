package com.taiuti.personallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.taiuti.personallibrary.core.Libraries;
import com.taiuti.personallibrary.model.Book;
import com.taiuti.personallibrary.model.BookOnLibrary;
import com.taiuti.personallibrary.model.Library;

import java.util.ArrayList;
import java.util.List;


public class LibrariesFragment extends Fragment {

    private Spinner spLibrary;
    private ArrayAdapter<Library> adLibrary;
    private List<Library> libraryList;
    private TextView txtNoBooks;

    private ProgressBar progressBar;

    private DatabaseReference dbRef;
    private ValueEventListener valueEventListener;
    private FirebaseUser user;
    private Long indexLibrary;

    private ListView bookListView;
    private BookAdapter bookAdapter;

    public LibrariesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_libraries, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inflate the layout for this fragment
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO da togliere e mettere in LibraryFragment
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showAddLibraryDialog();
            }
        });

        dbRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        libraryList = new ArrayList<>();
        adLibrary = new ArrayAdapter<Library>(getContext(),
                android.R.layout.simple_spinner_item,
                libraryList);

        spLibrary = view.findViewById(R.id.spLibrary);
        spLibrary.setAdapter(adLibrary);

        indexLibrary = 0L;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                populateLibray(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        spLibrary.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bookAdapter.clear();
                if  (i > 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    populateBooks();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        bookListView = (ListView) getView().findViewById(R.id.bookListView);

        // Initialize message ListView and its adapter
        List<Book> allBooks = new ArrayList<>();
        bookAdapter = new BookAdapter(getActivity(), R.layout.item_book, allBooks);
        bookListView.setAdapter(bookAdapter);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        txtNoBooks = view.findViewById(R.id.txtNoBooks);
        txtNoBooks.setVisibility(View.GONE);
        setupBookSelectedListener();
    }

    private void showAddLibraryDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_library, null);
        dialogBuilder.setView(dialogView);

        final EditText edAddLibrary = (EditText) dialogView.findViewById(R.id.edAddLibrary);
        final EditText edAddShelves = (EditText) dialogView.findViewById(R.id.edAddShelves);

        dialogBuilder.setTitle("Add Library dialog");
        dialogBuilder.setMessage("Enter library name and the number of shelves");

        final AlertDialog b = dialogBuilder.create();
        b.show();

        Button btnAddLibrary = (Button) dialogView.findViewById(R.id.btnAddLibrary);

        btnAddLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String edLib = edAddLibrary.getText().toString();
                String edShelf = edAddShelves.getText().toString();

                if (!(edLib.equals("") && edShelf.equals(""))) {
                    Long lastShelf = Libraries.getLastShelf(libraryList, edAddLibrary.getText().toString());

                    for (int i = 1; i <= Integer.parseInt(edAddShelves.getText().toString()); i++) {
                        Library newLibrary = new Library(++indexLibrary, edAddLibrary.getText().toString(), lastShelf + i);

                        dbRef.child(user.getUid()).child("libraries1").child(Long.toString(indexLibrary)).setValue(newLibrary);
                    }
                    b.cancel();
                }
            }
        });

    }

    private void setupBookSelectedListener() {
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), BookActivity.class);
                Gson bookGson = new Gson();
                String book = bookGson.toJson(bookAdapter.getItem(i));
                intent.putExtra("book", book);
                startActivity(intent);
            }

        });
    }

    private void populateLibray(DataSnapshot dataSnapshot) {
        adLibrary.clear();

        adLibrary.add(new Library(0L, "Select a shelf ..."));

        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            // read libraries in db and add in adapter
            Library lib = postSnapshot.getValue(Library.class);
            adLibrary.add(lib);

            if (lib.get_id() > indexLibrary) {
                indexLibrary = lib.get_id();
            }


        }
        adLibrary.notifyDataSetChanged();
    }


    private void populateBooks() {
        Library lib = (Library) spLibrary.getSelectedItem();
        dbRef.child(user.getUid()).child("books_on_shelves").child(lib.get_id().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            // read libraries in db and add in adapter
                            BookOnLibrary book = postSnapshot.getValue(BookOnLibrary.class);

                            bookAdapter.add(book);
                            txtNoBooks.setVisibility(View.GONE);
                        }
                        if (bookAdapter.getCount() == 0) {
                            txtNoBooks.setVisibility(View.VISIBLE);
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
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
