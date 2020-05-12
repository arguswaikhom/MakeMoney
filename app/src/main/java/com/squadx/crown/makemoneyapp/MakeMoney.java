package com.squadx.crown.makemoneyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MakeMoney extends AppCompatActivity {

    public static final String LOG_TAG_MAKEMONEY = MakeMoney.class.getSimpleName();

    public static int countClick = 0;

    private InterstitialAd mInterstitialAd;

    private List<ListContentPrimary> primaryListMakeMoney;
    private List<String> listTitleMakeMoney;
    private List<String> listLinkMakeMoney;
    private List<String> listByMakeMoney;
    private List<String> listSubscriptionMakeMoney;
    private PrimaryListAdapter primaryListAdapter;
    private AdView mAdView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_money);

        MobileAds.initialize(this, "ca-app-pub-1616066953053446~3374996169");
        /*MobileAds.initialize(this,
                        "ca-app-pub-3940256099942544~3347511713");*/
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1616066953053446/4228912838");
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.baseline_monetization_on_24);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        ListView primaryListViewMakeMoney = findViewById(R.id.primary_list);
        primaryListMakeMoney = getPrimaryListMakeMoney();

        primaryListAdapter = new PrimaryListAdapter(this, (ArrayList<ListContentPrimary>) primaryListMakeMoney);
        primaryListViewMakeMoney.setAdapter(primaryListAdapter);

        primaryListViewMakeMoney.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                countClick = countClick + 1;
                ListContentPrimary currentContentPrimaryObject = primaryListAdapter.getItem(i);

                Intent intent = new Intent(getApplicationContext(), WebBrowser.class);
                intent.putParcelableArrayListExtra(LOG_TAG_MAKEMONEY, currentContentPrimaryObject);
                startActivity(intent);
            }
        });

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
        if (mInterstitialAd.isLoaded() && countClick >= 3) {
            mInterstitialAd.show();
            countClick = 0;
        }
        Log.v(LOG_TAG_MAKEMONEY, "CountClick: " + countClick);
        Log.v(LOG_TAG_MAKEMONEY, "Interstitial loaded = " + String.valueOf(mInterstitialAd.isLoaded()));
    }

    public List<ListContentPrimary> getPrimaryListMakeMoney() {
        listTitleMakeMoney = Arrays.asList(getResources().getStringArray(R.array.list_title_make_money));
        listLinkMakeMoney = Arrays.asList(getResources().getStringArray(R.array.list_link_make_money));
        listSubscriptionMakeMoney = Arrays.asList(getResources().getStringArray(R.array.list_subscription_make_money));
        listByMakeMoney = Arrays.asList(getResources().getStringArray(R.array.list_by_make_money));
        List<ListContentPrimary> list = new ArrayList<>();

        for (int i = 0; i < listTitleMakeMoney.size() || i < listLinkMakeMoney.size(); i++) {
            list.add(new ListContentPrimary(listTitleMakeMoney.get(i),
                    listLinkMakeMoney.get(i), listSubscriptionMakeMoney.get(i), listByMakeMoney.get(i)));
        }
        return list;
    }
}