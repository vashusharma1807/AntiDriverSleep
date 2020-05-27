package com.example.antidriversleep;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback , Handler.Callback {

    private Button endButton, closedEyeDectected , closedEyeDectected2 ;
    private SurfaceHolder mSurfaceHolder ;
    private SurfaceView mSurfaceView;
    private Button preview;
    static final String TAG = "CamTest";
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 1242;
    private static final int MSG_CAMERA_OPENED = 1;
    private static final int MSG_SURFACE_READY = 2;
    private final Handler mHandler = new Handler(this);
    private Camera camera;
    private CameraCharacteristics cameraCharacteristics;
    ImageReader imageReader ;
    CameraManager mCameraManager;
    String[] mCameraIDsList;
    CameraDevice.StateCallback mCameraStateCB;
    CameraDevice mCameraDevice;
    Camera.PictureCallback jpegCallback;
    CameraCaptureSession.CaptureCallback captureCallback;
    CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    boolean mSurfaceCreated = true;
    boolean mIsCameraConfigured = false;
    private Surface mCameraSurface = null;
    private boolean surfaceCamera = true;
    private String sound ;
    private int time ;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        Bundle extra = getIntent().getExtras();

        assert extra != null;

         sound = extra.getString("Tune");
         time = extra.getInt("Time");


        Toast.makeText(this, sound+String.valueOf(time), Toast.LENGTH_SHORT).show();


        preview = (Button) findViewById(R.id.no_preview_button);
        closedEyeDectected = (Button) findViewById(R.id.result_display);
        closedEyeDectected2 = (Button) findViewById(R.id.result_display2);

        endButton = (Button) findViewById(R.id.end_button);

        try {

            this.mSurfaceView = (SurfaceView) findViewById(R.id.preview_view);
            this.mSurfaceHolder = this.mSurfaceView.getHolder();
            this.mSurfaceHolder.addCallback(this);
            this.mCameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);



            mCameraIDsList = this.mCameraManager.getCameraIdList();
            for (String id : mCameraIDsList) {
                Log.v(TAG, "CameraID: " + id);
                CameraCharacteristics characteristics
                        = mCameraManager.getCameraCharacteristics(id);

                // We don't use a front facing camera in this sample.

                /*Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                int max_count = characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);

                int[] modes = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);

                Toast.makeText(this, "MAX:"+String.valueOf(max_count)+" Mode"+String.valueOf(modes), Toast.LENGTH_SHORT).show();
                */

            }
        } catch (Exception e) {
            Toast.makeText(this, "Camera List:"+e.toString(), Toast.LENGTH_SHORT).show();
        }

        try {

            mCameraStateCB = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    mCameraDevice = camera;
                    mHandler.sendEmptyMessage(MSG_CAMERA_OPENED);
                    Toast.makeText(CameraActivity.this, "MSG CAMERA OPEN ", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
                    camera.close();
                }
            };
            Toast.makeText(this, "mCameraCB", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Camera OPen:"+e.toString(), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //requesting permission
        Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                Toast.makeText(getApplicationContext(), "request permission", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "PERMISSION_ALREADY_GRANTED", Toast.LENGTH_SHORT).show();
            try {
                mCameraManager.openCamera(mCameraIDsList[0], mCameraStateCB, new Handler());
            }
            catch (CameraAccessException e) {
                Toast.makeText(this,"OPEN Camera Error"+ e.toString(), Toast.LENGTH_SHORT).show();
            }
        }


        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (surfaceCamera == true) {
                    ChangeSurfaceView();
                    preview.setText("Preview");
                } else {
                   /* int permissionCheck = ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(CameraActivity.this, Manifest.permission.CAMERA)) {

                        } else {
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                            Toast.makeText(getApplicationContext(), "request permission", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //Toast.makeText(getApplicationContext(), "PERMISSION_ALREADY_GRANTED", Toast.LENGTH_SHORT).show();
                        try {
                            mCameraManager.openCamera(mCameraIDsList[0], mCameraStateCB, new Handler());
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }*/

                   mCameraDevice.close();
                    Intent intent = new Intent(CameraActivity.this, CameraActivity.class);
                    startActivity(intent);
                }
            }
        });

        /*try {
            if (camera != null) {
                camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                    @Override
                    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                        if (faces.length == 0) {
                            Toast.makeText(CameraActivity.this, "Face Length =0 ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CameraActivity.this, "Face Length >0 ", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Face Detect Error:" + e.toString(), Toast.LENGTH_SHORT).show();
        }*/


        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent end = new Intent(CameraActivity.this, WelcomeActivity.class);
                startActivity(end);
                finish();
            }
        });
    }


    private void ChangeSurfaceView() {
        surfaceCamera = false;
        mSurfaceView.setBackgroundColor(Color.GRAY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (mCaptureSession != null) {
                mCaptureSession.stopRepeating();
                mCaptureSession.close();
                mCaptureSession = null;
            }

            mIsCameraConfigured = false;
        } catch (final CameraAccessException e) {
            // Doesn't matter, cloising device anyway
            Toast.makeText(this,"CameraAccess Error"+ e.toString(), Toast.LENGTH_SHORT).show();

        } catch (final IllegalStateException e2) {
            // Doesn't matter, cloising device anyway
            Toast.makeText(this,"IllegalState Error"+ e2.toString(), Toast.LENGTH_SHORT).show();
        } finally {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
                mCaptureSession = null;
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        Toast.makeText(this, "handleMessage", Toast.LENGTH_SHORT).show();
        switch (msg.what) {
            case MSG_CAMERA_OPENED:
                break;
            case MSG_SURFACE_READY:
                // if both surface is created and camera device is opened
                // - ready to set up preview and other things
                if (mSurfaceCreated && (mCameraDevice != null) && !mIsCameraConfigured) {
                    configureCamera();
                }
                break;
        }
        return true;
    }

    private void configureCamera() {
        Toast.makeText(this, "OnCameraConfigured", Toast.LENGTH_SHORT).show();
        // prepare list of surfaces to be used in capture requests
        List<Surface> sfl = new ArrayList<Surface>();

        sfl.add(mCameraSurface); // surface for viewfinder preview

        // configure camera with all the surfaces to be ever used
        try {
            mCameraDevice.createCaptureSession(sfl,
                    new CaptureSessionListener(), null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mIsCameraConfigured = true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Toast.makeText(this, "OnRequestPermissionsResult", Toast.LENGTH_SHORT).show();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    try {
                        mCameraManager.openCamera(mCameraIDsList[0], mCameraStateCB, new Handler());
                    } catch (Exception e) {
                        Toast.makeText(this,"Camera Manager:"+ e.toString(), Toast.LENGTH_SHORT).show();
                    }
                else {
                    ChangeSurfaceView();
                    preview.setText("Preview");
                }
                break;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Toast.makeText(this, "Surface Created", Toast.LENGTH_SHORT).show();
        try {
            camera = Camera.open(1);
            Camera.Parameters parameters = camera.getParameters();

            camera.setDisplayOrientation(90);
            parameters.setPreviewFrameRate(30);


            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }


            mCameraSurface = holder.getSurface();
            surfaceCamera = true;
        }
        catch (Exception e)
        {
            Toast.makeText(this,"SurfaceCreated"+ e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Toast.makeText(this, "Surface Changed", Toast.LENGTH_SHORT).show();
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(width, height);
            //mImageReader = ImageReader.newInstance(width, height, format, 30);
        }
        mCameraSurface = holder.getSurface();
        mSurfaceCreated = true;
        mHandler.sendEmptyMessage(MSG_SURFACE_READY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCreated = false;
        camera.release();
    }

    private class CaptureSessionListener extends CameraCaptureSession.StateCallback {
        @Override
        public void onConfigureFailed(final CameraCaptureSession session) {
            Toast.makeText(CameraActivity.this, "CaptureSessionConfigure failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConfigured(final CameraCaptureSession session) {
            Toast.makeText(CameraActivity.this, "CaptureSessionConfigure onConfigured", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "CaptureSessionConfigure onConfigured");
            mCaptureSession = session;

            try {
                Toast.makeText(CameraActivity.this, "Preview Builder", Toast.LENGTH_SHORT).show();
                previewRequestBuilder = mCameraDevice
                        .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(mCameraSurface);
                mCaptureSession.setRepeatingRequest(previewRequestBuilder.build(),
                        captureCallback, mHandler);

                ImageReader reader = ImageReader.newInstance( 240,180, ImageFormat.JPEG, 1);
                List<Surface> outputSurfaces = new ArrayList<Surface>(2);
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                previewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = null;
                        try {
                            Toast.makeText(CameraActivity.this, "In Image Listner", Toast.LENGTH_SHORT).show();
                            image = reader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            Toast.makeText(CameraActivity.this, "Image:"+String.valueOf(buffer), Toast.LENGTH_SHORT).show();
                            DetectFaces faces = new DetectFaces(CameraActivity.this);
                            boolean result = faces.detectFace(bytes, sound);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }  finally {
                            if (image != null) {
                                image.close();
                            }
                        }
                    }
                };


               /* mCameraDevice.createCaptureSession(, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                if (null == mCameraDevice) {
                                    return;
                                }

                                // When the session is ready, we start displaying the preview.
                                mCaptureSession = cameraCaptureSession;
                                try {

                                    previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                                            CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL);


                                    // Auto focus should be continuous for camera preview.
                                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                    // Flash is automatically enabled when necessary.

                                    // Finally, we start displaying the camera preview.
                                    CaptureRequest mPreviewRequest = previewRequestBuilder.build();

                                    mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                            captureCallback, mHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                            }
                        });*/

            }
            catch (CameraAccessException e) {
                Log.d(TAG, "setting up preview failed");
                e.printStackTrace();
            }
        }


        private CameraCaptureSession.CaptureCallback mCaptureCallback
                = new CameraCaptureSession.CaptureCallback() {

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull CaptureResult partialResult) {
                process(partialResult);
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                           @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                process(result);
            }

            private void process(CaptureResult result) {

                Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
                Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
                if (faces != null && mode != null)
                    Toast.makeText(CameraActivity.this, "tag" + "faces : " +
                            faces.length + " , mode : " + mode, Toast.LENGTH_SHORT).show();


                        // We have nothing to do when the camera preview is working normally.

//                  Here i set Face Detection
                        previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                                CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL);



            }
        };
    }
}







