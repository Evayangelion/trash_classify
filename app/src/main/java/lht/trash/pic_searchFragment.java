package lht.trash;

import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class pic_searchFragment extends Fragment {
    TextureView textureView;

    public pic_searchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pic_search, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //getView().findViewById(R.id.bt_pic_to_text).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_pic_searchFragment_to_text_searchFragment));
        //getView().findViewById(R.id.bt_pic_to_voice).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_pic_searchFragment_to_voice_searchFragment));

        textureView=getView().findViewById(R.id.textureView);

        textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                updateTransform();
            }
        });
        textureView.post(new Runnable(){

            @Override
            public void run() {
                startCamera();
            }
        });
    }
    private void updateTransform(){
        Matrix matrix = new Matrix();
        float centerX=textureView.getWidth()/2f;
        float centerY = textureView.getHeight() / 2f;

        float[] rotations = {0,90,180,270};
        // Correct preview output to account for display rotation
        float rotationDegrees = rotations[textureView.getDisplay().getRotation()];

        matrix.postRotate(-rotationDegrees, centerX, centerY);

        // Finally, apply transformations to our TextureView
        textureView.setTransform(matrix);
    }
    private void startCamera() {
        // 1. preview
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(new Rational(1, 1))
                .setTargetResolution(new Size(640,640))
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) textureView.getParent();
                parent.removeView(textureView);
                parent.addView(textureView, 0);

                textureView.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });

        CameraX.bindToLifecycle(this, preview);
    }
}
