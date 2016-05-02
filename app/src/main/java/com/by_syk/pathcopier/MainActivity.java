package com.by_syk.pathcopier;

import com.by_syk.lib.storage.SP;
import com.by_syk.lib.uri.UriAnalyser;
import com.by_syk.pathcopier.util.C;
import com.by_syk.pathcopier.util.ExtraUtil;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;

public class MainActivity extends Activity {
    private SP sp = null;

    private TextView tvPath;
    private TextView tvText;

    //private String uriPath = "";
    private SpannableStringBuilder ssbUriPath = new SpannableStringBuilder();
    private String filePath = "";

    // Activity is running.
    private boolean is_running = true;

    // Stop closing the Activity automatically.
    private boolean is_tapped = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /*if (is_tapped) {
                is_tapped = false;
                handler.postDelayed(this, C.CLOSING_TIME * 2);
            } else {
                finish();
            }*/

            if (is_running && !is_tapped) {
                // Tell user how to view more info.
                if (!sp.getBoolean("tapped") && sp.getLaunchTimes() / 3 == 1) {
                    Toast.makeText(MainActivity.this, R.string.toast_to_tap,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                finish();
            }
        }
    };
    private Handler handler = new Handler();

    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        // Ask for permission: READ_EXTERNAL_STORAGE
        if (C.SDK >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            /*if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                
            }*/

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            analyse();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        is_running = false;
    }

    private void init() {
        sp = new SP(this);

        tvPath = (TextView) findViewById(R.id.tv_path);
        tvPath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View p1) {
                is_tapped = true;

                sp.save("tapped", true);

                /*if (!TextUtils.isEmpty(uriPath)) {
                    tvPath.setText(String.format("%1$s\n\n->\n\n%2$s",
                            uriPath, TextUtils.isEmpty(filePath) ? "null" : filePath));
                }*/
                if (ssbUriPath.length() > 0) {
                    tvPath.setText(ssbUriPath);
                    tvPath.append(String.format("\n\n->\n\n%1$s",
                            TextUtils.isEmpty(filePath) ? "null" : filePath));
                }
            }
        });

        tvText = (TextView) findViewById(R.id.tv_text);
        tvText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View p1) {
                is_tapped = true;
            }
        });

        // Listening scrolling to avoid disappearing when scrolling.
        findViewById(R.id.sv_content).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View p1, MotionEvent p2) {
                is_tapped = true;

                return false;
            }
        });
    }

    /**
     * API 23+
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /*if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            analyse();
        }*/
        analyse();
    }

    private void analyse() {
        Intent intent = getIntent();
        switch (intent.getAction()) {
            case Intent.ACTION_SEND: {
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    copy((String) null);
                } else {
                    final boolean IS_OK = copy((Uri) bundle.get(Intent.EXTRA_STREAM));
                    if (!IS_OK) {
                        // Maybe the object is text instead of file uri.
                        copy((String) bundle.get(Intent.EXTRA_TEXT));
                    }
                }
            }
            // DEBUG
            /*case Intent.ACTION_VIEW:
                copy(intent.getData());*/
        }

        handler.postDelayed(runnable, C.CLOSING_TIME);
    }

    private boolean copy(Uri uri) {
        if (uri == null) {
            //Toast.makeText(this, R.string.toast_failed, Toast.LENGTH_SHORT).show();
            return false;
        }

        //uriPath = UriAnalyser.getRawPath(uri);
        //Log.i(C.LOG_TAG, uriPath);
        ssbUriPath = UriAnalyser.getRawPathHighlighted(uri);
        Log.i(C.LOG_TAG, ssbUriPath.toString());

        filePath = UriAnalyser.getRealPath(this, uri);
        if (TextUtils.isEmpty(filePath)) {
            //Toast.makeText(this, R.string.toast_failed, Toast.LENGTH_SHORT).show();
            return false;
        }
        Log.i(C.LOG_TAG, filePath);

        tvPath.setText(filePath);

        ExtraUtil.copy2Clipboard(this, filePath);

        Toast.makeText(this, R.string.toast_copied_path, Toast.LENGTH_SHORT).show();

        return true;
    }

    private boolean copy(String text) {
        if (text == null) {
            Toast.makeText(this, R.string.toast_failed, Toast.LENGTH_SHORT).show();
            return false;
        }

        tvPath.setVisibility(View.GONE);
        tvText.setVisibility(View.VISIBLE);
        tvText.setText(text);

        ExtraUtil.copy2Clipboard(this, text);

        Toast.makeText(this, R.string.toast_copied_text, Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
