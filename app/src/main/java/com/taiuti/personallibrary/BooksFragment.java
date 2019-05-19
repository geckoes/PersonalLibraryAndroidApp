package com.taiuti.personallibrary;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applovin.adview.AppLovinAdView;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.taiuti.personallibrary.core.Books;
import com.taiuti.personallibrary.model.Book;
import com.taiuti.personallibrary.model.BookOnLibrary;

import java.util.ArrayList;
import java.util.List;

public class BooksFragment extends Fragment {

    private ListView mBookListView;
    private BookAdapter mBookAdapter;
    ProgressBar progressBar;
    private TextView tvNoBooks;
    private FirebaseUser user;

    private ChildEventListener mChildEventListener;
    private DatabaseReference mBooksDatabaseReference;

    private List<BookOnLibrary> books;

    public BooksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_books, container, false);
    }

    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        // inizializza AppLoving
        AppLovinSdk.initializeSdk(getView().getContext());
        // inizializza il database
        mBooksDatabaseReference = FirebaseDatabase.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();

        books = new ArrayList<>();
        // Initialize references to views
        progressBar = getView().findViewById(R.id.progressBar);
        mBookListView = getView().findViewById(R.id.bookListView);
        tvNoBooks = getView().findViewById(R.id.txtNoBooks);

        // Initialize message ListView and its adapter
        final List<Book> allBooks = new ArrayList<>();
        mBookAdapter = new BookAdapter(getActivity(), R.layout.item_book, allBooks);
        mBookListView.setAdapter(mBookAdapter);

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                BookOnLibrary book = dataSnapshot.getValue(BookOnLibrary.class);
                mBookAdapter.add(book);
                books.add(book);
                progressBar.setVisibility(View.INVISIBLE);
                tvNoBooks.setVisibility(View.GONE);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
//        mBooksDatabaseReference.addChildEventListener(mChildEventListener);
        setupBookSelectedListener();
        writeLogUser(user);


        // Find the Ad Container
        LinearLayout ll = getView().findViewById(R.id.ll_ads);
        // Add the ad view to your activity layout

        // AppLoving
        final AppLovinAdView adView = new AppLovinAdView( AppLovinAdSize.BANNER, getView().getContext() );

        // Load an ad!
        adView.loadNextAd();

        ll.addView( adView );

        new MyTask().execute();
    }

    private void setupBookSelectedListener() {
        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), BookActivity.class);
                Gson bookGson = new Gson();
                String book = bookGson.toJson(mBookAdapter.getItem(i));
                intent.putExtra("book", book);
                startActivity(intent);
            }

        });
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_book_list, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                List<BookOnLibrary> searching = Books.SearchInLocal(books, s);
                mBookAdapter.clear();
                mBookAdapter.addAll(searching);

                if (mBookAdapter.getCount() == 0) {
                    TextView tvTitle = getView().findViewById(R.id.titleTextView);
                    tvTitle.setText(getResources().getString(R.string.no_book_found));
                }
                getActivity().setTitle(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (s.equals("")) {
                    mBookAdapter.clear();
                    mBookAdapter.addAll(books);
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onResume() {
        super.onResume();
        mBooksDatabaseReference.child(user.getUid()).child("books").addChildEventListener(mChildEventListener);
        getActivity().setTitle(R.string.app_name);
    }

    public void onPause() {
        super.onPause();
        mBooksDatabaseReference.removeEventListener(mChildEventListener);
    }

    private void writeLogUser(FirebaseUser user) {
        // Create new log when user entries in the app
        String key = mBooksDatabaseReference.child("logs").push().getKey();

        mBooksDatabaseReference.child("logs").child(key).setValue(user.getUid());

    }

    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
