package com.squadx.crown.makemoneyapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PrimaryListAdapter extends ArrayAdapter<ListContentPrimary> {

    public static final String LOG_TAG = PrimaryListAdapter.class.getSimpleName();

    public PrimaryListAdapter(@NonNull Context context, @NonNull ArrayList<ListContentPrimary> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View currentView = convertView;
        if (currentView == null) {
            currentView = LayoutInflater.from(getContext()).inflate(R.layout.primary_list_content, parent, false);
        }

        ListContentPrimary currentContent = getItem(position);

        TextView title = currentView.findViewById(R.id.textView_title);
        title.setText(currentContent.getTitle());

        TextView subscription = currentView.findViewById(R.id.textView_subscription);
        subscription.setText(currentContent.getSubscription());

        TextView by = currentView.findViewById(R.id.textView_by);
        by.setText(currentContent.getBy());
        return currentView;
    }
}

