package com.squadx.crown.makemoneyapp.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.controller.AppController;
import com.squadx.crown.makemoneyapp.controller.PreferenceController;
import com.squadx.crown.makemoneyapp.controller.ReactionController;
import com.squadx.crown.makemoneyapp.controller.UpdateDataset;
import com.squadx.crown.makemoneyapp.model.LiUrl;
import com.squadx.crown.makemoneyapp.model.ListItem;
import com.squadx.crown.makemoneyapp.model.Primary;
import com.squadx.crown.makemoneyapp.page.BrowserActivity;
import com.squadx.crown.makemoneyapp.page.HomeActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ListItemAdapter.class.getName();
    private Context mContext;
    private List<ListItem> mDataset;

    public ListItemAdapter(Context context, List<ListItem> dataset) {
        this.mContext = context;
        this.mDataset = dataset;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView;
        switch (viewType) {
            case ListItem.TYPE_URL: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_primary_v2, parent, false);
                return new ViewHolder.PrimaryV2VH(rootView);
            }
            case ListItem.TYPE_PRIMARY:
            default: {
                rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_primary, parent, false);
                return new ViewHolder.PrimaryVH(rootView);
            }
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ListItem.TYPE_PRIMARY: {
                setUpPrimaryItem((ViewHolder.PrimaryVH) holder, (Primary) mDataset.get(position));
                break;
            }
            case ListItem.TYPE_URL: {
                setUpPrimaryV2Item((ViewHolder.PrimaryV2VH) holder, (LiUrl) mDataset.get(position));
                break;
            }
        }
    }

    private void changeButtonStyle(MaterialButton button, int color) {
        button.setTextColor(color);
        button.setIconTint(new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}}, new int[]{color}));
    }

    private void setUpPrimaryV2Item(ViewHolder.PrimaryV2VH holder, LiUrl obj) {
        Glide.with(mContext).load(obj.getImage()).into(holder.image);
        holder.title.setText(obj.getTitle());
        holder.source.setText(String.format("Source: %s", obj.getSource() != null ? obj.getSource() : ""));
        holder.upVote.setText(obj.getUpVotes() < 10 ? "Up" : String.valueOf(obj.getUpVotes()));
        holder.downVote.setText(obj.getDownVotes() < 10 ? "Down" : String.valueOf(obj.getDownVotes()));
        // holder.favouriteCount.setText(String.valueOf(obj.getFavouriteCount()));

        if (obj.getReads() < 10) {
            holder.read.setVisibility(View.INVISIBLE);
        } else {
            holder.read.setVisibility(View.VISIBLE);
            holder.read.setText(String.valueOf(obj.getReads()));
        }

        if (obj.getMyFav()) holder.favourite.setImageResource(R.drawable.ic_fill_favorite_24);
        else holder.favourite.setImageResource(R.drawable.ic_favorite_uncheck_24);

        Map<String, Boolean> reactions = PreferenceController.getInstance(mContext.getApplicationContext()).getReactions();
        if (reactions.containsKey(obj.getId())) {
            Boolean value = reactions.get(obj.getId());
            if (value == null) {
                changeButtonStyle((MaterialButton) holder.upVote, Color.WHITE);
                changeButtonStyle((MaterialButton) holder.downVote, Color.WHITE);
            } else if (value) {
                changeButtonStyle((MaterialButton) holder.upVote, mContext.getColor(R.color.colorAccent));
                changeButtonStyle((MaterialButton) holder.downVote, Color.WHITE);
            } else {
                changeButtonStyle((MaterialButton) holder.upVote, Color.WHITE);
                changeButtonStyle((MaterialButton) holder.downVote, mContext.getColor(R.color.colorAccent));
            }
        } else {
            changeButtonStyle((MaterialButton) holder.upVote, Color.WHITE);
            changeButtonStyle((MaterialButton) holder.downVote, Color.WHITE);
        }

        holder.layout.setOnClickListener(v -> {
            boolean isAdded = PreferenceController.getInstance(mContext.getApplicationContext()).addToRead(obj.getId());
            if (isAdded) {
                Map<String, Object> param = new HashMap<>();
                param.put("reads", FieldValue.increment(1));
                FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_article)).document(obj.getId()).update(param);
            }

            Intent intent = new Intent(mContext, BrowserActivity.class);
            intent.putExtra(BrowserActivity.TAG_ITEM_V2, obj.toString());
            mContext.startActivity(intent);
        });

        holder.favourite.setOnClickListener(v -> {
            PreferenceController pre = PreferenceController.getInstance(mContext.getApplicationContext());
            pre.updateFavourite(obj.getId());
            List<ListItem> temp = UpdateDataset.update(mContext.getApplicationContext(), mDataset);
            mDataset.clear();
            mDataset.addAll(temp);
            notifyDataSetChanged();
        });

        holder.upVote.setOnClickListener(v -> {
            if (!AppController.getInstance().isAuthenticated()) {
                showSignInRequireDialog();
                return;
            }
            ReactionController.clicked(mContext, obj.getId(), ReactionController.ACTION_UP_VOTE);
            notifyDataSetChanged();
        });

        holder.downVote.setOnClickListener(v -> {
            if (!AppController.getInstance().isAuthenticated()) {
                showSignInRequireDialog();
                return;
            }
            ReactionController.clicked(mContext, obj.getId(), ReactionController.ACTION_DOWN_VOTE);
            notifyDataSetChanged();
        });

        holder.remove.setOnClickListener(v -> {
            AlertDialog alert = new AlertDialog.Builder(mContext).setTitle("Remove article")
                    .setMessage("Are you sure you want to remove this article from your list?")
                    .setPositiveButton("Yes", ((dialog, which) -> {
                        PreferenceController.getInstance(mContext).updateRemovedArticle(obj.getId());
                        List<ListItem> temp = UpdateDataset.update(mContext.getApplicationContext(), mDataset);
                        mDataset.clear();
                        mDataset.addAll(temp);
                        notifyDataSetChanged();
                    }))
                    .setNegativeButton("No", null)
                    .create();
            alert.setOnShowListener(dialog -> {
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE);
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            });
            alert.show();
        });
    }

    private void showSignInRequireDialog() {
        AlertDialog alert = new AlertDialog.Builder(mContext).setTitle("Sign in require")
                .setMessage("Please sign in to perform the action")
                .setPositiveButton("Sign in", ((dialog, which) -> {
                    if (mContext instanceof HomeActivity)
                        ((HomeActivity) mContext).openMenu();
                }))
                .setNegativeButton("Cancel", null)
                .create();
        alert.setOnShowListener(dialog -> {
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        });
        alert.show();
    }

    private void setUpPrimaryItem(ViewHolder.PrimaryVH holder, Primary item) {
        holder.title.setText(item.getTitle());
        holder.subscription.setText(item.getSubscription());
        holder.by.setText(item.getBy());
        holder.root.setOnClickListener(view -> {
            PreferenceController.getInstance(mContext.getApplicationContext()).clicked();
            Intent intent = new Intent(mContext, BrowserActivity.class);
            intent.putExtra(BrowserActivity.TAG_ITEM, item);
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getItemType();
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
