package com.happylee.mydemo.activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.happylee.mydemo.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebViewActivity extends AppCompatActivity {
    WebView webView;
    Button toastAtWebViewButton;

    class JSInterface {
        @JavascriptInterface
        public void showToast(String arg){
            Toast.makeText(WebViewActivity.this,arg,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.web_view);
        //配置Javascript接口
        webView.addJavascriptInterface(new JSInterface (),"Android");
        //配置WebView为允许使用JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(WebViewActivity.this);
                b.setTitle("");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
        });

        //从一个网页跳另一个网页，只在当前WebView显示，而不是打开系统浏览器
        webView.setWebViewClient(new WebViewClient(){

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            //当WebView加载新URL时会回调此方法，可在这拦截WebView加载
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Toast.makeText(WebViewActivity.this, "这是在URL加载器拦截URL，并阻止它加载", Toast.LENGTH_SHORT).show();

                String url = request.getUrl().toString();
                //打开的网页包含www.baidu.com的url调用系统浏览器打开
                if (url.contains("www.baidu.com")) {
                    //调用系统默认浏览器处理url
                    view.stopLoading();
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    //返回true代表取消本WebView对该网页的加载
                    return true;
                }
                return true;
                //return false;
            }

            @Nullable
            @Override
            //响应实体时回调此方法，可在这拦截WebView加载
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                /*
                String htmlPage = "<html>\n" +
                        "<title>千度</title>\n" +
                        "<body>\n" +
                        "<a href=\"www.taobao.com\">千度</a>,比百度知道的多10倍\n" +
                        "</body>\n" +
                        "<html>";
                WebResourceResponse webResourceResponse = new WebResourceResponse("text/html", "utf-8", new ByteArrayInputStream(htmlPage.getBytes()));
                return webResourceResponse;
                 */

                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL("https://www.taobao.com/");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(10 * 1000);
                    httpURLConnection.setReadTimeout(40 * 1000);
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                WebResourceResponse webResourceResponse = null;
                webResourceResponse = new WebResourceResponse("text/html", "utf-8", new ByteArrayInputStream(stringBuilder.toString().getBytes()));
                return null;
                //return webResourceResponse;
                //return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //Toast.makeText(WebViewActivity.this, "页面加载前会调用此方法", Toast.LENGTH_SHORT).show();
                //super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //Toast.makeText(WebViewActivity.this, "页面加载后会调用此方法", Toast.LENGTH_SHORT).show();
            }
        });
        //从Android 9.0（API级别28）开始，默认情况下禁用明文支持。
        //因此http的url均无法在WebView中加载
        //webView.loadUrl("https://www.baidu.com");
        webView.loadUrl("file:///android_asset/login_html/index.html");

        toastAtWebViewButton = findViewById(R.id.web_view_button);
        toastAtWebViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:toast()");
                webView.evaluateJavascript("javascript:toast()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {

                    }
                });
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        //在 Activity 销毁的时候，可以先移除 WebView，再销毁 WebView，最后置空。
        if (webView != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}