package com.squadx.crown.makemoneyapp.view.viewholder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;

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
import com.squadx.crown.makemoneyapp.databinding.LiPrimaryLargeImageBinding;
import com.squadx.crown.makemoneyapp.model.ArticleV0;
import com.squadx.crown.makemoneyapp.model.ListItem;
import com.squadx.crown.makemoneyapp.page.ArticleHtmlActivity;
import com.squadx.crown.makemoneyapp.page.BrowserActivity;
import com.squadx.crown.makemoneyapp.page.HomeActivity;
import com.squadx.crown.makemoneyapp.util.ListItemType;
import com.squadx.crown.makemoneyapp.view.ListItemAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimaryLargeImageVH extends RecyclerView.ViewHolder {

    private final Context context;
    private final LiPrimaryLargeImageBinding binding;
    private ArticleV0 article;

    public PrimaryLargeImageVH(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        binding = LiPrimaryLargeImageBinding.bind(itemView);
    }

    public void bind(ListItemAdapter adapter, List<ListItem> dataset, ListItem item) {
        article = (ArticleV0) item;

        Glide.with(context).load(article.getImage()).into(binding.imageIv);
        binding.titleTv.setText(article.getTitle());
        binding.sourceTv.setText(String.format("Source: %s", article.getSource() != null ? article.getSource() : ""));
        binding.reactionInclude.upVoteBtn.setText(article.getUpVotes() < 10 ? "Up" : String.valueOf(article.getUpVotes()));
        binding.reactionInclude.downVoteBtn.setText(article.getDownVotes() < 10 ? "Down" : String.valueOf(article.getDownVotes()));
        // holder.favouriteCount.setText(String.valueOf(obj.getFavouriteCount()));

        if (article.getReads() < 10) {
            binding.reactionInclude.readBtn.setVisibility(View.INVISIBLE);
        } else {
            binding.reactionInclude.readBtn.setVisibility(View.VISIBLE);
            binding.reactionInclude.readBtn.setText(String.valueOf(article.getReads()));
        }

        if (article.getMyFav()) binding.favIv.setImageResource(R.drawable.ic_fill_favorite_24);
        else binding.favIv.setImageResource(R.drawable.ic_favorite_uncheck_24);

        Map<String, Boolean> reactions = PreferenceController.getInstance(context.getApplicationContext()).getReactions();
        if (reactions.containsKey(article.getId())) {
            Boolean value = reactions.get(article.getId());
            if (value == null) {
                changeButtonStyle((MaterialButton) binding.reactionInclude.upVoteBtn, Color.WHITE);
                changeButtonStyle((MaterialButton) binding.reactionInclude.downVoteBtn, Color.WHITE);
            } else if (value) {
                changeButtonStyle((MaterialButton) binding.reactionInclude.upVoteBtn, context.getColor(R.color.colorAccent));
                changeButtonStyle((MaterialButton) binding.reactionInclude.downVoteBtn, Color.WHITE);
            } else {
                changeButtonStyle((MaterialButton) binding.reactionInclude.upVoteBtn, Color.WHITE);
                changeButtonStyle((MaterialButton) binding.reactionInclude.downVoteBtn, context.getColor(R.color.colorAccent));
            }
        } else {
            changeButtonStyle((MaterialButton) binding.reactionInclude.upVoteBtn, Color.WHITE);
            changeButtonStyle((MaterialButton) binding.reactionInclude.downVoteBtn, Color.WHITE);
        }

        binding.articleCl.setOnClickListener(this::onClickedArticle);

        binding.favIv.setOnClickListener(v -> {
            PreferenceController pre = PreferenceController.getInstance(context.getApplicationContext());
            pre.updateFavourite(article.getId());
            List<ListItem> temp = UpdateDataset.update(context.getApplicationContext(), dataset);
            dataset.clear();
            dataset.addAll(temp);
            adapter.notifyDataSetChanged();
        });

        binding.reactionInclude.upVoteBtn.setOnClickListener(v -> {
            if (!AppController.getInstance().isAuthenticated()) {
                showSignInRequireDialog();
                return;
            }
            ReactionController.clicked(context, article.getId(), ReactionController.ACTION_UP_VOTE);
            adapter.notifyDataSetChanged();
        });

        binding.reactionInclude.downVoteBtn.setOnClickListener(v -> {
            if (!AppController.getInstance().isAuthenticated()) {
                showSignInRequireDialog();
                return;
            }
            ReactionController.clicked(context, article.getId(), ReactionController.ACTION_DOWN_VOTE);
            adapter.notifyDataSetChanged();
        });

        binding.removeIv.setOnClickListener(v -> {
            AlertDialog alert = new AlertDialog.Builder(context).setTitle("Remove article")
                    .setMessage("Are you sure you want to remove this article from your list?")
                    .setPositiveButton("Yes", ((dialog, which) -> {
                        PreferenceController.getInstance(context).updateRemovedArticle(article.getId());
                        List<ListItem> temp = UpdateDataset.update(context.getApplicationContext(), dataset);
                        dataset.clear();
                        dataset.addAll(temp);
                        adapter.notifyDataSetChanged();
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

    private void changeButtonStyle(MaterialButton button, int color) {
        button.setTextColor(color);
        button.setIconTint(new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}}, new int[]{color}));
    }

    private void showSignInRequireDialog() {
        AlertDialog alert = new AlertDialog.Builder(context).setTitle("Sign in require")
                .setMessage("Please sign in to perform the action")
                .setPositiveButton("Sign in", ((dialog, which) -> {
                    if (context instanceof HomeActivity)
                        ((HomeActivity) context).openMenu();
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

    private void onClickedArticle(View view) {
        if (article == null) return;
        boolean isAdded = PreferenceController.getInstance(context.getApplicationContext()).addToRead(article.getId());
        if (isAdded) {
            Map<String, Object> param = new HashMap<>();
            param.put("reads", FieldValue.increment(1));
            FirebaseFirestore.getInstance().collection(context.getString(R.string.col_article)).document(article.getId()).update(param);
        }

        if (article.getMmType() == ListItemType.ARTICLE_URL) {
            Intent intent = new Intent(context, BrowserActivity.class);
            intent.putExtra(BrowserActivity.TAG_ITEM_V2, article.toString());
            context.startActivity(intent);
        } else if (article.getMmType() == ListItemType.ARTICLE_HTML) {
            Intent intent = new Intent(context, ArticleHtmlActivity.class);
            intent.putExtra(ArticleHtmlActivity.ARTICLE_HTML, article.toString());
            context.startActivity(intent);
        }
    }
}
