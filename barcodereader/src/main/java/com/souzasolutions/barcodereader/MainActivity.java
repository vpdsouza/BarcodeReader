package com.souzasolutions.barcodereader;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.souzasolutions.barcodereader.utils.FontManager;

/**
 * Created by vincent.dsouza on 3/29/2017.
 */

public class MainActivity extends AppCompatActivity {

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "Barcode Main";

    private TextView tvBarcodeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appToolBar);
        setSupportActionBar(toolbar);

        tvBarcodeResult = (TextView) findViewById(R.id.tvBarcodeResult);

        Button btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setTypeface(FontManager.getTypeface(getApplicationContext()));
        btnScan.setText(FontManager.getBarcodeIcon());
        btnScan.setTextColor(Color.RED);
        btnScan.setTextSize(60);


    }

    public void displayNewScreen(View view) {
        Intent intent = new Intent(this, BarcodeReaderActivity.class);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    public void displayBarcodeScanner(View view) {
        Intent intent = new Intent(this, BarcodeScannerActivity.class);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeScannerActivity.ScannedBarcode);
                    //if the barcode is returned then display the result else see if the user has enabled the flash and take them back to the scan
                    if (barcode != null) {
                        tvBarcodeResult.setText(barcode.displayValue);
                        Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    } else {
                        boolean flashMode = data.getBooleanExtra(BarcodeScannerActivity.FlashMode, false);
                        Intent intent = new Intent(this, BarcodeScannerActivity.class);
                        intent.putExtra(BarcodeScannerActivity.FlashMode, flashMode);

                        startActivityForResult(intent, RC_BARCODE_CAPTURE);
                    }
                } else {
                    tvBarcodeResult.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                tvBarcodeResult.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
