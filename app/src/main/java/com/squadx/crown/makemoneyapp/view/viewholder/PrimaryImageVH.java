package com.squadx.crown.makemoneyapp.view.viewholder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.controller.PreferenceController;
import com.squadx.crown.makemoneyapp.controller.UpdateDataset;
import com.squadx.crown.makemoneyapp.databinding.LiPrimaryImageBinding;
import com.squadx.crown.makemoneyapp.model.ArticleV0;
import com.squadx.crown.makemoneyapp.model.ListItem;
import com.squadx.crown.makemoneyapp.page.ArticleHtmlActivity;
import com.squadx.crown.makemoneyapp.page.BrowserActivity;
import com.squadx.crown.makemoneyapp.util.ListItemType;
import com.squadx.crown.makemoneyapp.view.ListItemAdapter;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimaryImageVH extends RecyclerView.ViewHolder {

    private final Context context;
    private final LiPrimaryImageBinding binding;
    private ArticleV0 article;

    public PrimaryImageVH(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        binding = LiPrimaryImageBinding.bind(itemView);
    }

    public void bind(ListItemAdapter adapter, List<ListItem> dataset, ListItem item) {
        article = (ArticleV0) item;

        Glide.with(context).load(article.getImage()).into(binding.imageIv);
        binding.titleTv.setText(article.getTitle());

        if (article.getReads() < 10) {
            binding.readTv.setVisibility(View.INVISIBLE);
        } else {
            binding.readTv.setVisibility(View.VISIBLE);
            binding.readTv.setText(MessageFormat.format("{0} reads", article.getReads()));
        }

        if (article.getMyFav()) binding.favIv.setImageResource(R.drawable.ic_fill_favorite_24);
        else binding.favIv.setImageResource(R.drawable.ic_favorite_uncheck_24);


        binding.articleLl.setOnClickListener(this::onClickedArticle);

        binding.favIv.setOnClickListener(v -> {
            PreferenceController pre = PreferenceController.getInstance(context.getApplicationContext());
            pre.updateFavourite(article.getId());
            List<ListItem> temp = UpdateDataset.update(context.getApplicationContext(), dataset);
            dataset.clear();
            dataset.addAll(temp);
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
