package com.by_syk.pathcopier;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.by_syk.pathcopier.util.ExtraUtil;

/**
 * Created by By_syk on 2016-04-23.
 */
public class HelloActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        findViewById(R.id.tv_path).setVisibility(View.GONE);

        TextView tvText = (TextView) findViewById(R.id.tv_text);
        tvText.setVisibility(View.VISIBLE);

        String message = getString(R.string.about_desc, ExtraUtil.getVerName(this));
        tvText.setText(ExtraUtil.getLinkableDialogMessage(message));
        tvText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onDestroy() {
        ExtraUtil.hideComponent(this, getComponentName(), true);

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
