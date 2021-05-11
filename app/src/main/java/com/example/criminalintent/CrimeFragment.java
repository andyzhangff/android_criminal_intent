package com.example.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private ImageButton mPhotoButton;

    public static final String EXTRA_CRIME_ID = "crime_id";
    private static final String TAG = "Crime_Fragment";
    public static final String DIALOG_DATE = "date";
    public static final String DIALOG_TIME = "time";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;

    public static CrimeFragment newInstance(UUID crimeId) {
        
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID)getArguments().getSerializable(EXTRA_CRIME_ID);
        mCrime = CrimeLab.getInstance(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.menu_item_delete_crime:
                CrimeLab.getInstance(getActivity()).deleteCrime(mCrime);
                Intent i = new Intent(getActivity(), CrimeListActivity.class);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();

        mDateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        mTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(fm, DIALOG_TIME);
            }
        });

        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPhotoButton = (ImageButton)v.findViewById(R.id.crime_imageButton);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CrimeCameraActivity.class);
                startActivity(i);
            }
        });

        // if camera is not available, disable camera functionality
        PackageManager pm = getActivity().getPackageManager();
        boolean hasACamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                        Camera.getNumberOfCameras() > 0);
        if (!hasACamera) {
            mPhotoButton.setEnabled(false);
        }
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_DATE || requestCode == REQUEST_TIME ) {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            Log.d(TAG, "the date chosen is: " + date.toString());
            updateDate();
        }
    }

    private void updateDate(){
        String datePattern = "EEEE, MMM dd, yyyy";
        String timePattern = "HH:mm:ss";

        SimpleDateFormat simpleDateFormat =new SimpleDateFormat(datePattern);
        SimpleDateFormat simpleTimeFormat =new SimpleDateFormat(timePattern);

        mDateButton.setText(simpleDateFormat.format(mCrime.getDate()));
        mTimeButton.setText(simpleTimeFormat.format(mCrime.getDate()));
    }

    @Override
    public void onPause() {
        super.onPause();
//        CrimeLab.getInstance(getActivity()).saveCrimes();
        CrimeLab.getInstance(getActivity()).saveCrimesExternalStorage();
    }
}
