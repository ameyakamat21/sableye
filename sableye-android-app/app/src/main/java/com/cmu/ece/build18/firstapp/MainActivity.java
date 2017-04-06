package com.cmu.ece.build18.firstapp;

import android.*;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ScrollView;
import android.net.Uri;
import android.content.pm.PackageManager;
import 	android.hardware.Camera;

//google play services imports
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import android.location.Location;
import android.util.Log;

//Vision
import com.google.android.gms.common.api.CommonStatusCodes;
//import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
//import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
//import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.widget.ImageView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import java.io.InputStream;
import java.io.FileNotFoundException;


public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    public static final String STATE_LATITUDE = "STATE_LAT";
    public static final String STATE_LONGITUDE = "STATE_LON";
    private GoogleApiClient mGoogleApiClient;
    private double currLatitude, currLongitude;
    public static final int PICK_IMAGE_REQUEST = 1;
    TextView displayTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize google play services interface
        displayTextView = (TextView) findViewById(R.id.textview);
        displayTextView.append("<No text yet>");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if(savedInstanceState != null) {
            currLatitude = savedInstanceState.getDouble(STATE_LATITUDE);
            currLongitude = savedInstanceState.getDouble(STATE_LONGITUDE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        for(int i=0; i<grantResults.length; i++) {
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                showToast("Did not get permission for " + permissions[i] + ":(");
                return;
            }
        }

        showToast("Got all permissions!");
    }


    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION", "Does not have location permission!");
            showToast("Does not have location permission!");


            String[] permisionsToRequest = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permisionsToRequest, 1);
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            currLatitude = mLastLocation.getLatitude();
            currLongitude = mLastLocation.getLongitude();

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("LOCERR", "!!!!!!!! The connection has failed!: " + result);
    }

    @Override
    public void onConnectionSuspended(int someInt) {
        Log.e("LOCERR", " !!!!!!!!  The connection was suspended.");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putDouble(STATE_LATITUDE, currLatitude);
        savedInstanceState.putDouble(STATE_LONGITUDE, currLongitude);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showToast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }


    public void getLocationBtnCallback(View view) {

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION", "Does not have location permission!");
            showToast("Does not have location permission!");


            String[] permisionsToRequest = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permisionsToRequest, 1);
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        Log.e("LOG", "mGoogleApiClient.isConnected(): " + mGoogleApiClient.isConnected());

        if (mLastLocation != null) {
            currLatitude = mLastLocation.getLatitude();
            currLongitude = mLastLocation.getLongitude();
            showToast("Last loc: (" + currLatitude + ", " + currLongitude + ")");
        } else  {
            showToast("Last location was null.");
            Log.e("LOCERR", "!!!!!!!! Last location was null.");
        }
        //showToast("(" + currLatitude + ", " + currLongitude + ")");
    }

    public void pickImageBtnCallback(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_IMAGE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    Log.e("URI", "Got URI: " + imageUri);
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ImageView imgView = (ImageView)findViewById(R.id.imgview);
                    imgView.setImageBitmap(selectedImage);
                    imageBarcodeTask(selectedImage);
                    imageTextDetectTask(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            showToast("Got unknown activity result");
            Log.e("ERROR", "Got unknown result");
        }
    }

    public void processQrCallback(View view) {

        ImageView myImageView = (ImageView) findViewById(R.id.imgview);
        Bitmap myBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.puppy);
        myImageView.setImageBitmap(myBitmap);

        imageBarcodeTask(myBitmap);
    }

    private void imageTextDetectTask(Bitmap imgBitmap) {
        Context context = getApplicationContext();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // TODO: Set the TextRecognizer's Processor.

        // TODO: Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            Log.w("TAG", "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w("TAG", getString(R.string.low_storage_error));
            }
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
            if(thisTextBlock == null) {
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
    private String imageBarcodeTask(Bitmap imgBitmap) {
        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
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

        return parsedValues;
    }



}
