package com.by_syk.lib.uri;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * What's URI?
 * <p/>
 * URI = Scheme + (Authority) + Path + (Fragment)
 * <p/>
 * To know more, please visit
 *     http://developer.android.com/reference/android/net/Uri.html
 *
 * @author By_syk
 */
public class UriAnalyser {
    /**
     * Get raw file path of the URI.
     *
     * @param uri The file uri.
     * @return The raw path of the file.
     */
    public static String getRawPath(Uri uri) {
        if (uri == null) {
            return null;
        }

        // Or URLDecoder.decode(uri.toString())
        return Uri.decode(uri.toString());
    }

    /**
     * Get raw file path of the URI with highlight color.
     * <p/>And the scheme is red, the authority is green and the path is blue.
     *
     * @param uri The file uri.
     * @param is_light_bg If true, the text is showing on light background, or on dark background.
     * @return The raw path with highlight color.
     */
    public static SpannableStringBuilder getRawPathHighlighted(Uri uri, boolean is_light_bg) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String uriPath = getRawPath(uri);
        if (TextUtils.isEmpty(uriPath)) {
            return ssb;
        }
        ssb.append(uriPath);

        int[] colors_highlight = is_light_bg ? C.HL_LB : C.HL_DB;

        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        String path = uri.getPath();

        int index1 = 0;
        int index2 = uriPath.length();
        ssb.setSpan(new ForegroundColorSpan(colors_highlight[3]), index1, index2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (!TextUtils.isEmpty(scheme)) {
            index2 = scheme.length();
            ssb.setSpan(new ForegroundColorSpan(colors_highlight[0]), index1, index2,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            index1 = index2 + 3;
        }
        if (!TextUtils.isEmpty(authority)) {
            index2 = index1 + authority.length();
            ssb.setSpan(new ForegroundColorSpan(colors_highlight[1]), index1, index2,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            index1 = index2;
        }
        if (!TextUtils.isEmpty(path)) {
            index2 = index1 + path.length();
            ssb.setSpan(new ForegroundColorSpan(colors_highlight[2]), index1, index2,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ssb;
    }

    /**
     * Get raw file path of the URI with highlight color.
     * <p/>And the scheme is red, the authority is green and the path is blue.
     *
     * @param uri The file uri.
     * @return The raw path with highlight color.
     */
    public static SpannableStringBuilder getRawPathHighlighted(Uri uri) {
        return getRawPathHighlighted(uri, false);
    }

    /**
     * Get real file path of the URI.
     *
     * @param context The context to use.
     * @param uri The file uri.
     * @return The real path of the file.
     *
     * <p>Requires Permission: {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}
     */
    @TargetApi(19)
    public static String getRealPath(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }

        String result = null;

        final String SCHEME = uri.getScheme();

        if (C.SDK >= 19 && DocumentsContract.isDocumentUri(context, uri)) { // DocumentProvider
            result = getRealPathFromDocumentUri(context, uri);
        } else if ("content".equalsIgnoreCase(SCHEME)) { // MediaStore (and general)
            result = getRealPathFromGeneralUri(context, uri);
        } else if ("file".equalsIgnoreCase(SCHEME)) { // File
            result = getRealPathFromFileUri(uri);
        }

        return result;
    }

    /**
     * Get an accessible file from the URI.
     * If raw file is not accessible, make a duplication.
     *
     * @param context The context to use.
     * @param uri The file uri.
     * @return The accessible file.
     *
     * <p>Requires Permission: {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}
     */
    public static File getAccessibleFile(Context context, Uri uri) {
        String path = getRealPath(context, uri);
        if (path != null && (new File(path)).exists()) {
            return new File(path);
        }

        return extractFile(context, uri, null);
    }

    @TargetApi(19)
    private static String getRealPathFromDocumentUri(Context context, Uri uri) {
        String result = null;

        final String DOCUMENT_ID = DocumentsContract.getDocumentId(uri);

        final String AUTHORITY = uri.getAuthority();
        switch (AUTHORITY) {
            /*
             * ExternalStorageProvider
             *
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
                    /*
                     * Why not use PARAS[1]?
                     * To avoid making error for such path:
                     * /storage/emulated/0/App/OSBuild:v1.0.apk
                     */
                    result = String.format("%1$s/%2$s",
                            Environment.getExternalStorageDirectory(),
                            DOCUMENT_ID.substring(PARAS[0].length() + 1));
                }/* else {
                    /*
                     * Like this:
                     *     content://com.android.externalstorage.documents/76DD-33F3:APP/OSBuild.apk
                     * Maybe it's a file from OTG disk.
                     * Sometimes it occurs when the device is equipped with third OS.
                     *
                    result = String.format(Locale.US, "/storage/%1$s/%2$s",
                            PARAS[0], DOCUMENT_ID.substring(PARAS[0].length() + 1));
                }*/
                break;
            }
            /*
             * DownloadsProvider
             *
             * Like this:
             *     content://com.android.providers.downloads.documents/document/80
             * ->  /storage/emulated/0/Download/OSBuild.apk
             */
            case "com.android.providers.downloads.documents": {
                Uri contentUri = ContentUris
                        .withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(DOCUMENT_ID));
                result = getDataColumn(context, contentUri, null, null);
                break;
            }
            /*
             * MediaProvider
             *
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

        return result;
    }

    private static String getRealPathFromGeneralUri(Context context, Uri uri) {
        String result = null;

        final String AUTHORITY = uri.getAuthority();
        switch (AUTHORITY) {
            /*
             * Like this:
             *     content://com.google.android.bluetooth.fileprovider/bluetooth/bluetooth/OSBuild.apk
             * ->  /storage/emulated/0/Bluetooth/OSBuild.apk
             */
            case "com.google.android.bluetooth.fileprovider": {
                final String PATH = uri.getPath();
                if (PATH.startsWith("/bluetooth")) {
                    return PATH.replaceFirst("/bluetooth",
                            Environment.getExternalStorageDirectory().getPath());
                }
                break;
            }
            /*
             * Like this:
             *     content://com.android.email.provider/attachment/cachedFile?filePath=/data/user/0/com.android.email/cache/xxx.attachment
             * ->  /data/user/0/com.android.email/cache/xxx.attachment
             */
            case "com.android.email.provider":
                result = uri.getQueryParameter("filePath");
                break;
            /*
             * Like this:
             *     content://com.android.email.attachmentprovider/2/80/RAW
             * ->  /data/user/0/com.android.email/cache/xxx.attachment
             */
            /*case "com.android.email.attachmentprovider":
                break;*/
            /*
             * Like this:
             *     content://com.android.contacts/contacts/as_vcard/3585r288-1A1A1C1CBFC1BFBB14
             * ->
             */
            /*case "com.android.contacts":
                break;*/
            /*
             * From Chrome
             *
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
             * From Chrome
             *
             * Like this:
             *     content://com.android.chrome.FileProvider/images/screenshot/14614253333517-1664269127.jpg
             * ->  /data/data/com.android.chrome/files/images/screenshot/14614253333517-1664269127.jpg
             */
            case "com.android.chrome.FileProvider":
                result = "/data/data/com.android.chrome/files" + uri.getPath();
                break;
            /*
             * From Chrome Dev
             *
             * Like this:
             *     content://com.chrome.dev.FileProvider/images/screenshot/1461424897044467528231.jpg
             * ->
             */
            /*case "com.chrome.dev.FileProvider":
                break;*/
            /*
             * From Google Photos
             * Return the remote address
             *
             * Like this:
             *
             * ->
             */
            case "com.google.android.apps.photos.content":
                result = uri.getLastPathSegment();
                break;
            /*
             * From QuickPic
             *
             * Like this:
             *     content://media/external/images/media/48432
             * ->  /storage/emulated/0/Pictures/ic_launcher.png
             *
             * From Google Photos
             *
             * Like this:
             *     content://com.google.android.apps.photos.contentprovider/-1/1/content://media/external/images/media/4939/ORIGINAL/NONE/445782129
             * ->  /storage/emulated/0/Pictures/ic_launcher.png
             */
            default:
                result = getDataColumn(context, uri, null, null);
        }

        return result;
    }

