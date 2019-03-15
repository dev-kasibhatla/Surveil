package kasibhatla.dev.surveil;

import android.app.FragmentManager;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CameraPreviev2 extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    private static final String TAG = "camera-preview-2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_previev2);

        initializeParameters();



    }

    private void initializeParameters(){
        /*txtLog = (TextView) findViewById(R.id.txtLog);
        getSupportActionBar().hide();
        previewLayout = (FrameLayout) findViewById(R.id.camPreviewLayout);
        //Create an instance of camera
        cam = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        camPreview = new CameraPreview(this, cam);*/

        getSupportActionBar().hide();

        // Create an instance of Camera
        if(mCamera != null){
            mCamera.release();
        }
        mCamera = getCameraInstance();

        if(mCamera == null){
            Log.i(TAG, "Camera returning a null object");
        }
        // Create our Preview view and set it as the content of our activity.


        //problem is with preview sizes.
        //Let's try to see what is supported
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for(Camera.Size camSize:sizes){
            Log.i(TAG, "Dimensions: (w,h): " + camSize.width + "\t" + camSize.height);
        }
        Camera.Size cs = sizes.get(0);
        parameters.setPreviewSize(cs.width, cs.height);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(cameraOrientation);

        mPreview = new CameraPreview(this, mCamera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        //preview.setMinimumWidth(cs.width);
        //preview.setMinimumHeight(cs.height);
        preview.addView(mPreview);
        Log.i(TAG, "Dimensions: (w,h): " + cs.width + "\t" + cs.height);

    }
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
    public void btnReleaseCamera(View v){
        mCamera.stopPreview();
        mCamera.release();
    }
    public void btnCameraClick(View v){
        Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show();
    }
    int cameraOrientation =90;
    public void btnRotateCamera(View v){
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        mCamera.stopPreview();
        if(cameraOrientation > 300){
            cameraOrientation =0;
        }else {
            cameraOrientation += 90;
        }
        if(cameraOrientation == 90 || cameraOrientation == 270){
            preview.setMinimumHeight(240);
            preview.setMinimumWidth(320);
        }else{
            preview.setMinimumHeight(240);
            preview.setMinimumWidth(320);
        }
        mCamera.setDisplayOrientation(cameraOrientation);
        mPreview = new CameraPreview(this, mCamera);

        preview.addView(mPreview);
    }
    public void onBackPressed(){
        Intent i = new Intent(CameraPreviev2.this, MainActivity.class);
        mCamera.stopPreview();
        mCamera.release();
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }


}
