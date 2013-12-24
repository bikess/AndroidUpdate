package com.update.updatexml;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;


// 本程序是通过 读取位于Tomcat服务器上的XML文件，获取版本信息，以决定是否进行程序的自动更新
public class MainActivity extends Activity {
    
	public UpdateManager updatemanager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		这里定义版本更新函数的接口，传递参数为当前的活动，当前的activity！
		updatemanager = new UpdateManager(this);
		updatemanager.checkUpdate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
