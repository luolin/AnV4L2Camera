package pri.tool.anv4l2camera;

import static pri.tool.v4l2camera.ImageUtils.H264;
import static pri.tool.v4l2camera.ImageUtils.MJPEG;
import static pri.tool.v4l2camera.ImageUtils.NV12;
import static pri.tool.v4l2camera.ImageUtils.YUYV;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.ArrayList;

import pri.tool.bean.Parameter;
import pri.tool.v4l2camera.IDataCallback;
import pri.tool.v4l2camera.IStateCallback;
import pri.tool.v4l2camera.V4L2Camera;

public class V4L2CameraUtil {
    private static String TAG = V4L2CameraUtil.class.getSimpleName();

    private Context mContext;
    private V4L2Camera adCamera;
    private int mCamereid = 0;
    private int previewWidth = 1280;
    private int previewHeight = 720;
    CameraStateCallback cameraStateCallback;
    CameraDataCallback cameraDataCallback;
    ArrayList<Parameter> parameters;

    AutoFitSurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    String[] pixformats;
    String[][] resolutions;

    public V4L2CameraUtil(Context contxt, int cameraid, int width, int height, AutoFitSurfaceView surfaceView){
        mContext = contxt;
        mCamereid = cameraid;
        previewWidth = width;
        previewHeight = height;

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.e(TAG, "Surface create: "+cameraid);
                initCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    public void initCamera() {
        cameraStateCallback = new CameraStateCallback();
        adCamera = new V4L2Camera();
        adCamera.init(mCamereid, cameraStateCallback, null);
        adCamera.open(mCamereid);
    }

    class CameraStateCallback implements IStateCallback {

        @Override
        public void onOpened() {
            Log.d(TAG, "onOpened");

            parameters = adCamera.getCameraParameters(mCamereid);

            pixformats = new String[parameters.size()];
            resolutions = new String[parameters.size()][];

            String sPixFormat;
            for (int i = 0; i < parameters.size(); i++) {
                Log.e(TAG, "format:" + parameters.get(i).pixFormat);
                switch (parameters.get(i).pixFormat) {
                    case NV12:
                        sPixFormat = "NV12";
                        break;
                    case YUYV:
                        sPixFormat = "YUYV";
                        break;
                    case MJPEG:
                        sPixFormat = "MJPEG";
                        break;
                    case H264:
                        sPixFormat = "H264";
                        break;
                    default:
                        sPixFormat = "NV12";
                        break;
                }

                pixformats[i] = sPixFormat;

                int resolutionSize = parameters.get(i).frames.size();

                resolutions[i] = new String[resolutionSize];

                for (int j = 0; j < resolutionSize; j++) {
                    String resolution = parameters.get(i).frames.get(j).width + "*" + parameters.get(i).frames.get(j).height;
                    resolutions[i][j] = resolution;
                }
            }
            //star priview
            int ret;
            ret = adCamera.setPreviewParameter(mCamereid, previewWidth, previewHeight, NV12);
            if (ret < 0) {
                Toast.makeText(mContext, "不支持该图像格式", Toast.LENGTH_SHORT).show();
                return;
            }

            adCamera.setSurface(mCamereid, surfaceHolder);

            cameraDataCallback = new CameraDataCallback();
            adCamera.startPreview(mCamereid, cameraDataCallback);
        }

        @Override
        public void onError(int error) {

        }
    }

    class CameraDataCallback implements IDataCallback {

        @Override
        public void onDataCallback(int cameraid, byte[] data, int dataType, int width, int height) {
            Log.e(TAG, "camera "+ cameraid +" onDataCallbakck  dataType:" + dataType + ", width:" + width + ", height:" + height);
            //处理camera preview 数据
        }
    }
}
