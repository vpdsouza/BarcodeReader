package com.souzasolutions.barcodereader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.souzasolutions.barcodereader.ui.camera.CameraPreview;
import com.souzasolutions.barcodereader.ui.camera.CameraSource;
import com.souzasolutions.barcodereader.ui.camera.GraphicOverlay;
import com.souzasolutions.barcodereader.utils.FontManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by vincent.dsouza on 3/29/2017.
 */

public class BarcodeScannerActivity extends AppCompatActivity implements BarcodeGraphicTracker.BarcodeDetectorListener {

    private static final String TAG = "Read Barcode";

    //intent request code to handle Google Play Services if needed
    private static final int RC_HANDLE_GMS = 9001;

    //Permission request code
    private static final int RC_CAMERA_PERMISSION = 2;

    //CONSTANTS
    public static final String ScannedBarcode = "ScannedBarcode";
    public static final String FlashMode = "FlashMode";

    //Class level object variables
    private Switch mUseFlashSwitch;
    private CameraSource mCameraSource;
    private CameraPreview mCameraPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private int mCameraPermission;
    boolean mDeviceHasFlash = false;
    boolean mIsPermissionDialogPrompted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appToolBar);
        setSupportActionBar(toolbar);

        mCameraPreview = (CameraPreview) findViewById(R.id.cpScanner);
        mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.goOverlay);

        mUseFlashSwitch = (Switch) findViewById(R.id.switchFlash);
        FontManager fontManager = new FontManager();
        mUseFlashSwitch.setTypeface(FontManager.getTypeface(this));
        mUseFlashSwitch.setText(FontManager.getFlashIcon());
        mUseFlashSwitch.setTextColor(Color.WHITE);
        mUseFlashSwitch.setVisibility(View.GONE);
        findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mCameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        BarcodeGraphicTracker.barcodeDetectorListener = this;

        Box box = new Box(this);
        addContentView(box, new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        detectFlashOnDevice();

        if (mCameraPermission == PackageManager.PERMISSION_GRANTED) {
            setupFlashButton();
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }
    private void detectFlashOnDevice() {
        mDeviceHasFlash = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!mDeviceHasFlash) {
            mDeviceHasFlash = hardwareHasFlash();
        }
    }

    private boolean hardwareHasFlash() {
        boolean hasFlash = false;
        if (mCameraPermission == PackageManager.PERMISSION_GRANTED) {
            try {
                Camera camera = Camera.open();
                if (camera != null) {
                    Camera.Parameters parameters = camera.getParameters();
                    if (parameters != null) {
                        List<String> flashModes = parameters.getSupportedFlashModes();

                        if (flashModes != null) {
                            hasFlash = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hasFlash;
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_CAMERA_PERMISSION);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_CAMERA_PERMISSION);
            }
        };

        findViewById(R.id.llContainer).setOnClickListener(listener);
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_CAMERA_PERMISSION) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Barcode Scanner")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }



    /**
     * Creates and starts the camera.  Note that this uses a higher resolution
     * to enable the barcode detector to detect small barcodes at long distances.
     */
    private void createCameraSource() {
        Context context = getApplicationContext();

        //Create the barcode detector in order to detect the barcodes from the scanner
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        //Just make sure we have all the required API components installed and operational on the device, else handle it
        if (!barcodeDetector.isOperational()) {
            Log.v(TAG, "Detector dependencies are not yeat available");

            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, intentFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.v(TAG, getString(R.string.low_storage_error));
            }
        }

        CameraSource.Builder builder = new CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .setFlashMode(mUseFlashSwitch.isChecked() ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setRequestedFps(60.0f);

        mCameraSource = builder.build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraPreview != null) {
            mCameraPreview.stop();
        }
    }

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     ** Release the resources associated with the camers source, detectors, and the processing piplelines
     ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraPreview != null) {
            mCameraPreview.release();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dialog =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dialog.show();
        }

        if (mCameraSource != null) {
            try {
                mCameraPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void setupFlashButton() {
        if (mDeviceHasFlash) {
            mUseFlashSwitch.setVisibility(View.VISIBLE);
            mUseFlashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    toggleFlash();
                }
            });
            mUseFlashSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFlash();
                }
            });

        }
        else {
            mUseFlashSwitch.setVisibility(View.GONE);
        }
    }

    private void toggleFlash() {
        if (mUseFlashSwitch.isChecked()) {
            mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        }
        else {
            mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        startCameraSource();
    }



    @Override
    public void onBarcodeDetected(Barcode barcode) {
        retrieveBarcodeFromDetection(barcode);
    }

    private void retrieveBarcodeFromDetection(Barcode barcode) {

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                //notify the user about the detection through audible indicator
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);
                break;
            case AudioManager.RINGER_MODE_SILENT:
                // nothing to play or vibrate
                break;
        }

        Intent intent = new Intent();
        intent.putExtra(ScannedBarcode, barcode);
        //set the result of the detection to the activity and finish the activity to transfer the control to the caller
        setResult(CommonStatusCodes.SUCCESS, intent);
        finish();
    }

    private class Box extends View {
        private Paint paint = new Paint(Color.YELLOW);

        Box(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (mCameraPreview != null) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.YELLOW);
                paint.setStrokeWidth(5);

                Rect rect = new Rect();
                mCameraPreview.getLocalVisibleRect(rect);

                int width = 600;
                int height = 400;
                int left = (rect.right - width) / 2;
                int top = (rect.bottom - height) / 2;
                int right = left + width;
                int bottom = top + height;

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }
}
