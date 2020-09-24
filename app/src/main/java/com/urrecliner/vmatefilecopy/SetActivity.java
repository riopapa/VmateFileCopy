package com.urrecliner.vmatefilecopy;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import static com.urrecliner.vmatefilecopy.MainActivity.deleteFlag;
import static com.urrecliner.vmatefilecopy.MainActivity.editor;
import static com.urrecliner.vmatefilecopy.MainActivity.timeZone;

public class SetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        final EditText et = findViewById(R.id.timeZone);
        et.setText("" + timeZone);
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                timeZone = Float.parseFloat(et.getText().toString());
                editor.putFloat("timeZone", timeZone).apply();
            }
        });
        Switch sw = findViewById(R.id.delete);
        sw.setChecked(deleteFlag);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                deleteFlag = isChecked;
                editor.putBoolean("delete", deleteFlag).apply();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EditText et = findViewById(R.id.timeZone);
        timeZone = Float.parseFloat(et.getText().toString());
        editor.putFloat("timeZone", timeZone).apply();
    }
}


