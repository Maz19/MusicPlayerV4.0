package cn.tedu.music_player_v4.adapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.tedu.music_player_v4.R;
import cn.tedu.music_player_v4.entity.Music;
import cn.tedu.music_player_v4.util.BitmapUtils;
import cn.tedu.music_player_v4.util.HttpUtils;
import cn.tedu.music_player_v4.util.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 音乐列表适配器
 */
public class MusicAdapter extends BaseAdapter {
	private Context context;
	private List<Music> musics;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;

	public MusicAdapter(Context context, List<Music> musics, ListView listView) {
		this.context = context;
		this.musics = musics;
		this.inflater = LayoutInflater.from(context);
		this.imageLoader = new ImageLoader(context, listView);
	}

	@Override
	public int getCount() {
		return musics.size();
	}

	@Override
	public Music getItem(int position) {
		return musics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_lv_music, null);
			holder = new ViewHolder();
			holder.ivPic = (ImageView) convertView.findViewById(R.id.ivPic);
			holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			holder.tvSinger = (TextView) convertView
					.findViewById(R.id.tvSinger);
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		// 给holder中控件赋值
		Music m = getItem(position);
		holder.tvName.setText(m.getTitle());
		holder.tvSinger.setText(m.getArtist_name());
		// 显示图片
		imageLoader.displayImage(holder.ivPic, m.getPic_small());
		return convertView;
	}

	class ViewHolder {
		ImageView ivPic;
		TextView tvName;
		TextView tvSinger;
	}

	/**
	 * 停止线程
	 */
	public void stopThread() {
		imageLoader.stopThread();
	}

}
