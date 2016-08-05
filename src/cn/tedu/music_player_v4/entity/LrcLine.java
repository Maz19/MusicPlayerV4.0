package cn.tedu.music_player_v4.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述一个歌词行
 */
public class LrcLine {
	private String time; // 时间
	private String content; // 内容
	private static SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

	public LrcLine(String time, String content) {
		super();
		this.time = time;
		this.content = content;
	}

	public LrcLine() {
		// TODO Auto-generated constructor stub
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * 判断参数time描述的时间与time属性是否一致
	 * @param time  毫秒值
	 */
	public boolean equalsTime(int time){
		//t :   03:30
		String t = sdf.format(new Date(time));
		// time: 03:30.33
		if(this.time.startsWith(t)){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "["+time+"]"+content+"\n";
	}
}
