package com.squadx.crown.makemoneyapp.page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.squadx.crown.makemoneyapp.BuildConfig;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.controller.PreferenceController;
import com.squadx.crown.makemoneyapp.controller.ReactionController;
import com.squadx.crown.makemoneyapp.controller.UpdateDataset;
import com.squadx.crown.makemoneyapp.databinding.ActivityHomeBinding;
import com.squadx.crown.makemoneyapp.model.ArticleHtml;
import com.squadx.crown.makemoneyapp.model.ArticleUrl;
import com.squadx.crown.makemoneyapp.model.ArticleV0;
import com.squadx.crown.makemoneyapp.model.ListItem;
import com.squadx.crown.makemoneyapp.util.ListItemType;
import com.squadx.crown.makemoneyapp.util.MmString;
import com.squadx.crown.makemoneyapp.view.ListItemAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private final String TAG = HomeActivity.class.getName();
    private final List<ListItem> mDataSet = new ArrayList<>();
    private ListItemAdapter mAdapter;
    // private InterstitialAd mInterstitialAd;
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(this, initializationStatus -> {
        });

        initMoPubSdk();

       /* MobileAds.initialize(this, initializationStatus -> {
        });*/

        /*MobileAds.initialize(this, initializationStatus -> {
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                Log.d("MyApp", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status.getDescription(), status.getLatency()));
            }

            // Start loading ads here...
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        binding.bannerHomeAd.loadAd(adRequest);

        MediationTestSuite.launch(HomeActivity.this);*/

        /*mInterstitialAd = new InterstitialAd(this);
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
        });*/

        setUp();
        getData();
    }

    private void initMoPubSdk() {
        // if you want to support Native Facebook Ad then add below
        /*Map<String, String> facebookNativeBanner = new HashMap<>();
        facebookNativeBanner.put("native_banner", "true");*/

        // integration of FAN with MoPub
        /*SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder(getString(R.string.banner_home));
        configBuilder.withMediatedNetworkConfiguration(HomeActivity.class.getName(), facebookNativeBanner);*/

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(getString(R.string.banner_home))
                .withLegitimateInterestAllowed(false)
                .build();

        MoPub.initializeSdk(this, sdkConfiguration, () -> {
            binding.mopubBannerHomeAd.setAdUnitId(getString(R.string.banner_home));
            binding.mopubBannerHomeAd.loadAd();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mopubBannerHomeAd.destroy();
    }

    private void setUp() {
        binding.contentRv.setHasFixedSize(true);
        binding.contentRv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ListItemAdapter(this, mDataSet);
        binding.contentRv.setAdapter(mAdapter);

        binding.menuIv.setOnClickListener(v -> openMenu());
        binding.refreshIv.setOnClickListener(v -> getData());
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*if (!mInterstitialAd.isLoaded() || mInterstitialAd.isLoading()) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*int clicks = PreferenceController.getInstance(getApplicationContext()).getClick();
        Log.v(TAG, "CountClick: " + clicks);
        Log.v(TAG, "Interstitial loaded = " + mInterstitialAd.isLoaded());*/
    }

    void getData() {
        binding.loadingPbar.setVisibility(View.VISIBLE);
        ReactionController.syncWithFirestore(this, null);
        FirebaseFirestore.getInstance().collection(getString(R.string.col_article)).get()
                .addOnCompleteListener(task -> binding.loadingPbar.setVisibility(View.INVISIBLE))
                .addOnSuccessListener(this::onSuccessV2).addOnFailureListener(this::onFailure);
    }

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

    public void onSuccessV2(QuerySnapshot queryDocumentSnapshots) {
        Log.v(TAG, "onSuccessV2: " + PreferenceController.getInstance(getApplicationContext()).getVersion());
        if (!queryDocumentSnapshots.isEmpty()) {
            int pVersion = PreferenceController.getInstance(getApplicationContext()).getVersion();

            if (pVersion >= BuildConfig.VERSION_CODE) {
                showContent(queryDocumentSnapshots, true);
            } else {
                getFirebaseAppVersion(queryDocumentSnapshots);
            }
        }
    }

    private void showContent(QuerySnapshot queryDocumentSnapshots, Boolean showFullContent) {
        mDataSet.clear();
        Log.v(TAG, "showContent");
        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
            try {
                if (doc.exists()) {
                    Boolean hide = (Boolean) doc.get("hide");
                    if (!showFullContent && hide != null && hide) continue;
                    Long classType = (Long) doc.get(MmString.fieldMmType);
                    if (classType != null) {
                        if (classType == ListItemType.ARTICLE_URL) {
                            ArticleUrl obj = doc.toObject(ArticleUrl.class);
                            if (obj == null) {
                                Log.v(TAG, "Null doc found: " + doc);
                                continue;
                            }
                            obj.setId(doc.getId());
                            mDataSet.add(obj);
                        } else if (classType == ListItemType.ARTICLE_HTML) {
                            ArticleHtml obj = doc.toObject(ArticleHtml.class);
                            obj.setId(doc.getId());
                            mDataSet.add(obj);
                        }
                    } else {
                        Log.v(TAG, "Empty class field: " + doc);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        List<ListItem> temp = UpdateDataset.update(getApplicationContext(), mDataSet);
        mDataSet.clear();
        mDataSet.addAll(temp);
        Collections.shuffle(mDataSet);
        Collections.sort(mDataSet, (o1, o2) -> {
            boolean x = ((ArticleV0) o2).getMyFav();
            boolean y = ((ArticleV0) o1).getMyFav();
            return (x == y) ? 0 : (x ? 1 : -1);
        });
        mAdapter.notifyDataSetChanged();
    }

    private void getFirebaseAppVersion(QuerySnapshot queryDocumentSnapshots) {
        Log.v(TAG, "getFirebaseAppVersion");

        FirebaseFirestore.getInstance().collection(getString(R.string.col_make_money))
                .document(getString(R.string.doc_app_version)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long fbVersion = (Long) documentSnapshot.get(getString(R.string.field_version));
                        Log.v(TAG, "Firebase version: " + fbVersion);
                        if (fbVersion != null) {
                            if (fbVersion < BuildConfig.VERSION_CODE) {
                                showContent(queryDocumentSnapshots, false);
                            } else {
                                showContent(queryDocumentSnapshots, true);
                                PreferenceController.getInstance(getApplicationContext()).setVersion((int) (long) fbVersion);
                            }
                        }
                    }
                }).addOnFailureListener(e -> Log.v(TAG, "getFirebaseAppVersion: " + e));
    }

    public void onFailure(@NonNull Exception e) {
        Snackbar.make(findViewById(android.R.id.content), "Something went wrong!!", Snackbar.LENGTH_SHORT);
    }
}