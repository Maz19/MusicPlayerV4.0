package cn.tedu.music_player_v4.service;

import java.io.IOException;

import cn.tedu.music_player_v4.util.GlobalConsts;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;

/**
 * 播放音乐的service
 */
public class PlayMusicService extends Service{
	private MediaPlayer player;
	private boolean isLoop=true;

	@Override
	public void onCreate() {
		//初始化播放器
		player = new MediaPlayer();
		player.setOnPreparedListener(new OnPreparedListener() {
			//prepare完成后  执行该方法
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				//发送自定义广播  告诉Activity 音乐已经开始播放
				Intent i = new Intent(GlobalConsts.ACTION_START_PLAY);
				sendBroadcast(i);
			}
		});
		//启动工作线程  每1秒给Activity发一次广播
		new WorkThread().start();
	}
	
	//每1秒给Activity发一次广播
	class WorkThread extends Thread{
		@Override
		public void run() {
			while(isLoop){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//发送广播
				if(player.isPlaying()){
					Intent i = new Intent(GlobalConsts.ACTION_UPDATE_PROGRESS);
					i.putExtra("current", player.getCurrentPosition());
					i.putExtra("total", player.getDuration());
					sendBroadcast(i);
				}
			}
		}
	}
	
	/**
	 * 当有客户端绑定该service时  执行 
	 * context.bindService()
	 */
	public IBinder onBind(Intent intent) {
		return new MusicBinder();
	}

	/**
	 * 返回给客户端的binder对象
	 * 声明开放给客户端调用的接口方法
	 */
	public class MusicBinder extends Binder{
		
		/**
		 * 跳转到某个播放位置
		 * @param progress
		 */
		public void seekTo(int progress){
			player.seekTo(progress);
		}
		
		//暂停或播放
		public void playOrPause(){
			if(player.isPlaying()){
				player.pause();
			}else{
				player.start();
			}
		}
		
		/**
		 * 播放音乐
		 * @param url  音乐的路径
		 */
		public void playMusic(String url){
			try {
				player.reset();
				player.setDataSource(url);
				//异步加载音乐信息
				player.prepareAsync();
				//在player准备完成后  执行start播放
				//给player设置监听
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}


