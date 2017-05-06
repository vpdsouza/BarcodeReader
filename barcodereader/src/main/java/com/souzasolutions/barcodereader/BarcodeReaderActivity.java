package com.souzasolutions.barcodereader;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by vpdso on 4/16/2017.
 */

public class BarcodeReaderActivity extends Activity {
    SurfaceView cameraView;
    SurfaceView transparentView;
    SurfaceHolder holder;
    SurfaceHolder holderTransparent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_barcode_reader);
        // Create first surface with his holder(holder)
        cameraView = (SurfaceView)findViewById(R.id.CameraView);
//        cameraView.setOnTouchListener(onTouchListner);

        holder = cameraView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Create second surface with another holder (holderTransparent)
        transparentView = (SurfaceView)findViewById(R.id.TransparentView);

        holderTransparent = transparentView.getHolder();
        holderTransparent.setFormat(PixelFormat.TRANSPARENT);
        holderTransparent.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        holderTransparent.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        drawOverlay();
    }

    private void drawOverlay() {
        float rectLeft = 50;
        float rectTop = 50;
        float rectRight = 100;
        float rectBottom = 100;
        DrawFocusRect(rectLeft , rectTop , rectRight , rectBottom , Color.YELLOW);
    }



    private void DrawFocusRect(float RectLeft, float RectTop, float RectRight, float RectBottom, int color)
    {

        Canvas canvas = holderTransparent.lockCanvas();
        if (canvas == null) {
            canvas = new Canvas();
        }
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        //border's properties
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(3);
        canvas.drawRect(RectLeft, RectTop, RectRight, RectBottom, paint);


        holderTransparent.unlockCanvasAndPost(canvas);
    }
}
