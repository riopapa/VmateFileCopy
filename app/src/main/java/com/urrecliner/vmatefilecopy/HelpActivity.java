package com.urrecliner.vmatefilecopy;

import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_help);
            WebView webView = findViewById(R.id.webView);
//            WebSettings settings = webView.getSettings();
//            settings.setJavaScriptEnabled(true);
            webView.loadUrl("file:///android_res/raw/help.html");
        }
}
