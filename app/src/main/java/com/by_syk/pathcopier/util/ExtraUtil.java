package com.by_syk.pathcopier.util;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

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
}
