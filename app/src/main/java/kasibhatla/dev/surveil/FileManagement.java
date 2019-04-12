package kasibhatla.dev.surveil;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FileManagement extends AppCompatActivity {
    private String folder = MainActivity.folder;
    File pFolder, vFolder;
    String[] universalList;
    private final String TAG = "file-management";
    ListView lv;
    ImageView iv;

    private static final int CODE_DELETE_VIDEO = 2, CODE_DELETE_PHOTO=1,CODE_OPEN_VIDEO=4,CODE_OPEN_PHOTO=3;
    private TextView mTextMessage;
    private Button nextButton, previousButton;
   // boolean photoScreenVisible;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    iv.setVisibility(View.GONE);
                    homeSectionAccess();
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_photos);
                    iv.setVisibility(View.GONE);
                    photoSectionAccess();
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_videos);
                    iv.setVisibility(View.GONE);
                    videoSectionAccess();
                    return true;
                case R.id.navigation_player:
                    mTextMessage.setText("Media Player");
                    startMediaPlayer();
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_management);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        listFiles();
        initializeParameters();
        //setPhotoList();
    }

    protected void initializeParameters(){
        getSupportActionBar().hide();
        lv = (ListView)findViewById(R.id.fileListView);
        lv.setVisibility(View.GONE);
        iv=(ImageView)findViewById(R.id.playerImageView);
        nextButton = (Button)findViewById(R.id.button14);
        previousButton = (Button)findViewById(R.id.button13);
    }

    File[] photoList;
    File[] videoList;

    protected void listFiles(){
        pFolder = new File(folder, "photos");
        vFolder = new File(folder,"videos");
        if(pFolder.exists()){
            photoList = pFolder.listFiles();
        }else{
            pFolder.mkdir();
            Log.w(TAG, "can't find photo folder");
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }

        if(vFolder.exists()){
            videoList = vFolder.listFiles();
        }else{
            vFolder.mkdir();
            Log.w(TAG, "can't find photo folder");
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
        //arrange ascending (oof)

    }
    long[] photoIntegerArray, videoIntegerArray;
    protected void setPhotoList(){
        String[] photoArray = new String[photoList.length];
        double totalL=0.0, currL;
        for(int i=0;i<photoList.length;i++){
            currL = photoList[i].length()/1024.0;
            currL = Math.round(currL * 100.0)/100.0;
            photoArray[i] = photoList[i].getName()+"\t"+currL + " KB";
            totalL+=currL;
        }
        photoIntegerArray= new long[photoList.length];
        String temp,temp2;
        for(int i=0;i<photoList.length;i++){
            temp=photoArray[i]+"";
            temp2 = (temp.substring(4,11) + temp.substring(13,18)).trim();
            photoIntegerArray[i]=Long.parseLong(temp2);
        }
        //sorting descending (newest to oldest)
        long t1;
        File t2;
        for(int i=0;i<photoIntegerArray.length-1;i++){
            for(int j=0; j<photoIntegerArray.length-1-i;j++){
                if(photoIntegerArray[j]<photoIntegerArray[j+1]){
                    t1=photoIntegerArray[j];
                    photoIntegerArray[j] = photoIntegerArray[j+1];
                    photoIntegerArray[j+1]=t1;

                    t2=photoList[j];
                    photoList[j]=photoList[j+1];
                    photoList[j+1]=t2;

                    temp = photoArray[j];
                    photoArray[j]=photoArray[j+1];
                    photoArray[j+1]=temp;
                }
            }
        }

        //now photoArray can be used

        totalL = (double)Math.round(totalL/1024.0 * 100.0)/100.0;
        temp = "Photos"+"\t("+totalL+" MB)";
        mTextMessage.setText(temp);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.file_list_element,photoArray);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View v, int pos, long l) {
                String value=adapter.getItem(pos);
                showDialogMessage("Open?",value,CODE_OPEN_PHOTO,pos);

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapter.getItem(i);
                showDialogMessage("Delete?", value, CODE_DELETE_PHOTO, i);
                return false;
            }
        });
    }
    protected void setVideoList(){
        String[] videoArray = new String[videoList.length];
        double totalL=0.0, currL;
        for(int i=0;i<videoList.length;i++){
            currL=videoList[i].length()/(1024.0*1024.0);
            currL = Math.round(currL * 100.0)/100.0;
            videoArray[i] = videoList[i].getName() + "\t" +currL+" MB";
            totalL+=currL;
        }

        videoIntegerArray= new long[videoList.length];
        String temp;
        for(int i=0;i<videoList.length;i++){
            temp=videoArray[i]+"";
            videoIntegerArray[i]=Long.parseLong(temp.substring(0,7) + temp.substring(9,14));
        }

        long t1;
        File t2;
        for(int i=0;i<videoIntegerArray.length-1;i++){
            for(int j=0; j<videoIntegerArray.length-1-i;j++){
                if(videoIntegerArray[j]<videoIntegerArray[j+1]){
                    t1=videoIntegerArray[j];
                    videoIntegerArray[j] = videoIntegerArray[j+1];
                    videoIntegerArray[j+1]=t1;

                    t2=videoList[j];
                    videoList[j]=videoList[j+1];
                    videoList[j+1]=t2;

                    temp = videoArray[j];
                    videoArray[j]=videoArray[j+1];
                    videoArray[j+1]=temp;
                }
            }
        }
        //All video lists are now descending

        totalL = (double)Math.round(totalL * 100.0)/100.0;
        temp ="Videos"+"\t("+totalL+" MB)";
        mTextMessage.setText(temp);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.file_list_element,videoArray);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View v, int pos, long l) {
                String value=adapter.getItem(pos);
                showDialogMessage("Open?",value, CODE_OPEN_VIDEO, pos);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapter.getItem(i);
                showDialogMessage("Delete?",value, CODE_DELETE_VIDEO, i);
                return false;
            }
        });
    }

    protected void photoSectionAccess(){
        lv.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        previousButton.setVisibility(View.GONE);
        setPhotoList();
    }

    protected void videoSectionAccess(){
        lv.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        previousButton.setVisibility(View.GONE);
        setVideoList();
    }

    protected void homeSectionAccess(){
        lv.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        previousButton.setVisibility(View.GONE);
    }

    protected void deleteVideoFile(int i){
        if(videoList[i].delete()){
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            listFiles();
            setVideoList();
        }else{
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
    protected void deletePhotoFile(int i){
        if(photoList[i].delete()){
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            listFiles();
            setPhotoList();
        }else{
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    protected void openSelectedFileFromUri(Uri uri){
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "image/*");
        startActivity(i);
    }

    protected void showDialogMessage(String t, String c, final int actionCode, final int fileChosen){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(true);
        alertDialog.setTitle(t);
        alertDialog.setMessage(c);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(FileManagement.this, "Yes", Toast.LENGTH_SHORT).show();
                doIt(actionCode,fileChosen);
            }
        });
        alertDialog.setButton( DialogInterface.BUTTON_NEGATIVE,"NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(FileManagement.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

    protected void doIt(int actionCode, int fileChosen){
        switch (actionCode){
            case CODE_DELETE_PHOTO:
                deletePhotoFile(fileChosen);
                Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
                break;
            case CODE_DELETE_VIDEO:
                deleteVideoFile(fileChosen);
                Toast.makeText(this, "Video deleted",Toast.LENGTH_SHORT).show();
                break;
            case CODE_OPEN_PHOTO:
                Toast.makeText(this, "Opening photo", Toast.LENGTH_SHORT).show();
                openSelectedFileFromUri(Uri.fromFile(photoList[fileChosen]));
                break;
            case CODE_OPEN_VIDEO:
                Toast.makeText(this, "Opening video", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    protected void startMediaPlayer(){
        nextButton.setVisibility(View.VISIBLE);
        previousButton.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
        iv.setImageURI(Uri.fromFile(photoList[0]));
        currentIndex=0;
        iv.setVisibility(View.VISIBLE);
    }
    int currentIndex;
    public void nextItemPressed(View v){
        if(currentIndex<photoList.length-1){
            currentIndex++;
            iv.setImageURI(Uri.fromFile(photoList[currentIndex]));
//            Log.i(TAG, videoList[currentIndex].getName());
//            Log.i(TAG, Long.toString(photoIntegerArray[currentIndex]));
        }
    }
    public void previousItemPressed(View v){
        if(currentIndex>0){
            currentIndex--;
            iv.setImageURI(Uri.fromFile(photoList[currentIndex]));
        }
    }



}
