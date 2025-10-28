//package com.uworld.careerjourney.student_map
//
//import android.os.Bundle
//import android.view.View
//import android.webkit.JavascriptInterface
//import android.webkit.WebResourceRequest
//import android.webkit.WebResourceResponse
//import android.webkit.WebView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.webkit.WebViewAssetLoader
//import androidx.webkit.WebViewClientCompat
//import kotlin.apply
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var webView: WebView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.activity_main)
//
//        setUpWebView()
//    }
//
//    private fun setUpWebView() {
//        webView = findViewById(R.id.webView)
//
//        webView.apply {
//            val assetLoader = WebViewAssetLoader.Builder()
//                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this@MainActivity))
//                .build()
//
//            webView.webViewClient = object : WebViewClientCompat() {
//                override fun shouldInterceptRequest(
//                    view: WebView,
//                    request: WebResourceRequest
//                ): WebResourceResponse? {
//                    return assetLoader.shouldInterceptRequest(request.url)
//                }
//            }
//
//            settings.apply {
//                javaScriptEnabled = true
//                allowFileAccess = true
//                allowFileAccessFromFileURLs = true
//                allowUniversalAccessFromFileURLs = true
//                domStorageEnabled = true
//            }
//
//            clearCache(true)
//            clearHistory()
//
//            addJavascriptInterface(MapWVBridgeInterface(this@MainActivity), "activityBridge")
//            setLayerType(View.LAYER_TYPE_HARDWARE, null)
//
//
////            loadUrl("https://appassets.androidplatform.net/assets/interactiveMap/interactive_map_1.html")
//            loadUrl("https://appassets.androidplatform.net/assets/interactiveMap/interactive_map_2.html")
//        }
//
//    }
//
//    fun showCheckPointPopUp(nodeId: String) {
////        setContent {
////            GamifiedMap2Theme {
////                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
////                    CheckPointPopUp(
////                    )
////                }
////            }
////        }
//    }
//
////    @Composable
////    fun CheckPointPopUp() {
////        Box(
////            modifier = Modifier.fillMaxSize()
////                .background(Color(0x80000000))
////        ) {
////
////        }
////    }
//}
//
//class MapWVBridgeInterface(val activity: MainActivity?) {
//
//    @JavascriptInterface
//    fun handleOnCheckPointClick(nodeId: String) {
//        println("Checkpoint clicked: $nodeId")
//        activity?.runOnUiThread {
//            activity.showCheckPointPopUp(nodeId)
//        }
//    }
//}