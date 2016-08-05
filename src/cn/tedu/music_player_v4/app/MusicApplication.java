package cn.tedu.music_player_v4.app;

import java.util.List;

import cn.tedu.music_player_v4.entity.LrcLine;
import cn.tedu.music_player_v4.entity.Music;

import android.app.Application;

/**
 * 当app启动时创建
 */
public class MusicApplication extends Application {
	private List<Music> musicPlayList;
	private int position;
	private List<LrcLine> lrc;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public List<Music> getMusicPlayList() {
		return musicPlayList;
	}

	public List<LrcLine> getLrc() {
		return lrc;
	}

	public void setLrc(List<LrcLine> lrc) {
		this.lrc = lrc;
	}

	public void setMusicPlayList(List<Music> musicPlayList) {
		this.musicPlayList = musicPlayList;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	// 换上一首
	public void preMusic() {
		position = position == 0 ? 0 : position - 1;
	}

	// 换下一首
	public void nextMusic() {
		position = position == musicPlayList.size() - 1 ? 0 : position + 1;
	}

	// 获取当前音乐
	public Music getCurrentMusic() {
		return musicPlayList.get(position);
	}
}
