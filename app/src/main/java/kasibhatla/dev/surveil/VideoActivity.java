package kasibhatla.dev.surveil;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VideoActivity extends AppCompatActivity {

    private static final String TAG = "vid-act";
    Camera cam;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Camera.Size mPreviewSize;
    MediaRecorder mMediaRecorder;
    CameraPreview camPreview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //lets do this right
        initializeParameters();
        /*surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);*/
        getSupportActionBar().hide();

        //initializePreviewParameters();
    }

    public void initializeParameters(){
        cam = getCameraInstance();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Camera.Parameters p = cam.getParameters();

        final List<Camera.Size> listSize = p.getSupportedPreviewSizes();
        mPreviewSize = listSize.get(1);
        p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        //p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        cam.setParameters(p);
        try
        {
            cam.setPreviewDisplay(surfaceHolder);
            cam.startPreview();
        }
        catch (IOException e)
        {
            Log.i(TAG, "error starting preview");
            e.printStackTrace();
        }

        cam.unlock();
        Log.i(TAG, "initialization complete");
    }

    public void initializePreviewParameters(){
     //   cam = getCameraInstance();
        cam.lock();
        Camera.Parameters parameters = cam.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for(Camera.Size camSize:sizes){
            Log.i(TAG, "Dimensions: (w,h): " + camSize.width + "\t" + camSize.height);
        }
        Camera.Size cs = sizes.get(0);
        //parameters.setPreviewSize(cs.width, cs.height);
        //parameters.setPreviewSize(320,240);
        cam.setParameters(parameters);
//        cam.setDisplayOrientation(90);

//        cam.setDisplayOrientation(90);
    }
    boolean firstOpen=true;
    boolean previewIsOn=true;
    public void btnStartVidPreview(View v){

       /* if(!firstOpen){
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
        }*/
        camPreview = new CameraPreview(this, cam);

        try {
            cam.setPreviewDisplay(surfaceHolder);
        }catch (Exception e){
            Log.d(TAG, "preview error surfaceholder");
            e.printStackTrace();
        }

    }

    public void btnConfigureVideo(View v){
        try {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(cam);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mMediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath()+File.separator+ "video.mp4");
            //   mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
            mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

            mMediaRecorder.prepare();
            Log.i(TAG, "prepared mediarecorder");

        }catch (IOException ioe){
            Log.w(TAG, ioe.getMessage());
            ioe.printStackTrace();
        }

    }

    public void btnStartVideo(View v){
        try {
            mMediaRecorder.start();
            Toast.makeText(this, "started recorder", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "started recorder");
        }catch(Exception e){
            e.printStackTrace();
            Log.w(TAG, "unable to start recording");
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void btnStopVideo(View v){
        try {
            mMediaRecorder.stop();
            Toast.makeText(this, "started recorder", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "stopped recorder");
        }catch(Exception e){
            e.printStackTrace();
            Log.w(TAG, "unable to stop recording");
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //mandatory functions from mainactivity
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

    private File videoNameConstructor(){
        File name;
        //Temporary naming scheme until the working conditions are figured out
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        name  = new File(MainActivity.folder+"videos/",timeStamp+".mp4");
        return name;
    }

}