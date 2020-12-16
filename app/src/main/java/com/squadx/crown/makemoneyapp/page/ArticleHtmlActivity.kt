package com.squadx.crown.makemoneyapp.page

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.squadx.crown.makemoneyapp.R
import com.squadx.crown.makemoneyapp.databinding.ActivityArticleBinding
import com.squadx.crown.makemoneyapp.model.ArticleHtml

class ArticleHtmlActivity : AppCompatActivity() {
    companion object {
        const val ARTICLE_HTML: String = "ARTICLE_HTML"
        private val TAG = ArticleHtmlActivity::class.java.name
    }

    private var article: ArticleHtml? = null
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var interstitialAd: InterstitialAd
    private lateinit var binding: ActivityArticleBinding
    private var webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (binding.loadingPbar.visibility == View.GONE)
                binding.loadingPbar.visibility = View.VISIBLE
            if (newProgress == 100) binding.loadingPbar.visibility = View.GONE
        }
    }
    private var webViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return if (Uri.parse(url).scheme == "market") {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    val host = view?.context as Activity
                    host.startActivity(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    val uri = Uri.parse(url)
                    view?.loadUrl("http://play.google.com/store/apps/" + uri.host + "?" + uri.query)
                    false
                }
            } else false
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.webview.settings.javaScriptEnabled = true

        MobileAds.initialize(this) {}

        val articleJson: String? = intent.getStringExtra(ARTICLE_HTML)
        if (articleJson != null && articleJson.isNotEmpty()) {
            article = Gson().fromJson(articleJson, ArticleHtml::class.java)
            showArticle()
        }

        binding.bannerContentAd.loadAd(AdRequest.Builder().build())

        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(R.string.interstitial_content)
        interstitialAd.loadAd(AdRequest.Builder().build())
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

        binding.webview.webViewClient = webViewClient
        binding.webview.webChromeClient = webChromeClient
    }

    private fun showArticle() {
        supportActionBar?.title = article?.title
        binding.webview.loadData(article?.htmlContent, "text/html", "UTF-8")
    }

    override fun onStart() {
        super.onStart()
        showArticle()
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                if (interstitialAd.isLoaded) interstitialAd.show()
                else interstitialAd.loadAd(AdRequest.Builder().build())
                handler.postDelayed(this, 72000)
            }
        }
        handler.postDelayed(runnable, 72000)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (binding.webview.canGoBack()) binding.webview.goBack()
                    else finish()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}