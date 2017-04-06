package com.cmu.ece.build18.firstapp;

import android.hardware.Camera;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

import android.provider.MediaStore.Files.FileColumns;
/**
 * Created by ameya on 4/6/17.
 */

public class SableyePictureCallback implements Camera.PictureCallback {

    public static final String TAG="PICCALLBACK";

    private File imgDirectoryPath;
    public SableyePictureCallback(File imgDirectoryPath) {
        this.imgDirectoryPath = imgDirectoryPath;

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.e(TAG, "onPictureTaken()");
        try {
            File imageFilePath = new File(imgDirectoryPath, "latest.jpeg");
            FileOutputStream fos = new FileOutputStream(imageFilePath);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }
}
