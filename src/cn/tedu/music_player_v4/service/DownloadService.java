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
 * ִ���������ֲ�����Service
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
	 * �÷����ڹ����߳���ִ��, �ڸ÷�����
	 * ����ֱ�����к�ʱ���롣
	 * ʲôʱ��ִ�и÷�����
	 * ������startService������ǰservice��
	 * �������Ҫ��ɺ�ʱ����������Ϣ���С�
	 * ����Ϣ���н��ᱻ�����߳��е�Looper��ѭ
	 * ����˳��ִ�С�
	 *  
	 *  ���԰Ѳ���ͨ��intent���󴫵ݹ���
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
			if(target.exists()){ //�Ѿ����ع���
				Log.i("info", "�Ѿ����ع���....");
				return;
			}
			//��Ŀ¼������  ������Ŀ¼
			if(!target.getParentFile().exists()){
				target.getParentFile().mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(target);
			//ִ���������ֲ���
			sendNotification("���ֿ�ʼ����", "", 100, 0, true);
			HttpURLConnection conn=(HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			InputStream is = conn.getInputStream(); 
			//�߶�  �� ���浽�ļ���
			byte[] buffer = new byte[1024*100];
			int length = 0;
			int current = 0; 
			//����˷��ص���������
			int total = Integer.parseInt(conn.getHeaderField("Content-Length"));
			while((length=is.read(buffer)) != -1){
				//���ļ��������д����Ӧ����
				fos.write(buffer, 0 , length);
				fos.flush();
				current += length;
				//ʹ��֪ͨ���� ������
				sendNotification("", "", total, current, false);
			}
			fos.close();
			//���³���֪ͨ�Ĺ�����Ϣ
			cancelNotification();
			sendNotification("�����������", "�����������", 0, 0, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ����֪ͨ
	 */
	public void sendNotification(String ticker, String text, int max, int progress, boolean i){
		NotificationManager manager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		Notification.Builder builder = new Builder(this);
		builder.setTicker(ticker)
			.setContentTitle("��������")
			.setContentText(text)
			.setSmallIcon(R.drawable.ic_launcher);
		builder.setProgress(max, progress, i);
		manager.notify(NOTIFICATION_ID, builder.build());
	}
	
	//���֪ͨ
	public void cancelNotification(){
		NotificationManager manager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}
	
}



