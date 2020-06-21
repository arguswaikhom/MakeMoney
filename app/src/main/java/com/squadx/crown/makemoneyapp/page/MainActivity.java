package com.squadx.crown.makemoneyapp.page;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.controller.PreferenceController;
import com.squadx.crown.makemoneyapp.util.MockData;
import com.squadx.crown.makemoneyapp.view.ListItemAdapter;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private InterstitialAd mInterstitialAd;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView mAdView = findViewById(R.id.av_am_banner_ad);
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
    }

    private void setUpRecycler() {
        RecyclerView mRecyclerView = findViewById(R.id.rv_rvl_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new ListItemAdapter(this, MockData.getPrimaryListData()));
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
}