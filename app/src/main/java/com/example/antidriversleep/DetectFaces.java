package com.example.antidriversleep;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class DetectFaces {
    private float faceResult ;
    private MediaPlayer mp ;

    DetectFaces(Context context)
    {
        mp = MediaPlayer.create( context , R.raw.friends_theme_song);
        mp.start();
    }

    public boolean detectFace(byte[] bytes , String sound  ) {


        Bitmap bitmap = ( BitmapFactory.decodeByteArray(bytes,0,bytes.length));

        return detectFaces(bitmap);

    }


    private boolean detectFaces(Bitmap bitmap) {

        final int flag =0;
        //Toast.makeText(this, "Detect Face", Toast.LENGTH_SHORT).show();

        try {
            FirebaseVisionFaceDetectorOptions realTimeOpts =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                            .build();
            //Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            //Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(realTimeOpts);
            //Toast.makeText(this, "3", Toast.LENGTH_SHORT).show();

            try {
                Task<List<FirebaseVisionFace>> result =
                        detector.detectInImage(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                                            @Override
                                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                                //Toast.makeText(DisplayActivity.this, "Task result success", Toast.LENGTH_SHORT).show();
                                                faceResult = getFaceResult(faces);
                                                String s = String.valueOf(faceResult);
                                                //Toast.makeText(DisplayActivity.this, "Face result got !!"+s, Toast.LENGTH_SHORT).show();
                                                if(faceResult>0.95)
                                                {
                                                    //Toast.makeText(DisplayActivity.this, "Eye Closed", Toast.LENGTH_SHORT).show();

                                                    //closedEyeDectected.setVisibility(View.INVISIBLE);
                                                    mp.start();

                                                }
                                                else
                                                {
                                                    mp.pause();
                                                }
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //Toast.makeText(DisplayActivity.this, "Unable to process ML on Images", Toast.LENGTH_SHORT).show();
                                            }
                                        });
            }
            catch (Exception e)
            {
                //Toast.makeText(this, "Error in task :"+e.toString(), Toast.LENGTH_SHORT).show();
            }

        }
        catch (Exception e)
        {
            //Toast.makeText(this, "Error in model creation :"+e.toString(), Toast.LENGTH_SHORT).show();
        }


        return true;





    }



    private float getFaceResult(List<FirebaseVisionFace> faces) {

        float leftEyeClosedProb=0 , rightEyeClosedProb=0 ;
        for (FirebaseVisionFace face : faces) {
            /*Rect bounds = face.getBoundingBox();
            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
            if (leftEar != null) {
                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
            }

            // If contour detection was enabled:
            List<FirebaseVisionPoint> leftEyeContour =
                    face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
            List<FirebaseVisionPoint> upperLipBottomContour =
                    face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();

            // If classification was enabled:
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float smileProb = face.getSmilingProbability();
            }*/
            //if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
            rightEyeClosedProb = 1- face.getRightEyeOpenProbability();

            //if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
            leftEyeClosedProb = 1-face.getLeftEyeOpenProbability();


            //Toast.makeText(this, "left,right"+String.valueOf(leftEyeClosedProb)+String.valueOf(rightEyeClosedProb), Toast.LENGTH_SHORT).show();



            // If face tracking was enabled:
            //if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
            //   int id = face.getTrackingId();}

            return ((rightEyeClosedProb*leftEyeClosedProb));
        }



        return 0;
    }

}
