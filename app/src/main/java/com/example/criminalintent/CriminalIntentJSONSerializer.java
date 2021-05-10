package com.example.criminalintent;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class CriminalIntentJSONSerializer {

    private Context mContext;
    private String mFileName;
    private String mStorageStatus;
    private File mTargetDirectoryPath;
    private File mTargetFilePath;

    private final static String TAG = "JSONSerializer";

    public CriminalIntentJSONSerializer(Context c, String f) {
        mContext = c;
        mFileName = f;
    }

    public void saveCrimes(ArrayList<Crime> crimes)
    throws JSONException, IOException {
        // build an array in JSON
        JSONArray array = new JSONArray();
        for (Crime c: crimes) array.put(c.toJSON());

        // write file to disk
        OutputStreamWriter writer = null;
        try{
            OutputStream out = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());

        } finally {
            if (writer != null) writer.close();
        }
    }

    public void saveCrimesExternalStorage(ArrayList<Crime> crimes)
            throws IOException, JSONException {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.d(TAG, "external storage is read only or not available!");
            return;
        }

        // build an array in JSON
        JSONArray array = new JSONArray();
        for (Crime c: crimes) array.put(c.toJSON());

        FileOutputStream fos = null;
        mTargetDirectoryPath = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        mTargetFilePath = new File(mTargetDirectoryPath, mFileName);

        try{
            fos = new FileOutputStream(mTargetFilePath);
            fos.write(array.toString().getBytes());
        } finally {
            if (fos != null) fos.close();
        }

    }

    public ArrayList<Crime> loadCrimes() throws IOException, JSONException {
        ArrayList<Crime> crimes = new ArrayList<Crime>();
        BufferedReader reader = null;
        try {
            // open and read file into StringBuilder
            InputStream in = mContext.openFileInput(mFileName);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // line breaks are omitted and irrelevant
                jsonString.append(line);
            }
            // parse the JSON using JSONTokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
            // build the array of crimes from JSONObjects
            for (int i = 0; i < array.length(); i++) {
                crimes.add(new Crime(array.getJSONObject(i)));
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return crimes;
    }

    private boolean isExternalStorageReadOnly() {
        mStorageStatus = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(mStorageStatus)) {
            Log.d(TAG,  "SD card is ready only.");
            return true;
        }
        return false;
    }

    private boolean isExternalStorageAvailable() {
        mStorageStatus = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(mStorageStatus)) {
            Log.d(TAG,  "SD card is writeable.");
            return true;
        }
        return false;
    }
}
