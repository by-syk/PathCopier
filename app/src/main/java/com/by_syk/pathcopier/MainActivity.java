package com.by_syk.pathcopier;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;

public class MainActivity extends Activity {
    private TextView tvPath;
    private TextView tvText;

    private String uriPath = "";
    private String filePath = "";

    private final int CLOSING_TIME = 1800;

    private boolean delay_closing = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /*if (delay_closing) {
                delay_closing = false;
                handler.postDelayed(this, CLOSING_TIME * 2);
            } else {
                finish();
            }*/

            if (!delay_closing) {
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

    private void init() {
        tvPath = (TextView) findViewById(R.id.tv_path);
        tvPath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View p1) {
                delay_closing = true;

                if (!TextUtils.isEmpty(uriPath)) {
                    tvPath.setText(String.format("%1$s\n\n->\n\n%2$s",
                            uriPath, TextUtils.isEmpty(filePath) ? "null" : filePath));
                }
            }
        });

        tvText = (TextView) findViewById(R.id.tv_text);
        tvText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View p1) {
                delay_closing = true;
            }
        });

        // Listening scrolling to avoid disappearing when scrolling.
        findViewById(R.id.sv_content).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View p1, MotionEvent p2) {
                delay_closing = true;

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
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            copy((String) null);
        } else {
            final boolean IS_OK = copy((Uri) bundle.get(Intent.EXTRA_STREAM));
            if (!IS_OK) {
                // Maybe the object is text instead of file uri.
                copy((String) bundle.get(Intent.EXTRA_TEXT));
            }
        }

        handler.postDelayed(runnable, CLOSING_TIME);
    }

    private boolean copy(Uri uri) {
        if (uri == null) {
            //Toast.makeText(this, R.string.toast_failed, Toast.LENGTH_SHORT).show();
            return false;
        }

        uriPath = Uri.decode(uri.toString());
        Log.i(C.LOG_TAG, uriPath);

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
