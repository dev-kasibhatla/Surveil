package kasibhatla.dev.surveil;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "main-activity";
    public TextView txtLog;

    public static File sdcard = Environment.getExternalStorageDirectory();
    public static String folder = sdcard + "/Android/data/kasibhatla.dev.surveil/",
    photoFolder = folder + "photos/";

    //Getting a global camera cause this app revolves around it :D
    Camera cam;
    CameraPreview camPreview;
    SurfaceView previewLayout;
    SurfaceHolder surfaceHolder;

    //OnCreate stuff
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewLayout = (SurfaceView) findViewById(R.id.camPreviewLayout);
        surfaceHolder = previewLayout.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        initializeParameters();
    }

    /**
     * Initialize camera and set correct parameters for portrait mode.
     */
    private void initializeParameters(){
       txtLog = (TextView) findViewById(R.id.txtLog);
       getSupportActionBar().hide();

       //check and create folder
        File dir = new File(folder);
        if(!dir.exists()){
            boolean check = dir.mkdir();
            logThis(Boolean.toString(check));
            dir = new File(folder + "photos");
            dir.mkdir();
        }

       //previewLayout = (FrameLayout) findViewById(R.id.camPreviewLayout);
       //Create an instance of camera

        cam = getCameraInstance();
        Camera.Parameters parameters = cam.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for(Camera.Size camSize:sizes){
            Log.i(TAG, "Dimensions: (w,h): " + camSize.width + "\t" + camSize.height);
        }
        Camera.Size cs = sizes.get(0);
        parameters.setPreviewSize(cs.width, cs.height);
        parameters.setPictureSize(cs.width, cs.height);
        parameters.setRotation(270);

        cam.setDisplayOrientation(90);
        //parameters.setPreviewSize(320,240);
        previewLayout.getHolder().setFixedSize(cs.width, cs.height);
        cam.setParameters(parameters);



        //setting up a listener for log button:
        Button logButton =(Button)findViewById(R.id.logButton);
        logButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pushToLogs();
                    }
                }
        );
        logButton.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        btnClearLogs();
                        return false;
                    }
                }
        );

    }

    //Get logs going
    public static String logString = "Log:";
    private void logThis(String message){
        Log.i(TAG, message);
        logString+="\n" + message;
    }
    private void pushToLogs(){
        txtLog.setText(logString);
        Log.i(TAG, "Pushed to logs");
    }

    //Actual Methods
    private void getVideoStream(){

    }
    /** A safe way to get an instance of the Camera object. */
    //Should be static but trying it this way. Hoping system doesn't kill it
    //Another reason for it being static is to get the same object every time
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
            Log.i(TAG, "Camera instance error");
        }
        return c; // returns null if camera is unavailable
    }
    public void killCamera(){
        cam.release();
    }
    public void getCameraParameters(){
        //cam.getParameters();
        //Use cam.setParameters() to set all sorts of cam settings such as white balance, anti-banding frequency, etc
        //cam.setParameters(FOCUSE_MODE_VIDEO);
    }
    //Buttons


    boolean firstOpen=true;
    boolean previewIsOn=true;
    public void btnStartPreview(View v){
        //Let's do it the traditional way: Preview 1st
        if(!firstOpen){
            if(previewIsOn){
                cam.stopPreview();
                previewIsOn=false;
            }else{
                cam.startPreview();
                previewIsOn=true;
            }

        }else {
            //Get a preview
            camPreview = new CameraPreview(this, cam);

            try {
                cam.setPreviewDisplay(surfaceHolder);
            }catch (Exception e){
                Log.d(TAG, "preview error surfaceholder");
                e.printStackTrace();
            }
            firstOpen=false;
        }

    }

    public void stopPreview(View v){
        cam.stopPreview();

        /*
        Toast.makeText(this, "Destroying", Toast.LENGTH_SHORT).show();
        cam.release();
        cam.unlock();
         */
    }
    public void btnClickPicture(View v){
        cam.takePicture(null,null,pictureCallback);
    }

    public void btnClearLogs(){
        logString="Log:";
        pushToLogs();
        Log.i(TAG, "Cleared logs");
    }
    /*public void btnPushToLogs(View v){
        pushToLogs();
    }*/
    public void btnCheckForHardware(View v){
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            logThis("Camera available");
            pushToLogs();
        } else {
            // no camera on this device
            logThis("Camera unavailable");
            pushToLogs();
        }
    }
    public void btnKillEverything(View v){
        cam.stopPreview();
        cam.release();
    }

    public void btnCaptureVideo(View v){
        /*
        Order after getting preview:
            Unlock camera
            Configure media recorder
            set audio source
            set video source
            Video format and encoding
            setoutput file
            THEN CONNECT PREVIEW
            prepare media recorder
            start mediarecorder
            stop recording video
            release mediarecorder
            lock camera
            stop preview
            release camera
         */

        if(prepareMediaRecorder()){
            Log.i(TAG, "Media recorder was successful");
            logThis("Preparation successful");
            mediaRecorder.start();
            Toast.makeText(this, "Started video recording", Toast.LENGTH_SHORT).show();
        }else{
            logThis("Error with media recorder");
            Toast.makeText(this, "Error-mediarecorder", Toast.LENGTH_SHORT).show();
        }

    }

    public void btnStopVideo(View v){
        try{
            mediaRecorder.stop();
            mediaRecorder.reset();
            Toast.makeText(this, "Stop-error", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
        }
        mediaRecorder.release();
        cam.lock();
        //Toast.makeText(this, "Stopped recording", Toast.LENGTH_SHORT).show();
    }

    public MediaRecorder mediaRecorder;

    private boolean prepareMediaRecorder(){
        //cam is the camera instance
        mediaRecorder = new MediaRecorder();


        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        Camera.Parameters parameters = cam.getParameters();
        parameters.setPreviewSize(profile.videoFrameWidth,profile.videoFrameHeight);

        Log.i(TAG, profile.videoFrameWidth + "\t" + profile.videoFrameHeight);
       // previewLayout.setMinimumHeight(profile.videoFrameHeight);
       // previewLayout.setMinimumWidth(profile.videoFrameWidth);
        try{
            cam.setParameters(parameters);
        }catch (Exception e){
            e.printStackTrace();
        }
        camPreview = new CameraPreview(this, cam);
        //previewLayout.addView(camPreview);
        try {
            cam.setPreviewDisplay(surfaceHolder);
        }catch (Exception e){
            Log.d(TAG, "preview error surfaceholder");
            e.printStackTrace();
        }

//        cam.startPreview();

        //1
        //cam.stopPreview();
      //  cam=getCameraInstance();
        cam.unlock();
        mediaRecorder.setCamera(cam);

        //2 - sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
     //   mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
     //   mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

        //3 - camcorder profile
//        cam.setDisplayOrientation(90);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));


        //4 - output file
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        //preview
        mediaRecorder.setPreviewDisplay(camPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            mediaRecorder.release();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            mediaRecorder.release();
            return false;
        }
        return true;
    }




    //Implement picture callback for saving data as JPEG
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                //rotate image 90 clockwise

                //fos.write(data);
                fos.write(rotateImage(data,90));
                fos.close();
                Toast.makeText(MainActivity.this, "Created file", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

        }
    };


    public byte[] rotateImage(byte[] data, int degree){
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(degree);

        Bitmap bmp2= Bitmap.createBitmap(bmp, 0,0,w,h,matrix,true);

        //well that didn't work, so lets try converting it back to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp2.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] byteArray = stream.toByteArray();
        bmp2.recycle();
        return byteArray;
        /*
        * EXIF interface is a more versatile and accurate way of handling, but not
        * required in this scenario
        *
        * Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);

        ExifInterface exif=new ExifInterface(pictureFile.toString());

        Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
        if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
            realImage= rotate(realImage, 90);
        } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
            realImage= rotate(realImage, 270);
        } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
            realImage= rotate(realImage, 180);
        } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
            realImage= rotate(realImage, 90);
        }

        * */
    }

    //Saving media files
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
         //       Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        //We need a file in my private app folder
        File mediaStorageDir = new File(photoFolder);

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    public void btnOpenFileManagement(View v){
        Intent i = new Intent(MainActivity.this, FileManagement.class);
        startActivity(i);
    }

    public void btnVideoActivity(View v){
        //put some things back in place so that other activities can utilize 'em
        cam.lock();
        cam.release();
        Intent i = new Intent(MainActivity.this, VideoActivity.class);
        startActivity(i);
    }


}
