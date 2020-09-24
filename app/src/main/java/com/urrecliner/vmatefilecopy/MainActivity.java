package com.urrecliner.vmatefilecopy;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Activity mActivity;
    Context mContext;
    String srcFolder = "Vmate/sd/DCIM/100HSCAM";
    String dstFolder = "vmate";
    File srcFullPath = new File(Environment.getExternalStorageDirectory(), srcFolder);
    File cameraFullPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"");
    File dstFullPath = new File(cameraFullPath, dstFolder);
    TextView srcDst, result;
    File[] srcFiles = null;
    long [] sizes;
    DecimalFormat formatterKb = new DecimalFormat("###,###Kb");
    DecimalFormat formatterMb = new DecimalFormat("###,###Mb");
    DecimalFormat formatterGb = new DecimalFormat("###,###.## Gb");
    String srcFileName, dstFileName;
    static SharedPreferences sharedPref;
    static SharedPreferences.Editor editor;
    static float timeZone;
    static boolean firstTime, deleteFlag, yesNo= false;
    final SimpleDateFormat sdfDateTime = new SimpleDateFormat("YYYYMMdd_HHmmss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        mContext = this;
        askPermission();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        srcDst = findViewById(R.id.srcDst);
        result = findViewById(R.id.result);
        readyFolder(srcFullPath);
        readyFolder(dstFullPath);
        listUp_files();
        sharedPref = getApplicationContext().getSharedPreferences("vmate", MODE_PRIVATE);
        editor = sharedPref.edit();
        timeZone = sharedPref.getFloat("timeZone",-99f);
        firstTime = sharedPref.getBoolean("firstTime",true);
        deleteFlag = sharedPref.getBoolean("delete", false);
        result.setMovementMethod(new ScrollingMovementMethod());
        if (firstTime) {
            firstTime = false;
            editor.putBoolean("firstTime", false).apply();
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        }
        if (timeZone == -99f) {
            Intent intent = new Intent(this, SetActivity.class);
            startActivity(intent);
        }
        else {
            String txt = "Source : "+srcFolder+"\nDestination : "+ cameraFullPath.getName()+"/"+dstFolder+"\nTime Zone : "+ timeZone +
                    "\n"+sampleTimeShift();
            srcDst.setText(txt);
        }
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
            yes4FileCopy();
            return true;
        }
        else if (id == R.id.setting) {
            Intent intent = new Intent(this, SetActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    void yes4FileCopy() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Click Go to start File Copy");
        String s = sampleTimeShift()+"\n";
        if (deleteFlag)
            s += "<<< Remarks >>>\nEach files in source will be deleted after copying..";
        builder.setMessage(s);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    new run_fileCopy().execute("");
                } catch (Exception e) {
                    Log.e("Err", e.toString());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String sampleTimeShift() {
        if (srcFiles.length> 0) {
            long dateTime = srcFiles[0].lastModified();
            String srcName = srcFiles[0].getName();
            return srcName+"\n  => "+sdfDateTime.format(dateTime- (long) (timeZone *60*60*1000))
                    +srcName.substring(srcName.length()-4);
        }
        return "";
    }

    void listUp_files() {
        int idx = 0;

        srcFiles = srcFullPath.listFiles();
        if (srcFiles == null)
            return;
        Arrays.sort(srcFiles);
        sizes = new long[srcFiles.length];
        StringBuilder sb = new StringBuilder();
        for (File file: srcFiles) {
            String fileName = file.getName();
            sizes[idx] = file.length() / 1024;
            if (!fileName.substring(0,1).equals(".")) {
                sb.append(fileName).append("  ");
                sb.append(calcSize(sizes[idx]));
                sb.append("\n");
            }
            idx++;
        }
        result.setText(sb);
    }

    String calcSize(long siz) {
        float howBig = (float) siz;
        if (howBig < 5000)
            return(formatterKb.format(howBig));
        else if (howBig < 1000000)
            return formatterMb.format(howBig/1024);
        else
            return formatterGb.format(howBig/1024/1024);

    }
    void readyFolder (File dir){
        try {
            if (!dir.exists()) dir.mkdirs();
        } catch (Exception e) {
            Log.e("creating Folder error", dir + "_" + e.toString());
        }
    }

    class run_fileCopy extends AsyncTask<String, Integer, Void> {

        int count;
        @Override
        protected void onPreExecute() {
            count = 0;
            result.setText("");
            SystemClock.sleep(10);
        }

        @Override
        protected Void doInBackground(String... inputParams) {
            for (int idx = 0; idx < srcFiles.length; idx++) {
                srcFileName = srcFiles[idx].getName();
                Log.w("file",srcFileName);
                if (!srcFileName.substring(0, 1).equals(".")) {
                    try {
                        publishProgress(idx);
                        file_copy(srcFiles[idx]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            int currIdx = values[0];
            srcFiles[count] = new File(dstFolder, dstFileName);
            result.setText(listUpFiles(currIdx));
            count++;
        }

        @Override
        protected void onPostExecute(final Void statistics) {
            result.setText(listUpFiles(-1));
            Toast.makeText(mContext, "Copy Completed", Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                public void run() {
                    finish();
                    finishAffinity();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }, 3000);
        }
    }

    private SpannableString listUpFiles(int currIdx) {
        int sPos = 0, fPos = 0;
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < srcFiles.length; idx++) {
            if (currIdx == idx)
                sPos = sb.length();
            srcFileName = srcFiles[idx].getName();
                sb.append(srcFileName).append("  ");
                sb.append(calcSize(sizes[idx]));
//            sb.append((currIdx == idx)? " done.":"");
            if (currIdx == idx)
                fPos = sb.length();
            sb.append("\n");
        }
        SpannableString ss = new SpannableString(sb);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), sPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sPos > 0) {
            ss.setSpan(new ForegroundColorSpan(Color.GRAY), 0, sPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StrikethroughSpan(), 0, sPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        ss.setSpan(new StyleSpan(Typeface.BOLD), sPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new RelativeSizeSpan(1.2f), sPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    void file_copy(File srcFile) throws IOException {
        String srcName = srcFile.getName();
        long srcDate = srcFile.lastModified()- (long) (timeZone *60*60*1000);
        dstFileName = sdfDateTime.format(srcDate)+srcName.substring(srcName.length()-4);

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
        }
        Path path = Paths.get(dstFile.toString());
        FileTime stamp = FileTime.fromMillis(srcDate);
        try {
            Files.setAttribute(path, "creationTime", stamp);
            Files.setAttribute(path, "lastAccessTime", stamp);
            Files.setAttribute(path, "lastModifiedTime", stamp);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (deleteFlag)
            srcFile.delete();
//        try {
//            attr = Files.readAttributes(path, BasicFileAttributes.class);
//            FileTime fAccess = attr.lastAccessTime();
//            FileTime fCreate = attr.creationTime();
//            FileTime fModified = attr.lastModifiedTime();
//            Log.w("Date", "access="+fAccess+" create="+fCreate+" modi="+fModified);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
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
//            else
//                Toast.makeText(mContext, "Permissions not granted.", Toast.LENGTH_LONG).show();
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