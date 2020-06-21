package com.squadx.crown.makemoneyapp.page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.controller.PreferenceController;
import com.squadx.crown.makemoneyapp.controller.ReactionController;
import com.squadx.crown.makemoneyapp.controller.UpdateDataset;
import com.squadx.crown.makemoneyapp.model.LiUrl;
import com.squadx.crown.makemoneyapp.model.ListItem;
import com.squadx.crown.makemoneyapp.view.ListItemAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    private final String TAG = HomeActivity.class.getName();
    private List<ListItem> mDataSet;
    private ListItemAdapter mAdapter;
    private InterstitialAd mInterstitialAd;

    @BindView(R.id.pbar_ah_loading)
    ProgressBar mLoadingPBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView mAdView = findViewById(R.id.av_ah_banner_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (PreferenceController.getInstance(getApplicationContext()).getClick() % 4 == 0) {
                    mInterstitialAd.show();
                    PreferenceController.getInstance(getApplicationContext()).clicked();
                }
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        setUpRecycler();
        getData();
    }

    private void setUpRecycler() {
        RecyclerView mRecyclerView = findViewById(R.id.rv_ah_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDataSet = new ArrayList<>();
        mAdapter = new ListItemAdapter(this, mDataSet);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInterstitialAd.isLoaded() || mInterstitialAd.isLoading()) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int clicks = PreferenceController.getInstance(getApplicationContext()).getClick();
        Log.v(TAG, "CountClick: " + clicks);
        Log.v(TAG, "Interstitial loaded = " + mInterstitialAd.isLoaded());
    }

    @OnClick(R.id.iv_ah_refresh)
    void getData() {
        mLoadingPBar.setVisibility(View.VISIBLE);
        ReactionController.syncWithFirestore(this, null);
        FirebaseFirestore.getInstance().collection(getString(R.string.col_article)).get()
                .addOnCompleteListener(task -> mLoadingPBar.setVisibility(View.INVISIBLE))
                .addOnSuccessListener(this::onSuccess).addOnFailureListener(this::onFailure);
    }

    @OnClick(R.id.iv_ah_menu)
    public void openMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivityForResult(intent, MenuActivity.RC_MENU);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MenuActivity.RC_MENU && resultCode == RESULT_OK && data != null) {
            boolean doRefresh = data.getBooleanExtra(MenuActivity.RC_REFRESH_REQUIRE, false);
            if (doRefresh) getData();
        }
    }

    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
        mDataSet.clear();
        if (!queryDocumentSnapshots.isEmpty()) {
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                if (doc.exists()) {
                    Long classType = (Long) doc.get("class");
                    if (classType != null) {
                        if (classType == ListItem.TYPE_URL) {
                            LiUrl obj = doc.toObject(LiUrl.class);
                            if (obj == null) {
                                Log.v(TAG, "Null doc found: " + doc);
                                continue;
                            }
                            obj.setId(doc.getId());
                            mDataSet.add(obj);
                        }
                    } else {
                        Log.v(TAG, "Empty class field: " + doc);
                    }
                }
            }
        }
        List<ListItem> temp = UpdateDataset.update(getApplicationContext(), mDataSet);
        mDataSet.clear();
        mDataSet.addAll(temp);
        Collections.sort(mDataSet, (o1, o2) -> ((LiUrl) o2).getMyFav().compareTo(((LiUrl) o1).getMyFav()));
        mAdapter.notifyDataSetChanged();
    }

    public void onFailure(@NonNull Exception e) {
        Snackbar.make(findViewById(android.R.id.content), "Something went wrong!!", Snackbar.LENGTH_SHORT);
    }
}