    private static String getRealPathFromFileUri(Uri uri) {
        /*
         * Like this:
         *     file:///storage/emulated/0/Download/OSBuild.apk
         * ->  /storage/emulated/0/Download/OSBuild.apk
         */
        return uri.getPath();
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        if (uri == null) {
            return null;
        }

        String result = null;

        final String COLUMN = "_data";

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                    .query(uri, new String[]{COLUMN}, selection, selectionArgs, null);
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

    /**
     * That's a custom URI provided by third party app.
     * You should check their APIs about the permissions to work with their content URIs.
     * Normally you can use
     * ContentResolver.openInputStream(android.net.Uri)
     * to get the InputStream from that URI.
     */
    @TargetApi(8)
    private static File extractFile(Context context, Uri uri, File file) {
        if (uri == null) {
            return null;
        }

        boolean is_ok = false;

        /*String fileName;
        if (file == null) {
            fileName = uri.getLastPathSegment();
        } else {
            fileName = file.getName();
        }

        File tempFile = new File(C.SDK >= 8 ? context.getExternalCacheDir()
                : Environment.getDownloadCacheDirectory(),
                TextUtils.isEmpty(fileName) ? "temp" : fileName);*/

        if (file == null) {
            file = new File(C.SDK >= 8 ? context.getExternalCacheDir()
                    : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "temp");
        }

        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            is_ok = ExtraUtil.copyFile(inputStream, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return is_ok ? file : null;
    }

    /*/**
     * @param uri The file uri.
     * @return
     *
    public static String getUriDetails(Uri uri) {
        if (uri == null) {
            return "";
        }

        StringBuilder stringBuffer = new StringBuilder();

        // Or URLDecoder.decode(uri.toString())
        stringBuffer.append("Uri: ").append(Uri.decode(uri.toString()));
        stringBuffer.append("\n\nScheme: ").append(uri.getScheme());
        stringBuffer.append("\n\nAuthority: ").append(uri.getAuthority());
        stringBuffer.append("\n\n* User info: ").append(uri.getUserInfo());
        stringBuffer.append("\n\n* Host: ").append(uri.getHost());
        stringBuffer.append("\n\n* Port: ").append(uri.getPort());
        stringBuffer.append("\n\nPath: ").append(uri.getPath());
        stringBuffer.append("\n\n* Path segments: ").append(uri.getPathSegments());
        stringBuffer.append("\n\nQuery: ").append(uri.getQuery());
        stringBuffer.append("\n\nFragment: ").append(uri.getFragment());

        return stringBuffer.toString();
    }*/
}