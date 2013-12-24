package com.update.updatexml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class UpdateManager {
	public Context mcontext;
	
	public String Update_tag = "更新测试";
	
	public int nowVerCode=0;
	public String nowVerName="";
	public String nowAppName="";
	
	String serverPath = "http://10.103.30.69:8080/UpdateXml/";
	
	public HashMap<String, String> hashMap;
	public int serVerCode=0;
	public String serVerName="";
	public String serAppName="";
	public String downurl="";
	
	public ProgressBar mProgressBar;
	int progress;
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
	
    private Boolean cancelupdate = false;
    //定义下载对话框
    Dialog downloadDialog;
//  软件下载位
    private String savePath;
    private Thread thread=null;
	public UpdateManager(Context context){
		this.mcontext = context;
	}
	
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            // 正在下载
            case DOWNLOAD:
                // 设置进度条位置
                mProgressBar.setProgress(progress);
                break;
            case DOWNLOAD_FINISH:
                // 安装文件
            	Log.i(Update_tag, "下载完成，开始安装");
                installApk();
                break;
            default:
                break;
            }
        }
    };
	//此函数是进行版本更新的函数
	public void checkUpdate() {
		Log.i(Update_tag, "进入版本更新函数");
		// TODO Auto-generated method stub
//		检查版本是否需要更新，验证方式是比较服务器版本与本地版本
		if(isUpdate()){
//			需要更新，则弹出更新对话框
			Log.i(Update_tag, "需要更新，弹出更新对话框");
			showUpdateDialog();
		}
//		不需要更新
		else{
			Toast.makeText(mcontext, R.string.most_new,Toast.LENGTH_SHORT).show();
		}
		Log.i(Update_tag, "版本更新完成");
	}


	protected void installApk() {
		// TODO Auto-generated method stub
		Log.i(Update_tag, "进入应用安装函数");
//		通过Intent安装APK文件
		Intent install = new Intent(Intent.ACTION_VIEW);
		install.setDataAndType(Uri.fromFile(new File(savePath, serAppName+".apk")), "application/vnd.android.package-archive");
		Log.i(Update_tag, "安装完成");
		mcontext.startActivity(install);
		
		Log.i(Update_tag, "安装完成");
	}


	private void showUpdateDialog() {
		// TODO Auto-generated method stub
		Log.i(Update_tag, "显示更新对话框函数");
		AlertDialog.Builder builder = new Builder(mcontext);
		builder.setTitle(R.string.soft_update_title);
		builder.setMessage(R.string.soft_update_message);
		builder.setPositiveButton(R.string.soft_update_button,  new OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				arg0.dismiss();
				Log.i(Update_tag, "立即更新，显示下载对话框");
//				显示下载对话框
				showDownLoadDialog();
			}
			
		});
		builder.setNegativeButton(R.string.soft_update_later, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.i(Update_tag, "稍后更新");
				dialog.dismiss();
			}
		});
		Dialog updatedDialog = builder.create();
		updatedDialog.show();
	}


	protected void showDownLoadDialog() {
		// TODO Auto-generated method stub
		Log.i(Update_tag, "进入软件下载对话框函数");
		AlertDialog.Builder builder = new Builder(mcontext);
		builder.setTitle(R.string.soft_update_down);
//		 给对话框添加进度条
		final LayoutInflater inflater = LayoutInflater.from(mcontext);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgressBar = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		
		builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				Log.i(Update_tag, "更新取消");
//				点击取消，将取消下载状态置为true；
//				中断下载进程！！，中断下载进程后，下载终止，包未下载完成。
				thread.interrupt();
				
//				此时需要删除对应下载的安装包
				
				cancelupdate = true;
			}
		});
		downloadDialog = builder.create();
		downloadDialog.show();
