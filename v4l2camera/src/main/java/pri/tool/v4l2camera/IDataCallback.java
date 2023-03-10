package pri.tool.v4l2camera;

public interface IDataCallback {
    void onDataCallback(int cameraid, byte[] data, int dataType, int width, int height);
}
