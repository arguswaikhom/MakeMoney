package com.squadx.crown.makemoneyapp.view;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squadx.crown.makemoneyapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

class ViewHolder {
    static class PrimaryVH extends RecyclerView.ViewHolder {
        TextView title;
        TextView by;
        TextView subscription;
        LinearLayout root;

        PrimaryVH(View view) {
            super(view);
            title = view.findViewById(R.id.textView_title);
            by = view.findViewById(R.id.textView_by);
            subscription = view.findViewById(R.id.textView_subscription);
            root = view.findViewById(R.id.ll_plc_root);
        }
    }

    static class PrimaryV2VH extends RecyclerView.ViewHolder {
        @BindView(R.id.fab_ipv2_remove) FloatingActionButton remove;
        @BindView(R.id.iv_lpv2_image) ImageView image;
        @BindView(R.id.tv_ipv2_source) TextView source;
        @BindView(R.id.tv_ipv2_title) TextView title;
        @BindView(R.id.btn_lir_up_vote) Button upVote;
        @BindView(R.id.btn_lir_down_vote) Button downVote;
        @BindView(R.id.btn_lir_favourite) Button favouriteCount;
        @BindView(R.id.btn_lir_read) Button read;
        @BindView(R.id.cl_lpv2_layout) ConstraintLayout layout;
        @BindView(R.id.iv_lpv2_favourite) ImageView favourite;

        PrimaryV2VH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