//		进入下载文件函数
		downLoadApk();
		Log.i(Update_tag, "软件对话框函数运行完成");
	}


	private void downLoadApk() {
		// TODO Auto-generated method stub
		//一旦开始新的进程，必然要重新启动一个线程
		Log.i(Update_tag, "启动新进程，进行下载文件");
		 // 启动新线程下载软件
        thread = new downLoadApkThread();
        thread.start();
		
	}
	
	private class downLoadApkThread extends Thread{
		public void run(){
			try{
				Log.i(Update_tag, "进入apk下载线程中");
				//判断SD卡是否存在，并且是否具有读写的权限
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					//获得存储卡的路径
					String sdPath = Environment.getExternalStorageDirectory()+"/";
					savePath = sdPath+"download";
					
					URL url = new URL(downurl);
					//创建http连接
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					//获取文件大小
					int length = conn.getContentLength();
					//创建输入流
					InputStream is = conn.getInputStream();
					
					File file = new File(savePath);
//					判断目录是否存在
					if(!file.exists()){
						file.mkdir();
					}
					File apkFile = new File(savePath, serAppName+".apk");
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					byte buf[] = new byte[1024];
					//写入文件中
					do{
						int numread = is.read(buf);
						count +=numread;
						//计算进度条的位置
						progress =(int)(((float) count /length)*100);
//						更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if(numread<=0){
							//下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							Log.i(Update_tag, "顺利下载完成");
							break;
						}
//						写入文件
						fos.write(buf,0,numread);
					}while(!cancelupdate);//点击取消就停止下载
					fos.close();is.close();
				}
			}catch (MalformedURLException e) {
				// TODO: handle exception
				e.printStackTrace();
			}catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			//  取消下载对话框的显示
	        downloadDialog.dismiss();
	        Log.i(Update_tag, "下载函数执行完成");
			
		}
	}

	private boolean isUpdate() {
		// TODO Auto-generated method stub
		Log.i(Update_tag, "进入版本比较函数");
//		首先获取目前程序的版本
		if(getVerSion(mcontext)){
			if(getServerVersion(serverPath)){
				if(serVerCode>nowVerCode){
					Log.i(Update_tag, "服务器版本高，需要更新");
					return true;
				}
			}
		}
		Log.i(Update_tag, "版本比较完成");
		return false;
	}


	public InputStream getInputStreamFromUrl(String urlStr)
	        throws MalformedURLException, IOException {
		Log.i(Update_tag, "获得与服务器连接函数");
	    URL url = new URL(urlStr);
//	    获得与对应服务器地址的Url连接
	    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
//	    获得这个连接的输入流  
	    InputStream inputStream = urlConn.getInputStream();
	    Log.i(Update_tag, "取得与指定服务器地址的http连接，并返回该连接的输入流");
	    return inputStream;
	}
	private boolean getServerVersion(String serverPath) {
		// TODO Auto-generated method stub
		Log.i(Update_tag, "获取服务器上版本函数");
//		从指定的服务器地址中取得与服务器的连接，并获得服务器的输出流
		String xmlPath = serverPath+"version.xml";
		try {
			InputStream inputStream = getInputStreamFromUrl(xmlPath);
			hashMap = new HashMap<String, String>();
			ParseXmlService pxs = new ParseXmlService();
			try {
				hashMap = pxs.parseXml(inputStream);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serVerCode = Integer.valueOf(hashMap.get("versioncode"));
		serVerName = hashMap.get("versionname");
		serAppName = hashMap.get("appname");
		downurl = hashMap.get("downurl");
		
		Log.i(Update_tag, "获取服务器上版本函数完成"+Integer.toString(serVerCode));
		return true;
	}


	private boolean getVerSion(Context context) {
		// TODO Auto-generated method stub
		Log.i(Update_tag, "获取目前的程序版本函数");
		
		try{
			nowVerCode = context.getPackageManager().getPackageInfo("com.update.updatexml", 0).versionCode;
			nowVerName = context.getPackageManager().getPackageInfo("com.update.updatexml", 0).versionName;
			nowAppName = context.getResources().getText(R.string.app_name).toString();
		}catch(NameNotFoundException e){
			e.printStackTrace();
		}
		Log.i(Update_tag, "获取当前程序版本函数完成，目前的函数版本为"+Integer.toString(nowVerCode));
		return true;
	}

}
