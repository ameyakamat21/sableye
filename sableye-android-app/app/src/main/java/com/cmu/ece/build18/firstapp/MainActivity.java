package com.cmu.ece.build18.firstapp;

import android.*;
import android.Manifest;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.View;
import android.content.Context;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ScrollView;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.graphics.ImageFormat;
import android.view.SurfaceView;
import android.graphics.SurfaceTexture;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;

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

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.File;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    public static final String STATE_LATITUDE = "STATE_LAT";
    public static final String STATE_LONGITUDE = "STATE_LON";
    public static final String IMAGE_DIRECTORY_PATH = "/sableye_media/";
    public static final String IMAGE_FILE_NAME = "latest.jpeg";
    public static final int LOCATION_PERMISSION_CODE = 1;
    public static final int CAMERA_PERMISSION_CODE = 2;
    private GoogleApiClient mGoogleApiClient;
    private double currLatitude, currLongitude;
    public static final int PICK_IMAGE_REQUEST = 1;
    private Camera backCamera;
    private File imageDirPath;
    private ImageView imageDisplay;
    private TextView displayTextView;
    private TextToSpeech textToSpeech;
    private boolean isTtsReady=false;
    private ImageProcessingCallback imgProcessingCallback;
    private boolean pressedCaptureKeyRecently;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pressedCaptureKeyRecently=false;
        imageDisplay = (ImageView) findViewById(R.id.imgview);
        displayTextView = (TextView) findViewById(R.id.textview);
        displayTextView.append("<No text yet>");

        //create directory (if not present) to store images
        imageDirPath = new File(Environment.
                getExternalStorageDirectory() + IMAGE_DIRECTORY_PATH);

        if (!imageDirPath.exists()) {
            imageDirPath.mkdir();
        }

        //initialize camera
        cameraInit();

        //Initialize TextToSpeech module
        textToSpeech = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    showToast("TTS module initialized!");
                    isTtsReady=true;
                } else {
                    showToast("Error initializing TTS module.");
                }
            }
        });

        imgProcessingCallback =
                new ImageProcessingCallback(getApplicationContext(), displayTextView, textToSpeech);
        //initialize google play services interface
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
        String permissionsGrantedStr = "";
        for(int i=0; i<grantResults.length; i++) {
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                showToast("Did not get permission for " + permissions[i] + ":(");
                return;
            }
            permissionsGrantedStr += permissions[i] + ", ";
        }

        showToast("Got permissions! (" + permissionsGrantedStr + ")");
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
            requestPermissions(permisionsToRequest, LOCATION_PERMISSION_CODE);
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
    protected void onPause() {
        super.onPause();
        if(backCamera != null) {
            backCamera.release();
            backCamera=null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(backCamera != null) {
            backCamera.release();
            backCamera=null;
        }

        textToSpeech.shutdown();
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
            requestPermissions(permisionsToRequest, LOCATION_PERMISSION_CODE);
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
                    String textToSpeak=imgProcessingCallback.imageBarcodeTask(selectedImage);
                    textToSpeak+=imgProcessingCallback.imageTextDetectTask(selectedImage);
                    textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            showToast("Got unknown activity result");
            Log.e("ERROR", "Got unknown result");
        }
    }


    private void captureImageRoutine() {

        displayTextView.setText("Focusing camera...\n");
        backCamera.autoFocus(new Camera.AutoFocusCallback()
        {
            @Override
            public void onAutoFocus(boolean success, Camera camera)
            {
                File imgFile = new File(imageDirPath + "latest.jpeg");

                ImageProcessingCallback imgProcCallback = new ImageProcessingCallback(
                        getApplicationContext(), displayTextView, textToSpeech);

                displayTextView.append("Autofocus complete.");
                backCamera.takePicture(null, null,
                        new SableyePictureCallback(imgFile, imageDisplay, imgProcCallback, textToSpeech));
            }
        });


    }

    public void takePhotoBtnCallback(View view) {
        if(!isTtsReady) {
            showToast("TTS module initialized yet!");
        }

        captureImageRoutine();
    }

    /** Check if this device has a camera
     *
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void cameraInit() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED ||

                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED ||

                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            Log.e("PERMISSION", "Does not have camera permission!");
            showToast("Does not have camera permission! Requesting..");

            String[] permisionsToRequest = {Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};

            requestPermissions(permisionsToRequest, CAMERA_PERMISSION_CODE);
        }

        backCamera = getCameraInstance();

        if(backCamera == null) {
            return;
        }

        Camera.Parameters parameters = backCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        List<String> focusModes = parameters.getSupportedFocusModes();
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        backCamera.setParameters(parameters);
        SurfaceView mview = new SurfaceView(getBaseContext());
        SurfaceTexture surfaceTexture = new SurfaceTexture(10);

        try {
            backCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            showToast("Got IOException while setting preview texture.");
        }
//        backCamera.setPreviewDisplay(mview.getHolder());
//        backCamera.setPreviewDisplay(null);
        backCamera.startPreview();
        //backCamera.takePicture(null, null, new SableyePictureCallback(imageDirPath));
        //backCamera.stopPreview();
    }

    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            showToast("Camera not available");
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:

                if (action == KeyEvent.ACTION_DOWN) {
                    if(pressedCaptureKeyRecently) {
                        //detected double press

                        //do double press action
                    }
                    pressedCaptureKeyRecently=true;

                    //check for double keypress. In which case, dispatch to
                    //default volume down functionality
                    CountDownTimer timeToDoublePress = new CountDownTimer(700, 700) {
                        @Override
                        public void onTick(long millisUntilFinished) {}

                        @Override
                        public void onFinish() {
                            pressedCaptureKeyRecently=false;
                        }
                    };
                    timeToDoublePress.start();
                    return super.dispatchKeyEvent(event);
                }
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    captureImageRoutine();

                }
                return true;

        }
        return super.dispatchKeyEvent(event);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
//
//        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            displayTextView.append("VOLDOWN\n");
//        }
//        return super.onKeyDown(keyCode, keyEvent);
//    }

}
