package com.Android.appSplash;

import com.Android.musicPlayers.MyAppMainActivity;
import com.Android.musicPlayers.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

public class splashActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState);
		//隐藏状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//隐藏标题栏
		getActionBar().hide();
		//加载启动界面
		setContentView(R.layout.activity_splash);
		Thread myThread=new Thread(){//创建子线程
			@Override
			public void run() {
				try{
					sleep(2000);//使程序休眠五秒
					Intent it=new Intent(getApplicationContext(),MyAppMainActivity.class);//启动MainActivity
					startActivity(it);
					finish();//关闭当前活动
					overridePendingTransition(0, 0);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		};
		myThread.start();//启动线程
	}
}
