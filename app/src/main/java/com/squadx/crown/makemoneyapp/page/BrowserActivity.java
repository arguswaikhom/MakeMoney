package com.squadx.crown.makemoneyapp.page;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.model.LiUrl;
import com.squadx.crown.makemoneyapp.model.Primary;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledExecutorService;

public class BrowserActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String LOG_TAG = BrowserActivity.class.getSimpleName();
    public static final String TAG_ITEM = "item";
    public static final String TAG_ITEM_V2 = "item_v2";

    private String link;
    private int mProgress;
    private String mCurrentUrl;
    private WebView webView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeLayout;
    private InterstitialAd mInterstitialAd;
    private ScheduledExecutorService scheduler;
    private Handler handler;
    private Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView mAdView = findViewById(R.id.av_ab_banner_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isConnected()) {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException e) {
            Log.e("WebBrowser", " ", e);
        }

        getSupportActionBar().setElevation(0);

        Intent intent = getIntent();
        if (intent.hasExtra(TAG_ITEM_V2)) {
            LiUrl obj = new Gson().fromJson(intent.getStringExtra(TAG_ITEM_V2), LiUrl.class);
            link = obj.getUrl();
            getSupportActionBar().setTitle(obj.getTitle());
        } else {
            Primary obj = (Primary) intent.getParcelableExtra(TAG_ITEM);
            link = obj.getLink();
            getSupportActionBar().setTitle(obj.getTitle());
        }

        //mTextViewConfess = findViewById(R.id.textView_confess);
        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        swipeLayout = findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        //webView.requestFocusFromTouch();
        //webView.requestFocus();
        //webView.reload();

        setProgressBarVisibility(true);

        final Activity activity = this;
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                mProgress = progress;
                if (isProgressDone()) {
                    setTitle(getString(R.string.app_name));
                    progressBar.setVisibility(View.GONE);
                }

            }
        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mCurrentUrl = url;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mCurrentUrl = url;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getScheme().equals("market")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        Activity host = (Activity) view.getContext();
                        host.startActivity(intent);
                        return true;
                    } catch (ActivityNotFoundException e) {
                        Uri uri = Uri.parse(url);
                        view.loadUrl("http://play.google.com/store/apps/" + uri.getHost() + "?" + uri.getQuery());
                        return false;
                    }

                }
                return false;
            }
        });
        webView.loadUrl(link);
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d(LOG_TAG, "Interstitial not loaded");
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
                handler.postDelayed(this, 72000);
            }
        };
        handler.postDelayed(runnable, 72000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null).invoke(webView, (Object[]) null);
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(webView, (Object[]) null);

        } catch (ClassNotFoundException e) {
            Log.v(LOG_TAG, "ClassNotFoundException: ", e);
        } catch (NoSuchMethodException e) {
            Log.v(LOG_TAG, "NoSuchMethodException: ", e);
        } catch (InvocationTargetException e) {
            Log.v(LOG_TAG, "InvocationTargetException: ", e);
        } catch (IllegalAccessException e) {
            Log.v(LOG_TAG, "IllegalAccessException: ", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onRefresh() {
        webView.loadUrl(mCurrentUrl);
        if (isProgressDone()) {
            new Handler().postDelayed(() -> swipeLayout.setRefreshing(false), 1000);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                webView.loadUrl(link);
                return true;

            case R.id.action_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                try {
                    intent.setData(Uri.parse(mCurrentUrl));
                    startActivity(intent);
                } catch (NullPointerException e) {
                    Log.v(LOG_TAG, "Current URL : " + String.valueOf(mCurrentUrl == null), e);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isProgressDone() {
        return mProgress == 100;
    }
}
