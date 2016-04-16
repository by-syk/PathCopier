package com.by_syk.lib.uri;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * @author By_syk
 */
public class UriAnalyser {
    private final static int SDK = Build.VERSION.SDK_INT;

    /**
     * Since Marshmallow, it failed to read such uris like this:
     * content://downloads/my_downloads/2802
     * content://com.google.android.bluetooth.fileprovider/bluetooth/bluetooth/OSBuild.apk
     * <p/>
     * In some devices, it failed to read such uri like this (from OTG):
     * content://com.android.externalstorage.documents/76DD-33F3:APP/OSBuild.apk
     *
     * @param context The context to use.
     * @param uri The file uri.
     * @return The real path of the file.
     */
    @TargetApi(19)
    public static String getRealPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        String result = null;

        final String AUTHORITY = uri.getAuthority();
        final String SCHEME = uri.getScheme();

        if (SDK >= 19 && DocumentsContract.isDocumentUri(context, uri)) {
            final String DOCUMENT_ID = DocumentsContract.getDocumentId(uri);

            switch (AUTHORITY) {
                /*
                 * Like this:
                 *     content://com.android.externalstorage.documents/document/primary:App/OSBuild.apk
                 * ->  /storage/emulated/0/App/OSBuild.apk
                 */
                case "com.android.externalstorage.documents": {
                    final String[] PARAS = DOCUMENT_ID.split(":");
                    if (PARAS.length == 1) {
                        break;
                    }

                    if (PARAS[0].equalsIgnoreCase("primary")) {
                        result = String.format("%1$s/%2$s",
                                Environment.getExternalStorageDirectory(),
                                DOCUMENT_ID.substring(PARAS[0].length() + 1));
                    }/* else {
                        //Maybe it's a file from OTG device.
                        //DEBUG
                        result = String.format("/storage/%1$s/%2$s",
                        PARAS[0], DOCUMENT_ID.substring(PARAS[0].length() + 1));
                    }*/
                    break;
                }
                /*
                 * Like this:
                 *     content://com.android.providers.downloads.documents/document/80
                 * ->  /storage/emulated/0/Download/OSBuild.apk
                 */
                case "com.android.providers.downloads.documents": {
                    final Uri URI = ContentUris.withAppendedId(Uri
                                    .parse("content://downloads/public_downloads"),
                            Long.valueOf(DOCUMENT_ID));
                    result = getDataColumn(context, URI, null, null);
                    break;
                }
                /*
                 * Like this:
                 *     content://com.android.providers.media.documents/document/audio:80
                 * ->  /storage/emulated/0/Movies/Home.mp3
                 */
                case "com.android.providers.media.documents": {
                    final String[] PARAS = DOCUMENT_ID.split(":");
                    if (PARAS.length == 1) {
                        break;
                    }

                    Uri contentUri = null;
                    switch (PARAS[0].toLowerCase()) {
                        case "image":
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "video":
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "audio":
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    result = getDataColumn(context, contentUri, "_id=?", new String[]{PARAS[1]});
                }
            }
        } else if ("content".equalsIgnoreCase(SCHEME)) {
            //DEBUG
            switch (AUTHORITY) {
                /*
                 * Like this:
                 *     content://com.google.android.bluetooth.fileprovider/bluetooth/bluetooth/OSBuild.apk
                 * ->  /storage/emulated/0/Bluetooth/OSBuild.apk
                 */
                case "com.google.android.bluetooth.fileprovider": {
                    final String PATH = uri.getPath();
                    if (PATH.startsWith("/bluetooth")) {
                        result = PATH.replaceFirst("/bluetooth",
                                Environment.getExternalStorageDirectory().getPath());
                    }
                    break;
                }
                /*
                 * Like this:
                 *     content://downloads/my_downloads/80
                 * ->  /storage/emulated/0/Download/OSBuild.apk
                 */
                case "downloads": {
                    uri = Uri.parse(uri.toString().replace("content://downloads/my_downloads/",
                            "content://downloads/public_downloads/"));
                    result = getDataColumn(context, uri, null, null);
                    break;
                }
                /*
                 * Like this:
                 *     content://com.android.email.provider/attachment/cachedFile?filePath=/data/user/0/com.android.email/cache/xxx.attachment
                 * ->  /data/user/0/com.android.email/cache/xxx.attachment
                 *
                 * But permission denied.
                 */
                case "com.android.email.provider":
                    result = uri.getQueryParameter("filePath");
                    break;
                /*
                 * Like this:
                 *     content://com.android.email.attachmentprovider/2/80/RAW
                 * ->  /data/user/0/com.android.email/cache/xxx.attachment
                 *
                 * But permission denied.
                 */
                case "com.android.email.attachmentprovider":
                    break;
                default:
                    result = getDataColumn(context, uri, null, null);
            }
        }
        /*
         * Like this:
         *     file:///storage/emulated/0/Download/OSBuild.apk
         * ->  /storage/emulated/0/Download/OSBuild.apk
         */
        else if ("file".equalsIgnoreCase(SCHEME)) {
            result = uri.getPath();
        }

        return result;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selection_args) {
        if (uri == null) {
            return null;
        }

        String result = null;

        final String COLUMN = "_data";

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{COLUMN}, selection, selection_args, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int COLUMN_INDEX = cursor.getColumnIndexOrThrow(COLUMN);
                result = cursor.getString(COLUMN_INDEX);
            }
        } catch (IllegalArgumentException | SecurityException e) {
            /*
             * IllegalArgumentException
             * "_data" does not exists.
             * Opening .apk file from FX (v4.0.6.0) - Network will cause this.
             */
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }
}