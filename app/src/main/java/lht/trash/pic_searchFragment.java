package lht.trash;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lht.trash.env.Camera2Proxy;
import lht.trash.env.CameraUtils;
import lht.trash.env.ColorConvertUtil;
import lht.trash.tflite.Classifier;
import lht.trash.views.AutoFitTextureView;
import lht.trash.views.Camera2View;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class pic_searchFragment extends Fragment  {
    private static final String TAG = "CameraFragment";
    private Classifier.Model model = Classifier.Model.QUANTIZED_MOBILENET;
    private Classifier.Device device = Classifier.Device.CPU;
    private int numThreads = -1;
    private Classifier classifier;
    private Camera2View mCameraView;
    private byte[][] yuvBytes = new byte[3][];
    static final int kMaxChannelValue = 262143;
    private int[] rgbBytes = null;
    private Bitmap rgbFrameBitmap = null;

    private Camera2Proxy mCameraProxy;
    private TextView picResult;

    GarbageDatabase garbageDatabase;
    GarbageDataDao garbageDataDao;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mCameraProxy.openCamera();
            mCameraProxy.setPreviewSurface(texture);
            // 根据相机预览设置View大小，避免显示变形
            Size previewSize = mCameraProxy.getPreviewSize();
            mCameraView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pic_search, null);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        //mCloseIv = rootView.findViewById(R.id.toolbar_close_iv);
        //mSwitchCameraIv = rootView.findViewById(R.id.toolbar_switch_iv);
        //mTakePictureIv = rootView.findViewById(R.id.take_picture_iv);
        mCameraView = rootView.findViewById(R.id.mCamera);
        picResult = rootView.findViewById(R.id.pic_result);
        mCameraProxy = mCameraView.getCameraProxy();

        //mCloseIv.setOnClickListener(this);
        //mSwitchCameraIv.setOnClickListener(this);
        //mTakePictureIv.setOnClickListener(this);
        mCameraProxy.setImageAvailableListener(mOnImageAvailableListener);

        //数据库操作
        garbageDatabase = Room.databaseBuilder(this.getContext(),GarbageDatabase.class,"garbage_database")
                .allowMainThreadQueries()
                .build();
        garbageDataDao=garbageDatabase.getGarbageDataDao();


    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraView.isAvailable()) {
            mCameraProxy.openCamera();
        } else {
            mCameraView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraProxy.releaseCamera();
    }

    private byte[] mYuvBytes;
    private boolean mIsShutter;

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    public static void convertYUV420ToARGB8888(
            byte[] yData,
            byte[] uData,
            byte[] vData,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int[] out) {
        int yp = 0;
        for (int j = 0; j < height; j++) {
            int pY = yRowStride * j;
            int pUV = uvRowStride * (j >> 1);

            for (int i = 0; i < width; i++) {
                int uv_offset = pUV + (i >> 1) * uvPixelStride;

                out[yp++] = YUV2RGB(0xff & yData[pY + i], 0xff & uData[uv_offset], 0xff & vData[uv_offset]);
            }
        }
    }
    private static int YUV2RGB(int y, int u, int v) {
        // Adjust and check YUV values
        y = (y - 16) < 0 ? 0 : (y - 16);
        u -= 128;
        v -= 128;

        // This is the floating point equivalent. We do the conversion in integer
        // because some Android devices do not have floating point in hardware.
        // nR = (int)(1.164 * nY + 2.018 * nU);
        // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
        // nB = (int)(1.164 * nY + 1.596 * nV);
        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
        r = r > kMaxChannelValue ? kMaxChannelValue : (r < 0 ? 0 : r);
        g = g > kMaxChannelValue ? kMaxChannelValue : (g < 0 ? 0 : g);
        b = b > kMaxChannelValue ? kMaxChannelValue : (b < 0 ? 0 : b);

        return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }

            int width = mCameraProxy.getPreviewSize().getWidth();
            int height = mCameraProxy.getPreviewSize().getHeight();
            if (rgbBytes == null) {
                rgbBytes = new int[width * height];
            }
            if (mYuvBytes == null) {
                // YUV420 大小总是 width * height * 3 / 2
                mYuvBytes = new byte[width * height * 3 / 2];
            }

            // YUV_420_888
            Image.Plane[] planes = image.getPlanes();
            fillBytes(planes,yuvBytes);
            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            convertYUV420ToARGB8888(
                    yuvBytes[0],
                    yuvBytes[1],
                    yuvBytes[2],
                    width,
                    height,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    rgbBytes);
            rgbFrameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            rgbFrameBitmap.setPixels(rgbBytes,0,width,0,0,width,height);

            try {
                classifier = Classifier.create(getActivity(), model, device, numThreads);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(classifier!=null){
                final List<Classifier.Recognition> results =
                        classifier.recognizeImage(rgbFrameBitmap,90);
                Log.d("有没有内容啊",""+results.size());
                //picResult.setText(results.get(0).getTitle());
                Log.d("have classifid",results.get(0).getTitle());
                String search=results.get(0).getTitle();
//Toast.makeText(getActivity(), tap, Toast.LENGTH_SHORT).show();
                GarbageData result = garbageDataDao.searchGarbage(search);
                try{

                    String catg="";
                    switch (result.getCategory()){
                        case "1":catg+="可回收垃圾";break;
                        case "2":catg+="有害垃圾";break;
                        case "4":catg+="厨余垃圾";break;
                        case "8":catg+="其他垃圾";break;
                        case "16":catg+="其他垃圾";break;
                    }
                    String showResult=result.getId()+":"+result.getStuff()+"属于"+catg;
                    picResult.setText(showResult);
                }catch (Exception e){
                    //Toast.makeText(getActivity(),"没有这种垃圾",Toast.LENGTH_SHORT).show();
                    //String showResult="这东西不用回收，扔了吧";
                    //textView.setText(showResult);
                }
            }
            else{
                Log.d("fuck","咋还是空的");

            }
            if (mIsShutter) {
                mIsShutter = false;
/*
                // save yuv data
                String yuvPath = FileUtil.SAVE_DIR + System.currentTimeMillis() + ".yuv";
                FileUtil.saveBytes(mYuvBytes, yuvPath);

                // save bitmap data
                String jpgPath = yuvPath.replace(".yuv", ".jpg");
                Bitmap bitmap = ColorConvertUtil.yuv420pToBitmap(mYuvBytes, width, height);
                pic_classify();
                final List<Classifier.Recognition> results =
                        classifier.recognizeImage(bitmap,90);
                Log.d("有没有内容啊",""+results.size());

                Log.d("have classifid",results.get(0).getTitle());
                FileUtil.saveBitmap(bitmap, jpgPath);*/
            }

            // 一定不能忘记close
            image.close();
        }
    };
}
