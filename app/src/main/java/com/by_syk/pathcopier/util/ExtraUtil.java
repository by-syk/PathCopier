package com.by_syk.pathcopier.util;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.URLSpan;

/**
 * @author By_syk
 */
public class ExtraUtil {
    @TargetApi(11)
    @SuppressWarnings("deprecation")
    public static void copy2Clipboard(Context context, String text) {
        if (text == null) {
            return;
        }

        if (C.SDK >= 11) {
            ClipboardManager clipboardManager = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("path", text);
            clipboardManager.setPrimaryClip(clipData);
        } else {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(text);
        }
    }

    /**
     * Set the enabled setting for a package component (activity, receiver, service, provider).
     * This setting will override any enabled state which may have been set
     * by the component in its manifest.
     *
     * @param componentName The component to enable
     * @param is_hide
     */
    public static void hideComponent(Context context, ComponentName componentName, boolean is_hide) {
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(componentName,
                is_hide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static String getVerName(Context context) {
        String verName = "1.0";

        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            verName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "v" + verName;
    }
    /**
     * @param message 包含空格隔开或独立成行的完整链接，如：
     *                ……
     *                https://github.com/kenglxn/GRGen
     *                ……
     */
    public static SpannableString getLinkableDialogMessage(String message) {
        SpannableString spannableString = new SpannableString(message);

        int temp_pos;
        int temp_end = 0;
        int temp_end2;
        while (temp_end < message.length()) {
            temp_pos = message.indexOf("http", temp_end);
            if (temp_pos < 0) {
                break;
            }

            temp_end = message.indexOf(" ", temp_pos);
            temp_end2 = message.indexOf("\n", temp_pos);
            if (temp_end2 > 0 && temp_end2 < temp_end) {
                temp_end = temp_end2;
            }

            if (temp_end <= 0) {
                temp_end = message.length();
            }
            spannableString.setSpan(new URLSpan(message.substring(temp_pos, temp_end)),
                    temp_pos, temp_end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

}
