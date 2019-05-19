package com.taiuti.personallibrary;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.taiuti.personallibrary.model.Book;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {

    Context context;
    List<Book> booksList;

    public BookAdapter(Context context, int resource, List<Book> booksList) {
        super(context, resource, booksList);
        this.context = context;
        this.booksList = booksList;
    }

    public BookAdapter(Context context, ArrayList<Book> aBooks) {
        super(context, 0, aBooks);
        this.context = context;
        this.booksList = aBooks;
    }

    @Override
    public int getCount() {
        return booksList.size();
    }

    @Override
    public Book getItem(int position) {
        return booksList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = parent.inflate(context, R.layout.item_book, null);

        }
        Book book = (Book) getItem(position);

        ImageView coverImageView = (ImageView) convertView.findViewById(R.id.coverImageView);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.authorsTextView);

        titleTextView.setText(book.getTitle());
        authorTextView.setText(book.getAuthors());
        Picasso.with(getContext()).load(Uri.parse(book.getLinkImage())).error(R.drawable.no_cover).into(coverImageView);

        return convertView;
    }
}
