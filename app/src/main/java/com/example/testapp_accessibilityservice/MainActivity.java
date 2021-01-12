package com.example.testapp_accessibilityservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final String LOGTAG_isAccessibilityEnabled = "isAccessibilityEnabled";
    private final String ACCESSIBILITY_SERVICE_NAME = "com.example.testapp_accessibilityservice/com.example.testapp_accessibilityservice.Test_AccessibilityService";


    private boolean btnflg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStart_MainLayout);
        Button btnEnd = findViewById(R.id.btnEnd_MainLayout);
        Button btnTest1 = findViewById(R.id.btnTest1_MainActivity);
        Button btnTest2 = findViewById(R.id.btnTest2_MainActivity);
        Button btnTest3 = findViewById(R.id.btnTest3_MainActivity);

        btnflg = true;

        btnTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnflg) {
                    btnTest1.setBackgroundColor(Color.RED);
                    btnflg = false;

                }else{
                    btnTest1.setBackgroundColor(Color.BLUE);
                    btnflg = true;

                }
            }
        });

        btnTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnflg) {
                    btnTest2.setBackgroundColor(Color.GREEN);
                    btnflg = false;

                }else{
                    btnTest2.setBackgroundColor(Color.YELLOW);
                    btnflg = true;

                }
            }
        });


        btnTest3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnflg) {
                    btnTest3.setBackgroundColor(Color.BLACK);
                    btnflg = false;

                }else{
                    btnTest3.setBackgroundColor(Color.MAGENTA);
                    btnflg = true;

                }
            }
        });


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                make_SarviceIntent();

            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end__SarviceIntent();
            }
        });
    }

    private void make_SarviceIntent(){
        //ユーザー補助がオンか確認
        if (isAccessibilityEnabled()) {
            Intent i = new Intent(this, Test_AccessibilityService.class);
            i.setAction("ON");
            startService(i);
        }else{
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    private void end__SarviceIntent(){
        if (isAccessibilityEnabled()) {
            Intent i = new Intent(this, Test_AccessibilityService.class);
            i.setAction("OFF");
            startService(i);
        }else{
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
}

    public boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;

        //戻り値
        boolean accessibilityFound = false;
        try {
            //ユーザー補助の確認
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(LOGTAG_isAccessibilityEnabled, "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(LOGTAG_isAccessibilityEnabled, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled==1) {
            //ユーザー補助on
            Log.d(LOGTAG_isAccessibilityEnabled, "***ACCESSIBILIY IS ENABLED***: ");

            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.d(LOGTAG_isAccessibilityEnabled, "Setting: " + settingValue);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    Log.d(LOGTAG_isAccessibilityEnabled, "Setting: " + accessabilityService);
                    //目的のサービスが有効になっているか確認
                    if (accessabilityService.equalsIgnoreCase(ACCESSIBILITY_SERVICE_NAME)){
                        Log.d(LOGTAG_isAccessibilityEnabled, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }

            Log.d(LOGTAG_isAccessibilityEnabled, "***END***");
        }
        else {
            //ユーザー補助off
            Log.d(LOGTAG_isAccessibilityEnabled, "***ACCESSIBILIY IS DISABLED***");
        }
        return accessibilityFound;
    }

}