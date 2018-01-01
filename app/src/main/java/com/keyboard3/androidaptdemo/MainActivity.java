package com.keyboard3.androidaptdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.keyboard3.apt.Mobclick;
import com.keyboard3.mobclickinject.MobclickInit;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobclickInit.inject(this);
    }

    @Mobclick(value = R.id.btn_ok, type = "click_baidu")
    public void mobclick(View view) {
        Toast.makeText(this, "查看日志输出", Toast.LENGTH_SHORT).show();
    }
}
