package pri.tool.v4l2camera;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pri.tool.bean.Frame;
import pri.tool.bean.FrameRate;
import pri.tool.bean.Parameter;

import static pri.tool.v4l2camera.ImageUtils.NV12;


public class V4L2Camera{
    private final static String TAG ="V4LCamera";

    public static final int MINIMUM_PREVIEW_SIZE = 320;

    public final static int SUCCESS = 0;
    public final static int ERROR_INIT_FAIL = -1;
    public final static int ERROR_LOGIN_FAIL = -2;
    public final static int ERROR_STATE_ILLEGAL = -3;
    public final static int ERROR_CAPABILITY_UNSUPPORT = -4;
    public final static int ERROR_OPEN_FAIL = -5;
    public final static int ERROR_PREVIEW_FAIL = -6;

    IStateCallback mIStateCallback;//状态回调，如打开camera成功失败状态，其他异常
    IDataCallback mIDataCallback;//camera 数据回调

    Size mPreviewSize;

    static {
        System.loadLibrary("v4l-android");
    }

    public void init(int cameraid, IStateCallback callback, Context context) {
        mIStateCallback = callback;
        native_init();
    }

    /*
     *  释放SDK资源
     */
    public void release(int cameraid) {
        native_release();
        mIStateCallback = null;
    }

    public void open(int cameraid) {
        int ret = native_open(cameraid);

        if (ret == SUCCESS) {
            if(mIStateCallback != null)
                mIStateCallback.onOpened();
        } else {
            if(mIStateCallback != null)
                mIStateCallback.onError(ERROR_OPEN_FAIL);
        }
    }

    public void close(int cameraid) {
        native_close(cameraid);
    }

    /**
     * 设置预览 Surface。
     */
    public void setSurface(int cameraid, SurfaceHolder holder) {
        if (holder != null) {
            native_setSurface(cameraid, holder.getSurface());
        } else {
            native_setSurface(cameraid, (Surface)null);
        }

    }

    /**
     * 开始预览。
     */
    public void startPreview(int cameraid, IDataCallback callback) {
        mIDataCallback = callback;
        native_startPreview(cameraid);
    }

    /**
     * 停止预览。
     */
    public void stopPreview(int cameraid) {
        native_stopPreview(cameraid);
        mIDataCallback = null;
    }

    public ArrayList<Parameter> getCameraParameters(int cameraid) {
        return native_getParameters(cameraid);
    }

    public int setPreviewParameter(int cameraid, int width, int height, int pixFormat) {
        return native_setPreviewSize(cameraid, width, height, pixFormat);
    }

    public Size chooseOptimalSize(int cameraid, int desireWidth, int desireHeight) {

        ArrayList<Parameter> parameters = native_getParameters(cameraid);
        for(Parameter parameter:parameters) {
            Log.e(TAG, "support format: " + parameter.pixFormat);
            for (Frame frame : parameter.frames) {
                Log.e(TAG, "support frame: width:" + frame.width + ", height:" + frame.height);
                for (FrameRate frameRate:frame.frameRate) {
                    Log.e(TAG, "support framerate: numerator:" + frameRate.numerator + ", denominator:" + frameRate.denominator);
                }
            }
        }
/*
        FrameRate fa = new FrameRate(1, 30);
        ArrayList<FrameRate> fas = new ArrayList<>();
        fas.add(fa);
        Frame f = new Frame(1280, 720, fas);
        ArrayList<Frame> fs = new ArrayList<>();
        fs.add(f);
        Parameter p = new Parameter(NV12, fs);
        ArrayList<Parameter> parameters = new ArrayList<>();
        parameters.add(p);
*/
        List<Size> cameraSizes = getSupportedPreviewSizes(parameters, NV12);
        Size[] sizes = new Size[cameraSizes.size()];
        int i = 0;
        for (Size size : cameraSizes) {
            sizes[i++] = new Size(size.getWidth(), size.getHeight());
        }
        mPreviewSize = chooseOptimalSize(sizes, desireWidth, desireHeight);
        Log.e(TAG, "setPreviewSize:" + mPreviewSize.getWidth() + ", denominator:" + mPreviewSize.getHeight());
        native_setPreviewSize(cameraid, mPreviewSize.getWidth(), mPreviewSize.getHeight(), NV12);

        return mPreviewSize;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the minimum of both, or an exact match if possible.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param width The minimum desired width
     * @param height The minimum desired height
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    protected static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        final Size desiredSize = new Size(width, height);

        // Collect the supported resolutions that are at least as big as the preview Surface
        boolean exactSizeFound = false;
        final List<Size> bigEnough = new ArrayList<Size>();
        final List<Size> tooSmall = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.equals(desiredSize)) {
                // Set the size but don't return yet so that remaining sizes will still be logged.
                exactSizeFound = true;
            }

            if (option.getHeight() >= minSize && option.getWidth() >= minSize) {
                bigEnough.add(option);
            } else {
                tooSmall.add(option);
            }
        }

        Log.i(TAG, "Desired size: " + desiredSize.toString() + ", min size: " + minSize + "x" + minSize);
        Log.i(TAG, "Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]");
        Log.i(TAG, "Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]");

        if (exactSizeFound) {
            Log.i(TAG,"Exact size match found.");
            return desiredSize;
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            final Size chosenSize = Collections.min(bigEnough, new CompareSizesByArea());
            Log.i(TAG, "Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
            return chosenSize;
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    List<Size> getSupportedPreviewSizes(ArrayList<Parameter> parameters, int format) {
        List<Size> sizeList = new ArrayList<>();

        for(Parameter parameter:parameters) {
            Log.e(TAG, "support format: " + parameter.pixFormat);
            if (parameter.pixFormat == format) {
                for (Frame frame : parameter.frames) {
                    Size size = new Size(frame.width, frame.height);
                    sizeList.add(size);
                }
            }
        }

        return sizeList;
    }

    //Jni 层回调的函数
    private void postDataFromNative(int cameraid, byte[] data, int width, int height, int pixformat) {
        if (mIDataCallback != null) {
            mIDataCallback.onDataCallback(cameraid, data, pixformat, width, height);
        }
    }

    private native final void native_init();
    private native final void native_release();
    private native final int native_open(int cameraid);
    private native final void native_close(int cameraid);
    private native final ArrayList<Parameter> native_getParameters(int cameraid);
    private native final int native_setPreviewSize(int cameraid, int width, int height, int pixFormat);
    private native final int native_setSurface(int cameraid, Object surface);
    private native final int native_startPreview(int cameraid);
    private native final int native_stopPreview(int cameraid);

}
