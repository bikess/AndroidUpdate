package com.update.updatexml;


// 此更新类，是使用异步任务类AsyncTask<>来实现下载更新展示，利用AsyncTask类实现更加简单，方便，无需手动创建线程就可以实现线程间的异步操作，非常好用
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;



import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


public class UpdateManager2 {
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

    // 定义下载对话框
    Dialog downloadDialog;
    
    // 软件下载位置
    private String savePath;
	public UpdateManager2(Context context){
		this.mcontext = context;
	}
	
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
		Toast.makeText(mcontext, "下载完成", Toast.LENGTH_SHORT);
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
		final String sdPath = Environment.getExternalStorageDirectory()+"/";
		savePath = sdPath+"download";
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
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					savePath = sdPath+"download";
				}
				new updateTask().execute();
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
	
//	自定义一个异步任务类，继承自异步任务类AsyncTask,异步任务类AsyncTask<参数1,参数2,参数3>，三个参数分别为：执行参数  进度参数  结果参数
//	执行参数为doInBackground执行时的传入参数
	private class updateTask extends AsyncTask<Void, Integer, Void>{

		private Boolean cancel = false;
		/* 执行那些很耗时的后台计算工作。可以调用publishProgress方法来更新实时的任务进度。 */ 
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
//			异步任务执行的时候调用，进行下载，此任务就是异步任务的执行函数，此函数中进行异步任务的执行
			Log.i(Update_tag, "进入异步任务执行函数中。。。");
			try{
					URL url = new URL(downurl);
					//创建http连接
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					//获取资源的总长度
					int length = conn.getContentLength();
					//创建输入流
					InputStream is = conn.getInputStream();
//					当前下载的长度
					int currentLength = 0;
					File file = new File(savePath);
//					判断目录是否存在
					if(!file.exists()){
						file.mkdir();
					}
					File apkFile = new File(savePath, serAppName+".apk");
					FileOutputStream fos = new FileOutputStream(apkFile);
//					创建中转数组
					byte buf[] = new byte[1024];
					//写入文件中
					while((is.read(buf)!=-1)&&!cancel){
						fos.write(buf);
						fos.flush();				
//						每当fos。write（buf）一次，那么本地文件的大小就增加了buf数组的长度
						currentLength+=buf.length;
//						更新进度，传入进度参数，这里为Integer
						this.publishProgress(currentLength,length);
					}
					fos.close();
					is.close();
				}
			catch (MalformedURLException e) {
				// TODO: handle exception
				e.printStackTrace();
			}catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			//异步任务执行完毕的时候调用:关闭提示框，当异步任务顺利执行完成后调用此方法
			Log.i(Update_tag, "进入异步任务执行完成后函数中。。。");
			super.onPostExecute(result);
			downloadDialog.dismiss();
//			调用安装函数进行安装
			installApk();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
//			异步任务准备执行的时候调用：创建对话框并且显示, 此函数是异步任务执行前的准备工作，可以进行UI界面等的准备工作
			super.onPreExecute();
			Log.i(Update_tag, "进入异步任务执行前的准备函数中。。。");
			AlertDialog.Builder builder = new Builder(mcontext);
			builder.setTitle(R.string.soft_update_down);
//			 给对话框添加进度条
			final LayoutInflater inflater = LayoutInflater.from(mcontext);
			View v = inflater.inflate(R.layout.softupdate_progress, null);
			mProgressBar = (ProgressBar) v.findViewById(R.id.update_progress);
			mProgressBar.setMax(0);
			builder.setView(v);
			builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					Log.i(Update_tag, "更新取消");
					cancel(true);
					cancel=true;
				/*
				 * 在任何时候只要执行cancel(boolean) 方法，当前任务就会被取消，
				 * 随后引发isCancelled() 方法的执行，该方法返回true.isCancelled()
				 * 方法执行之后，任务不再执行onPostExecute() ，而是执行onCancelled(Object) 方法
				 */
				}
			});
			downloadDialog = builder.create();
			downloadDialog.show();
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			//此函数用于异步任务执行的时候，进行异步任务执行界面的更新
			Log.i(Update_tag, "进入异步任务执行更新函数中。。。");
			//调用 publishProgress 方法的时候执行，用来操作前台更新页面
//			设置进度条的最大值
			mProgressBar.setMax(values[1]);
//			设置进度条当前的值
			mProgressBar.setProgress(values[0]);
		}
		@Override
		protected void onCancelled(Void result) {
			// TODO Auto-generated method stub
			Log.i(Update_tag, "进入异步任务执行取消函数。。。");
			
		}
		
		
	
	}

}
