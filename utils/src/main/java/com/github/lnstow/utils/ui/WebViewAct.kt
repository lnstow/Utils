package com.github.lnstow.utils.ui

import android.content.Context
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.github.lnstow.utils.ext.LaunchParams
import com.github.lnstow.utils.ext.MATCH
import com.github.lnstow.utils.ext.VGLP
import com.github.lnstow.utils.ext.addView
import com.github.lnstow.utils.ext.getLp
import com.github.lnstow.utils.ext.newFrag
import com.github.lnstow.utils.ext.startAct

abstract class WebLpAbs(
    val url: String,
    val centerTitle: Boolean = true,
    val fixTitle: String? = null,
    val nextBtn: String? = null,
    val nextPage: WebLpAbs? = null,
) : LaunchParams {
    abstract fun newInstance(): () -> WebFragAbs
    fun openLink(ctx: Context = BaseAct.top) {
        ctx.startAct<WebViewAct>(this)
    }
}

class WebViewAct : FragWrapperActivity() {
    override fun initFrag(): Fragment {
        val lp = getLp<WebLpAbs>()
        return newFrag(lp.newInstance(), lp)
    }
}

open class WebFragAbs : PageBlockFragment() {
    private val lp by lazy { getLp<WebLpAbs>() }
    private lateinit var webView: WebView

    override fun LinearLayout.initPageBlock() {
        webView = addView(::WebView) {
            layoutParams = VGLP(MATCH, MATCH)

            // 明确为 WebView 启用硬件层，以避免渲染时序冲突
            // 解决在部分旧机型上 返回按钮消失
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            addJavascriptInterface(jsBridge, jsBridge.name)
            webViewClient = BaseWebViewClient()
            webChromeClient = BaseChromeClient()
            initWebView(this)
            loadUrl(lp.url)
        }
        if (com.github.lnstow.utils.ext.debug) WebView.setWebContentsDebuggingEnabled(true)
    }

    protected open val jsBridge: JsBridge = JsBridge()

    protected open inner class JsBridge {
        open val name: String = "JsBridge"
//        @JavascriptInterface
//        fun setExtraNavBtn(options: String) {
//            applyNavBtn(options.fromJson())
//        }
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface(jsBridge.name)
        super.onDestroy()
    }

    protected open fun initWebView(webView: WebView) {}
    protected open fun updateTitle(title: CharSequence?) {
        title ?: return
        tb?.titleTv?.text = lp.fixTitle ?: title
    }

    protected open inner class BaseChromeClient : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            updateTitle(title)
        }
    }

    protected open inner class BaseWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            updateTitle(view.title)
        }
    }
}