package com.example.criminalintent;

import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

public class CrimeCameraActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment() {
        return new CrimeCameraFragment();
    }
}
