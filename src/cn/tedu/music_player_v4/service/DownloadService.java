package cn.tedu.music_player_v4.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.tedu.music_player_v4.R;
import cn.tedu.music_player_v4.util.HttpUtils;
import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;
import android.widget.Toast;

/**
 * 执行下载音乐操作的Service
 */
public class DownloadService extends IntentService{
	
	private static final int NOTIFICATION_ID = 100;

	public DownloadService(){
		super("download");
	}
	
	public DownloadService(String name) {
		super(name);
	}

	/**
	 * 该方法在工作线程中执行, 在该方法中
	 * 可以直接运行耗时代码。
	 * 什么时候执行该方法？
	 * 当调用startService启动当前service后，
	 * 将会把需要完成耗时操作存入消息队列。
	 * 该消息队列将会被工作线程中的Looper轮循
	 * 并且顺序执行。
	 *  
	 *  可以把参数通过intent对象传递过来
	 *  intent.putExtra()
	 *  intent.getXXXExtra()
	 */
	protected void onHandleIntent(Intent intent) {
		String url = intent.getStringExtra("url");
		String title = intent.getStringExtra("title");
		String bit = intent.getStringExtra("bit");
		try {
			String filename = "_"+bit+"/"+title+".mp3";
			File target = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), filename);
			if(target.exists()){ //已经下载过了
				Log.i("info", "已经下载过了....");
				return;
			}
			//父目录不存在  创建父目录
			if(!target.getParentFile().exists()){
				target.getParentFile().mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(target);
			//执行下载音乐操作
			sendNotification("音乐开始下载", "", 100, 0, true);
			HttpURLConnection conn=(HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			InputStream is = conn.getInputStream(); 
			//边读  边 保存到文件中
			byte[] buffer = new byte[1024*100];
			int length = 0;
			int current = 0; 
			//服务端返回的总数据量
			int total = Integer.parseInt(conn.getHeaderField("Content-Length"));
			while((length=is.read(buffer)) != -1){
				//向文件输出流中写出相应数据
				fos.write(buffer, 0 , length);
				fos.flush();
				current += length;
				//使用通知更新 进度条
				sendNotification("", "", total, current, false);
			}
			fos.close();
			//重新出现通知的滚动消息
			cancelNotification();
			sendNotification("音乐下载完成", "音乐下载完成", 0, 0, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送通知
	 */
	public void sendNotification(String ticker, String text, int max, int progress, boolean i){
		NotificationManager manager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		Notification.Builder builder = new Builder(this);
		builder.setTicker(ticker)
			.setContentTitle("音乐下载")
			.setContentText(text)
			.setSmallIcon(R.drawable.ic_launcher);
		builder.setProgress(max, progress, i);
		manager.notify(NOTIFICATION_ID, builder.build());
	}
	
	//清除通知
	public void cancelNotification(){
		NotificationManager manager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}
	
}



