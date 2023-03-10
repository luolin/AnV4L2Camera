package pri.tool.anv4l2camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import pri.tool.bean.Parameter;
import pri.tool.v4l2camera.IDataCallback;
import pri.tool.v4l2camera.IStateCallback;
import pri.tool.v4l2camera.V4L2Camera;

import static pri.tool.v4l2camera.ImageUtils.H264;
import static pri.tool.v4l2camera.ImageUtils.MJPEG;
import static pri.tool.v4l2camera.ImageUtils.NV12;
import static pri.tool.v4l2camera.ImageUtils.YUYV;

public class V4L2CameraActivity extends Activity {
    private final static String TAG = "V4L2CameraActivity";


    private int previewWidth = 1280;
    private int previewHeight = 720;
    AutoFitSurfaceView surfaceView1;
    V4L2CameraUtil v4L2CameraUtil1;

    AutoFitSurfaceView surfaceView2;
    V4L2CameraUtil v4L2CameraUtil2;

    AutoFitSurfaceView surfaceView3;
    V4L2CameraUtil v4L2CameraUtil3;

    AutoFitSurfaceView surfaceView4;
    V4L2CameraUtil v4L2CameraUtil4;

    AutoFitSurfaceView surfaceView5;
    V4L2CameraUtil v4L2CameraUtil5;

    AutoFitSurfaceView surfaceView6;
    V4L2CameraUtil v4L2CameraUtil6;

    AutoFitSurfaceView surfaceView7;
    V4L2CameraUtil v4L2CameraUtil7;

    AutoFitSurfaceView surfaceView8;
    V4L2CameraUtil v4L2CameraUtil8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        verifyStoragePermissions(this);

        Context context = getApplicationContext();

        surfaceView1 = findViewById(R.id.cameraSurface1);
        surfaceView1.setAspectRatio(previewWidth, previewHeight);
        v4L2CameraUtil1 = new V4L2CameraUtil(context, 0, previewWidth, previewHeight, surfaceView1);

        surfaceView2 = findViewById(R.id.cameraSurface2);
        surfaceView2.setAspectRatio(previewWidth, previewHeight);
        v4L2CameraUtil2 = new V4L2CameraUtil(context, 1, previewWidth, previewHeight, surfaceView2);

        surfaceView3 = findViewById(R.id.cameraSurface3);
        surfaceView3.setAspectRatio(previewWidth, previewHeight);
        v4L2CameraUtil3 = new V4L2CameraUtil(context, 2, previewWidth, previewHeight, surfaceView3);

        surfaceView4 = findViewById(R.id.cameraSurface4);
        surfaceView4.setAspectRatio(previewWidth, previewHeight);
        v4L2CameraUtil4 = new V4L2CameraUtil(context, 3, previewWidth, previewHeight, surfaceView4);

        // csi2
        try {
            surfaceView5 = findViewById(R.id.cameraSurface5);
            surfaceView5.setAspectRatio(previewWidth, previewHeight);
            v4L2CameraUtil1 = new V4L2CameraUtil(context, 4, previewWidth, previewHeight, surfaceView5);
        }catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }

        try {
        surfaceView6 = findViewById(R.id.cameraSurface6);
        surfaceView6.setAspectRatio(previewWidth, previewHeight);
        v4L2CameraUtil2 = new V4L2CameraUtil(context, 5, previewWidth, previewHeight, surfaceView6);
        }catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }

        try {
            surfaceView7 = findViewById(R.id.cameraSurface7);
            surfaceView7.setAspectRatio(previewWidth, previewHeight);
            v4L2CameraUtil3 = new V4L2CameraUtil(context, 6, previewWidth, previewHeight, surfaceView7);
        }catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
        try {
            surfaceView8 = findViewById(R.id.cameraSurface8);
            surfaceView8.setAspectRatio(previewWidth, previewHeight);
            v4L2CameraUtil4 = new V4L2CameraUtil(context, 7, previewWidth, previewHeight, surfaceView8);
        }catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    /*
     * android 动态权限申请
     * */
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
