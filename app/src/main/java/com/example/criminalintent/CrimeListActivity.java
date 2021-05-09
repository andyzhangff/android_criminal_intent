package com.example.criminalintent;

import android.os.Bundle;
import android.view.Menu;

import androidx.fragment.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

}
