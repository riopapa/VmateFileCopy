package com.urrecliner.vmatefilecopy;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Activity mActivity;
    Context mContext;
    String srcFolder = "Vmate/sd/DCIM/100HSCAM";
    String dstFolder = "vmate";
    File srcFullPath = new File(Environment.getExternalStorageDirectory(), srcFolder);
    File dstFullPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), dstFolder);
    TextView srcDst, result;
    File[] srcFiles = null;
    String srcFileName, dstFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        mContext = this;
        askPermission();
        readyFolder(srcFullPath);
        readyFolder(dstFullPath);
        srcDst = findViewById(R.id.srcDst);
        String txt = "Source : "+srcFolder+"\nDestination : DCIM/"+dstFolder;
        srcDst.setText(txt);
        result = findViewById(R.id.result);
        listUp_files();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.fileCopy) {
            try {
                new run_fileCopy().execute("");
            } catch (Exception e) {
                Log.e("Err",e.toString());
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void listUp_files() {
        srcFiles = srcFullPath.listFiles();
        if (srcFiles == null)
            return;
        Arrays.sort(srcFiles);
        StringBuilder sb = new StringBuilder();
        for (File file: srcFiles) {
            String fileName = file.getName();
            if (!fileName.substring(0,1).equals(".")) {
                sb.append(fileName);
                sb.append("\n");
            }
        }
        result.setText(sb);
    }

    void readyFolder (File dir){
        try {
            if (!dir.exists()) dir.mkdirs();
        } catch (Exception e) {
            Log.e("creating Folder error", dir + "_" + e.toString());
        }
    }

    class run_fileCopy extends AsyncTask<String, String, Void> {

        int count;
        @Override
        protected void onPreExecute() {
            count = 0;
            result.setText("");
        }

        @Override
        protected Void doInBackground(String... inputParams) {
            for (File srcFile : srcFiles) {
                srcFileName = srcFile.getName();
                if (!srcFileName.substring(0, 1).equals(".")) {
                    try {
                        file_copy(srcFile);
                        publishProgress();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            srcFiles[count] = new File(dstFolder, dstFileName);
            StringBuilder sb = new StringBuilder();
            for (File srcFile : srcFiles) {
                srcFileName = srcFile.getName();
                sb.append(srcFileName).append("\n");
            }
            result.setText(sb);
            result.invalidate();
            count++;
        }

        @Override
        protected void onPostExecute(final Void statistics) {
            Toast.makeText(mContext, "Copy Completed", Toast.LENGTH_SHORT).show();
        }
    }

    void file_copy(File srcFile) throws IOException {
        String srcName = srcFile.getName();
        Date srcDate = new Date(srcFile.lastModified());
        final SimpleDateFormat sdfDateTimeLog = new SimpleDateFormat("YYMMdd_HHmmss", Locale.getDefault());
        dstFileName = sdfDateTimeLog.format(srcDate)+srcName.substring(srcName.length()-4);

        File dstFile = new File (dstFullPath, dstFileName);
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = new FileInputStream(srcFile).getChannel();
            dstChannel = new FileOutputStream(dstFile).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        }finally{
            srcChannel.close();
            dstChannel.close();
            dstFile.setLastModified(srcDate.getTime());
        }
    }

    // ↓ ↓ ↓ P E R M I S S I O N    RELATED /////// ↓ ↓ ↓ ↓
    ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();

    private void askPermission() {
//        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (permissionsToRequest.size() != 0) {
            requestPermissions(permissionsToRequest.toArray(new String[0]),
//            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    ALL_PERMISSIONS_RESULT);
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList <String> result = new ArrayList<String>();
        for (String perm : wanted) if (hasPermission(perm)) result.add(perm);
        return result;
    }
    private boolean hasPermission(String permission) {
        return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
    }

    //    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (String perms : permissionsToRequest) {
                if (hasPermission(perms)) {
                    permissionsRejected.add(perms);
                }
            }
            if (permissionsRejected.size() > 0) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
            }
            else
                Toast.makeText(mContext, "Permissions not granted.", Toast.LENGTH_LONG).show();
        }
    }
    private void showDialog(String msg) {
        showMessageOKCancel(msg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(permissionsRejected.toArray(
                                new String[0]), ALL_PERMISSIONS_RESULT);
                    }
                });
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

// ↑ ↑ ↑ ↑ P E R M I S S I O N    RELATED /////// ↑ ↑ ↑

}