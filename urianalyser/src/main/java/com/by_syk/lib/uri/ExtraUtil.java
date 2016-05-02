package com.by_syk.lib.uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by By_syk on 2016-04-26.
 */
class ExtraUtil {
    public static boolean copyFile(InputStream inputStream, File targetFile) {
        if (targetFile == null) {
            return false;
        }

        boolean result = false;

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(targetFile);
            int temp_len;
            byte[] buffer = new byte[1024];
            while ((temp_len = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, temp_len);
            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
}
