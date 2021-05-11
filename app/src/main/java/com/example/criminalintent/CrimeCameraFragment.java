package com.example.criminalintent;


import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CrimeCameraFragment extends Fragment {

    private static final String TAG = "CrimeCameraFragment";

    private Camera mCamera;
    private SurfaceView mSurfaceView;

    @SuppressWarnings("deprecation")
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_camera, container, false);

        Button takePictureButton = (Button)v.findViewById(R.id.crime_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                // tell the camera to use this surface as preview area
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error setting up preview display, ", e);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                if (mCamera == null) return;

                // The surface has changed size, update the camera preview size
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
                parameters.setPreviewSize(s.width, s.height);
                mCamera.setParameters(parameters);
                try {
                    mCamera.startPreview();
                } catch (Exception e){
                    Log.e(TAG, "Could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
        });

        return v;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        mCamera = Camera.open();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @SuppressWarnings("deprecation")
    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {
        // compare surfaceview size with available system camera size to get best size
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.height * bestSize.width;
        for (Camera.Size s: sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }
}
