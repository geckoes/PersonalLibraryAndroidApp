package com.taiuti.personallibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.taiuti.personallibrary.core.Books;
import com.taiuti.personallibrary.model.Book;
import com.taiuti.personallibrary.core.BookClient;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class SearchFragment extends Fragment implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton useFlash;
    private EditText textToSearch;
    private ListView lvBooks;
    private BookAdapter bookAdapter;
    private ProgressBar prBar;

    private static final int RC_BARCODE_CAPTURE = 9001;


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        textToSearch = (EditText) view.findViewById(R.id.text_to_search);

        useFlash = (CompoundButton) view.findViewById(R.id.use_flash);

        Button readBarcode = view.findViewById(R.id.read_barcode);
        readBarcode.setOnClickListener(this);
        Button searchBtn = view.findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(this);

        lvBooks = (ListView) view.findViewById(R.id.bookListView);
        ArrayList<Book> aBooks = new ArrayList<>();
        bookAdapter = new BookAdapter(getContext(), aBooks);
        lvBooks.setAdapter(bookAdapter);
        super.onViewCreated(view, savedInstanceState);

        prBar = view.findViewById(R.id.progressBar);
        prBar.setVisibility(View.INVISIBLE);

        setupBookSelectedListener();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    textToSearch.setText(barcode.displayValue);
                    Log.d("Barcode", "Barcode read: " + barcode.displayValue);
                } else {
                    //        statusMessage.setText(R.string.barcode_failure);
                    Log.d("Barcode", "No barcode captured, intent data is null");
                }
            } else {
                textToSearch.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        } else if(view.getId() == R.id.searchBtn) {
            //    List<Book> books = SearchOnLine.searchOnGoogle(txt.getText().toString());
            prBar.setVisibility(View.VISIBLE);
            fetchBooks(textToSearch.getText().toString());

        }
    }

    private void fetchBooks(String txt) {
        BookClient client;
        client = new BookClient();

        client.getBooksOnGoogleBooks(txt, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                TextView tvTitle = getView().findViewById(R.id.tvNoBookFound);
                if (response != null) {
                    ArrayList<Book> books = Books.fromJsonArray(response);
                    bookAdapter.clear();
                    bookAdapter.addAll(books);
                    bookAdapter.notifyDataSetChanged();
                    tvTitle.setVisibility(
                            bookAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
                } else {
                    tvTitle.setVisibility(
                            bookAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
                    tvTitle.setText("Timeout");
                }
                prBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setupBookSelectedListener() {
        lvBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

}
