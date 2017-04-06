package com.cmu.ece.build18.firstapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.speech.tts.TextToSpeech;
import android.widget.ImageView;
import android.util.Log;

import android.provider.MediaStore.Files.FileColumns;
/**
 * Created by ameya on 4/6/17.
 */

public class SableyePictureCallback implements Camera.PictureCallback {

    public static final String TAG="PICCALLBACK";

    private File imgFilePath;
    private ImageView imageView;
    private ImageProcessingCallback imgProcessingCallback;
    private TextToSpeech textToSpeech;

    public SableyePictureCallback(File imgFilePath, ImageView imageView,
                                  ImageProcessingCallback imgProcessingCallback,
                                  TextToSpeech textToSpeech) {
        this.imgFilePath = imgFilePath;
        this.imageView=imageView;
        this.imgProcessingCallback=imgProcessingCallback;
        this.textToSpeech=textToSpeech;

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.e(TAG, "onPictureTaken()");

        //write to file
        try {
            FileOutputStream fos = new FileOutputStream(imgFilePath);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        //display file in ImageView
        Bitmap imgBitmap = BitmapFactory.
                decodeFile(imgFilePath.getAbsolutePath());

        if(imgBitmap == null) {
            Log.e("ERR!", "Bitmap null in onPictureTaken()");
            imageView.setBackgroundColor(66);
            return;
        }

        Bitmap rotatedBitmap = ImageProcessingCallback.rotateBitmap(imgBitmap, 90);
        imageView.setImageBitmap(rotatedBitmap);
        String textToSpeak = imgProcessingCallback.imageBarcodeTask(rotatedBitmap);
        textToSpeak += imgProcessingCallback.imageTextDetectTask(rotatedBitmap);
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
}
