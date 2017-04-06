package com.cmu.ece.build18.firstapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import android.widget.TextView;
import android.graphics.Matrix;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

/**
 * Created by ameya on 4/6/17.
 */

public class ImageProcessingCallback {

    private Context context;
    private TextView displayTextView;

    public ImageProcessingCallback(Context context, TextView displayTextView) {
        this.context=context;
        this.displayTextView=displayTextView;
    }

    public void imageTextDetectTask(Bitmap imgBitmap) {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // TODO: Set the TextRecognizer's Processor.

        // TODO: Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            Log.w("TAG", "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);

            //TODO low storage handling
//            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

//            if (hasLowStorage) {
//                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
//                Log.w("TAG", getString(R.string.low_storage_error));
//            }
        }

        Frame frame = new Frame.Builder().setBitmap(imgBitmap).build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);
        //clear displaytextview
        displayTextView.setText("");
        if(textBlocks.size() < 1) {
            showToast("No text detected.");
            displayTextView.setText("No text detected.");
            return;
        }

        for(int i=0; i<textBlocks.size(); i++) {

            TextBlock thisTextBlock = textBlocks.get(i);
            if (thisTextBlock == null) {
                displayTextView.append("<null>");
                continue;
            }
            displayTextView.append(textBlocks.get(i).getValue() + "\n");
        }
    }


    /**
     * Check for barcodes
     * @param imgBitmap
     * @return
     */
    public String imageBarcodeTask(Bitmap imgBitmap) {
        BarcodeDetector detector =
                new BarcodeDetector.Builder(context)
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();

        if(!detector.isOperational()){
            showToast("Could not set up the detector!");
            return "";
        }

        Frame frame = new Frame.Builder().setBitmap(imgBitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);
        Barcode thisCode=null;
        String parsedValues = "";
        if(barcodes.size() < 1) {
            showToast("No barcodes found.");
        }

        for(int i=0; i<barcodes.size(); i++) {
            thisCode = barcodes.valueAt(i);
            showToast(thisCode.rawValue);
            parsedValues += "\n" + thisCode.rawValue;

        }

        showToast("Barcodes: " + parsedValues, Toast.LENGTH_LONG);
        return parsedValues;
    }

    private void showToast(String msg) {

        showToast(msg, Toast.LENGTH_SHORT);
    }

    private void showToast(String msg, int duration) {

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, true);
    }

}
