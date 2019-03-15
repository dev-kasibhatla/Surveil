package kasibhatla.dev.surveil;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "main-activity";
    public TextView txtLog;

    //Getting a global camera cause this app revolves around it :D
   // Camera cam;
   // CameraPreview camPreview;
    FrameLayout previewLayout;

    //OnCreate stuff
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeParameters();
    }
    private void initializeParameters(){
       txtLog = (TextView) findViewById(R.id.txtLog);
       getSupportActionBar().hide();
       previewLayout = (FrameLayout) findViewById(R.id.camPreviewLayout);
       //Create an instance of camera
     //  cam = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
      //  camPreview = new CameraPreview(this, cam);

    }

    //Get logs going
    public static String logString = "Log:";
    private void logThis(String message){
        Log.i(TAG, message);
        logString+="\n" + message;
    }
    private void pushToLogs(){
        txtLog.setText(logString);
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

    public void btnClearLogs(View v){
        logString="Log:";
        pushToLogs();
    }
    public void btnPushToLogs(View v){
        pushToLogs();
    }
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
    public void btnOpenCameraPreview(View v){
        Intent i = new Intent(MainActivity.this, CameraPreviev2.class);
        startActivity(i);
    }

}
