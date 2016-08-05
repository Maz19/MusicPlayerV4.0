package cn.tedu.music_player_v4.fragment;

import java.util.List;

import cn.tedu.music_player_v4.R;
import cn.tedu.music_player_v4.adapter.MusicAdapter;
import cn.tedu.music_player_v4.app.MusicApplication;
import cn.tedu.music_player_v4.entity.Music;
import cn.tedu.music_player_v4.entity.SongInfo;
import cn.tedu.music_player_v4.entity.SongUrl;
import cn.tedu.music_player_v4.model.MusicModel;
import cn.tedu.music_player_v4.model.MusicModel.Callback;
import cn.tedu.music_player_v4.service.PlayMusicService.MusicBinder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

/**
 * ��ʾ�ȸ���
 */
public class HotMusicFragment extends Fragment{
	private ListView listView;
	private MusicAdapter adapter;
	private List<Music> musics;
	MusicModel model = new MusicModel();
	private MusicBinder musicBinder;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_music_list, null);
		//��ʼ���ؼ�
		setViews(view);
		//��listView����¼�����
		setListeners();
		//��ѯ�¸������� List<Music>
		model.findHotMusicList(new MusicModel.Callback() {
			public void onMusicListLoaded(List<Music> musics) {
				//�������б������Ϻ�ִ��
				HotMusicFragment.this.musics = musics;
				setAdapter(musics);
			}
		}, 0, 20);
		return view;
	}
	
	/**
	 * ���ü���
	 */
	private void setListeners() {
		//listview�Ĺ�������
		listView.setOnScrollListener(new OnScrollListener() {
			private boolean isBottom;
			/**
			 * ������״̬�ı�ʱִ��
			 */
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_FLING:
					//Log.i("info", "SCROLL_STATE_FLING");
					break;
				case SCROLL_STATE_IDLE:
					//Log.i("info", "SCROLL_STATE_IDLE");
					//����Ѿ�������  ������һҳ
					if(isBottom){ //������  
						Log.i("info", "������һҳ...");
						//
						model.findHotMusicList(new Callback() {
							public void onMusicListLoaded(List<Music> ms) {
								//����������ӵ������ݼ����� ����adapter
								if(ms.isEmpty()){//û������
									Toast.makeText(getActivity(), "�Ѿ���ͷ��", Toast.LENGTH_SHORT).show();
									return;
								}
								musics.addAll(ms);
								adapter.notifyDataSetChanged();
							}
						}, musics.size(), 20);
					}
					
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
					//Log.i("info", "SCROLL_STATE_TOUCH_SCROLL");
					break;
				}
			}
			//������ʱִ�и÷���
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem+visibleItemCount == totalItemCount){
					//Log.i("info", "������....");
					isBottom = true;
				}else{
					isBottom = false;
				}
			}
		});
		
		//���item����
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//�ѵ�ǰ�����б���position���浽MusicApplication��
				MusicApplication app = (MusicApplication) getActivity().getApplication();
				app.setMusicPlayList(musics);
				app.setPosition(position);
				
				final Music m=musics.get(position);
				String songId = m.getSong_id();
				//����songId�����ø�������ϸ��Ϣ
				model.getSongInfoBySongId(songId, new MusicModel.SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						//�жϻ�ȡ���������Ƿ���null 
						if(urls == null || info==null){
							Toast.makeText(getActivity(), "���ּ���ʧ��, �������", Toast.LENGTH_SHORT).show();
							return;
						}
						//��ʼ׼����������
						m.setUrls(urls);
						m.setSongInfo(info);
						//��ȡ��ǰ��Ҫ���ŵ����ֵ�·��
						SongUrl url = urls.get(0);
						String musicpath=url.getShow_link();
						//Log.i("info", "path:"+musicpath);
						//��ʼ��������
						musicBinder.playMusic(musicpath);
					}
				});
			}
		});
	}

	/**
	 * ��ʼ��
	 * @param view
	 */
	private void setViews(View view) {
		listView = (ListView) view.findViewById(R.id.listView);
	}
	
	/**
	 * ��listView����������
	 */
	public void setAdapter(List<Music> musics){
		adapter = new MusicAdapter(getActivity(), musics, listView);
		listView.setAdapter(adapter);
	}
	
	public void setMusicBinder(MusicBinder binder){
		this.musicBinder = binder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//��adapter�е��߳�ͣ��
		adapter.stopThread();
	}
	
}
