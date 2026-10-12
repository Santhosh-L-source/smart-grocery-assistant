package com.grocery.assistant.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    public static final String APP_URL = "https://smart-grocery-assistant.onrender.com/";
    private static final int PERMISSION_REQ_CODE = 101;

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout loadingLayout;
    private TextView loadingText;
    private LinearLayout errorLayout;
    private Button retryButton;

    private PermissionRequest pendingWebPermission;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingText = findViewById(R.id.loadingText);
        errorLayout = findViewById(R.id.errorLayout);
        retryButton = findViewById(R.id.retryButton);

        setupWebView();
        setupRefreshAndRetry();
        setupBackNavigation();

        requestHardwarePermissions();

        loadAppUrl();
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        CookieManager.getInstance().setAcceptCookie(true);

        String originalUA = settings.getUserAgentString();
        settings.setUserAgentString(originalUA + " SmartGroceryMobile/1.0");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                errorLayout.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                loadingLayout.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    swipeRefreshLayout.setRefreshing(false);
                    loadingLayout.setVisibility(View.GONE);
                    if (!isNetworkAvailable()) {
                        errorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                MainActivity.this.runOnUiThread(() -> {
                    pendingWebPermission = request;
                    request.grant(request.getResources());
                });
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 80) {
                    loadingLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupRefreshAndRetry() {
        swipeRefreshLayout.setColorSchemeResources(R.color.accent_green);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isNetworkAvailable()) {
                webView.reload();
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "இணைய இணைப்பு இல்லை (No Internet)", Toast.LENGTH_SHORT).show();
            }
        });

        retryButton.setOnClickListener(v -> {
            errorLayout.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.VISIBLE);
            loadingText.setText(R.string.waking_server);
            webView.loadUrl(APP_URL);
        });
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void loadAppUrl() {
        if (!isNetworkAvailable()) {
            loadingLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            return;
        }

        loadingLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        webView.loadUrl(APP_URL);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void requestHardwarePermissions() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        boolean needsReq = false;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needsReq = true;
                break;
            }
        }

        if (needsReq) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (pendingWebPermission != null) {
                pendingWebPermission.grant(pendingWebPermission.getResources());
                pendingWebPermission = null;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
