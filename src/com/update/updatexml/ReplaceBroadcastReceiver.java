package com.update.updatexml;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

//	采用广播的形式删除文件，当系统监听到文件安装完成的时候就会删除指定文件下下载的文件。
public class ReplaceBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG="APK删除";
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		File downloadApk= new File(Environment.getExternalStorageDirectory()+"/download","UpdateXmL.apk");
		if(downloadApk.exists()){
			downloadApk.delete();
		}
		Log.i(TAG, "下载的文件已经被删除了");
	}

}
