package kasibhatla.dev.surveil;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    FrameLayout previewLayout;

    //OnCreate stuff
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        cam.setParameters(parameters);
        cam.setDisplayOrientation(90);


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
    public void btnGetVideoStream(View v){
        previewLayout.removeAllViews();
        //previewLayout.addView(camPreview);
    }


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
            FrameLayout preview = (FrameLayout) findViewById(R.id.camPreviewLayout);
            preview.addView(camPreview);
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
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

        }
    };


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


}
