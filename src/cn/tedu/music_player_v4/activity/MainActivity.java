package cn.tedu.music_player_v4.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.tedu.music_player_v4.R;
import cn.tedu.music_player_v4.adapter.SearchResultAdapter;
import cn.tedu.music_player_v4.app.MusicApplication;
import cn.tedu.music_player_v4.entity.LrcLine;
import cn.tedu.music_player_v4.entity.Music;
import cn.tedu.music_player_v4.entity.SongInfo;
import cn.tedu.music_player_v4.entity.SongUrl;
import cn.tedu.music_player_v4.fragment.HotMusicFragment;
import cn.tedu.music_player_v4.fragment.NewMusicFragment;
import cn.tedu.music_player_v4.model.MusicModel;
import cn.tedu.music_player_v4.model.MusicModel.Callback;
import cn.tedu.music_player_v4.model.MusicModel.LrcCallback;
import cn.tedu.music_player_v4.model.MusicModel.SongInfoCallback;
import cn.tedu.music_player_v4.service.DownloadService;
import cn.tedu.music_player_v4.service.PlayMusicService;
import cn.tedu.music_player_v4.service.PlayMusicService.MusicBinder;
import cn.tedu.music_player_v4.util.BitmapUtils;
import cn.tedu.music_player_v4.util.BitmapUtils.BitmapCallback;
import cn.tedu.music_player_v4.util.GlobalConsts;

public class MainActivity extends FragmentActivity {
	private RadioGroup radioGroup;
	private ViewPager viewPager;
	private RadioButton rbNew;
	private RadioButton rbHot;
	private ImageView ivCMPic;
	private TextView tvCMTitle;
	private ImageView ivPMAlbum;
	private ImageView ivPMBackground;
	private TextView tvPMTitle;
	private TextView tvPMSinger;
	private SeekBar seekBar;
	private TextView tvPMCurrentTime;
	private TextView tvPMTotalTime;
	private TextView tvPMLrc;
	private RelativeLayout rlPlayMusic;
	private RelativeLayout rlSearchMusic;
	private EditText etKeyword;
	private ListView lvSearchResult;
	private List<Fragment> fragments;
	private PagerAdapter pagerAdapter;
	private ServiceConnection conn;
	protected MusicBinder musicBinder;
	private UpdateMusicInfoReceiver receiver;
	private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
	private MusicModel model;
	protected List<Music> searchMusicList;
	protected SearchResultAdapter searchMusicAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		model = new MusicModel();
		//�ؼ���ʼ��
		setViews();
		//��viewPager����������
		setViewPagerAdapter();
		//ʵ��tab��ǩ��viewpager������
		setListeners();
		//��Service
		bindMusicService();
		//ע�����
		registComponent();
	}
	
	/**
	 * ע��������
	 */
	private void registComponent() {
		//ע��㲥������
		receiver = new UpdateMusicInfoReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_START_PLAY);
		filter.addAction(GlobalConsts.ACTION_UPDATE_PROGRESS);
		this.registerReceiver(receiver, filter);
	}

	/**
	 * ��service
	 */
	private void bindMusicService() {
		Intent intent = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {
			//�쳣�Ͽ�ʱ ִ��
			public void onServiceDisconnected(ComponentName name) {
			}
			//����service�󶨳ɹ��� ִ��
			public void onServiceConnected(ComponentName name, IBinder service) {
				musicBinder = (MusicBinder) service;
				//�󶨳ɹ���  ��musicBinder ��Fragment
				NewMusicFragment f = (NewMusicFragment) fragments.get(0);
				f.setMusicBinder(musicBinder);
				HotMusicFragment f2 = (HotMusicFragment) fragments.get(1);
				f2.setMusicBinder(musicBinder);
			}
		};
		this.bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		//�����Service�İ�
		this.unbindService(conn);
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	/**
	 * ����
	 */
	private void setListeners() {
		//�������б�����¼�����
		lvSearchResult.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//�������б��ɵ�ǰ�Ĳ����б����app
				MusicApplication app = (MusicApplication) getApplication();
				app.setMusicPlayList(searchMusicList);
				app.setPosition(position);
				//��ȡ��ǰ�����Music����
				final Music m = searchMusicList.get(position);
				String songId = m.getSong_id();
				//ͨ��songId��ѯ���׸����Ļ�����Ϣ SongInfo
				model.getSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> url, SongInfo info) {
						m.setUrls(url);
						m.setSongInfo(info);
						//����musicBinder��playMusic������������
						String musicUrl = url.get(0).getShow_link();
						musicBinder.playMusic(musicUrl);
					}
				});
			}
		});
		
		//��seekbar�����ק����
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					musicBinder.seekTo(progress);
				}
			}
		});
		
		//��rlPM
		rlPlayMusic.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		//����viewpager ���� ������
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {
				switch (position) {
				case 0:
					Log.i("info","�����˵�1ҳ..");
					rbNew.setChecked(true);
					break;
				case 1:
					Log.i("info","�����˵�2ҳ..");
					rbHot.setChecked(true);
					break;
				}
			}
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		//������� ����viewpager
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioNew:
					Log.i("info", "ѡ����radioNew..");
					viewPager.setCurrentItem(0);
					break;
				case R.id.radioHot:
					Log.i("info", "ѡ����radioHot..");
					viewPager.setCurrentItem(1);
					break;
				}
			}
		});
	}
	
	/**
	 * ����
	 */
	public void doClick(View view){
		switch (view.getId()) {
		case R.id.tvDownLoad: //������غ�
			Log.i("info", "���������.....");
			download();
			break;
		case R.id.btnSearchMusic: //��������
			Log.i("info", "�������������.....");
			searchMusic();
			break;
		case R.id.btnCancel: //������������
			rlSearchMusic.setVisibility(View.INVISIBLE);
			TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -rlPlayMusic.getHeight());
			anim.setDuration(300);
			rlSearchMusic.startAnimation(anim);
			break;
		case R.id.btnSearch:  //��ʾ��������
			Log.i("info", "�������ʾ��������.....");
			rlSearchMusic.setVisibility(View.VISIBLE);
			anim = new TranslateAnimation(0, 0, -rlPlayMusic.getHeight(), 0);
			anim.setDuration(300);
			rlSearchMusic.startAnimation(anim);
			break;
		case R.id.ivPMPre:  //��һ��
			MusicApplication app=(MusicApplication) getApplication();
			app.preMusic();
			final Music m = app.getCurrentMusic();
			//����m�Ļ�����Ϣ
			model.getSongInfoBySongId(m.getSong_id(), new SongInfoCallback() {
				public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
					m.setUrls(urls);
					m.setSongInfo(info);
					musicBinder.playMusic(m.getUrls().get(0).getShow_link());
				}
			});
			break;
		case R.id.ivPMPlay:  //��ͣ�򲥷�
			musicBinder.playOrPause();
			break;
		case R.id.ivPMNext:
			app=(MusicApplication) getApplication();
			app.nextMusic();
			final Music m2 = app.getCurrentMusic();
			//�ȼ������ֵĻ�����Ϣ 
			model.getSongInfoBySongId(m2.getSong_id(), new SongInfoCallback() {
				public void onSongInfoLoaded(List<SongUrl> url, SongInfo info) {
					m2.setUrls(url);
					m2.setSongInfo(info);
					musicBinder.playMusic(m2.getUrls().get(0).getShow_link());
				}
			});
			break;
		case R.id.ivCMPic: //��ʾ���Ž���
			showRlPlayMusic();
			break;
		}
	}
	
	/**
	 * ���ص�ǰ����
	 */
	private void download() {
		MusicApplication app = (MusicApplication) getApplication();
		final Music m = app.getCurrentMusic();
		final List<SongUrl> urls = m.getUrls();
		//�Ѽ����е����� ����Ϊ�ַ�������
		String[] data = new String[urls.size()];
		for(int i = 0; i<urls.size(); i++){
			SongUrl url = urls.get(i);
			double fs = 100.0*Integer.parseInt(url.getFile_size())/1024/1024;
			//fs :  123.234234234
			data[i]=Math.floor(fs)/100+"M";
		}
		//����AlertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("ѡ��汾")
			.setItems(data, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SongUrl url = urls.get(which);
					String filelink = url.getShow_link();
					Log.i("info", "�����ļ�:"+filelink);
					//����Serviceִ�����ز���
					Intent intent = new Intent(MainActivity.this, DownloadService.class);
					intent.putExtra("url", filelink);
					intent.putExtra("title", m.getTitle());
					intent.putExtra("bit", url.getFile_bitrate());
					startService(intent);
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * ��������
	 */
	private void searchMusic() {
		//1.  ��ȡ�ؼ���
		String key = etKeyword.getText().toString();
		if(key.equals("")){
			Toast.makeText(this, "������ؼ���", Toast.LENGTH_SHORT).show();
			return;
		}
		//2.  ���ݹؼ���  ��ѯ��ؽ��  http  Model
		model.searchMusic(key, 1, new Callback() {
			public void onMusicListLoaded(List<Music> musics) {
				//3.  List<Music>
				searchMusicList = musics;
				//4.  ����Adapter
				searchMusicAdapter = new SearchResultAdapter(MainActivity.this, musics);
				lvSearchResult.setAdapter(searchMusicAdapter);
			}
		});
	}

	/**
	 * ���û�����˷��ذ�ťʱִ��
	 */
	@Override
	public void onBackPressed() {
		if(rlPlayMusic.getVisibility() == View.VISIBLE){
			hideRlPlayMusic();
		}else{
			super.onBackPressed();
		}
	}
	
	/**
	 * ��ʾ���Ž���
	 */
	private void showRlPlayMusic() {
		rlPlayMusic.setVisibility(View.VISIBLE);
		TranslateAnimation anim = new TranslateAnimation(0, 0, rlPlayMusic.getHeight(), 0);
		anim.setDuration(300);
		rlPlayMusic.startAnimation(anim);
	}

	/**
	 * ���ز��Ž���
	 */
	private void hideRlPlayMusic(){
		rlPlayMusic.setVisibility(View.INVISIBLE);
		TranslateAnimation anim = new TranslateAnimation(0, 0, 0, rlPlayMusic.getHeight());
		anim.setDuration(300);
		rlPlayMusic.startAnimation(anim);
	}
	
	/**
	 * ��viewPager����������
	 */
	private void setViewPagerAdapter() {
		//����Fragment����Դ
		fragments = new ArrayList<Fragment>();
		//��fragments���������Fragment
		fragments.add(new NewMusicFragment());
		fragments.add(new HotMusicFragment());
		pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
	}

	/**
	 * �ؼ���ʼ��
	 */
	private void setViews() {
		lvSearchResult = (ListView) findViewById(R.id.lvSearchResult);
		rlSearchMusic = (RelativeLayout) findViewById(R.id.rlSearchMusic);
		etKeyword = (EditText) findViewById(R.id.etKeyword);
		tvPMLrc = (TextView) findViewById(R.id.tvPMLrc);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		rbNew = (RadioButton) findViewById(R.id.radioNew);
		rbHot = (RadioButton) findViewById(R.id.radioHot);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		ivCMPic = (ImageView) findViewById(R.id.ivCMPic);
		tvCMTitle = (TextView) findViewById(R.id.tvCMTitle);
		rlPlayMusic = (RelativeLayout) findViewById(R.id.rlPlayMusic);
		ivPMAlbum = (ImageView) findViewById(R.id.ivPMAlbum);
		ivPMBackground = (ImageView) findViewById(R.id.ivPMBackground);
		tvPMTitle = (TextView) findViewById(R.id.tvPMTitle);
		tvPMSinger = (TextView) findViewById(R.id.tvPMSinger);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		tvPMCurrentTime = (TextView) findViewById(R.id.tvPMCurrentTime);
		tvPMTotalTime = (TextView) findViewById(R.id.tvPMTotalTime);
	}

	/**
	 * ��дviewPager��Adapter
	 */
	class MyPagerAdapter extends FragmentPagerAdapter{
		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
		
	}
	
	/**
	 * �������ڸ������ֽ��ȵĹ㲥������
	 */
	class UpdateMusicInfoReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			//�����Ѿ���ʼ����
			if(action.equals(GlobalConsts.ACTION_UPDATE_PROGRESS)){
				//�������ֵĲ��Ž���  seekbar textView
				int current=intent.getIntExtra("current", 0);
				int total=intent.getIntExtra("total", 0);
				seekBar.setMax(total);
				seekBar.setProgress(current);
				//��������textView
				tvPMCurrentTime.setText(sdf.format(new Date(current)));
				tvPMTotalTime.setText(sdf.format(new Date(total)));
				//���¸����Ϣ
				MusicApplication app = (MusicApplication) getApplication();
				List<LrcLine> lines = app.getLrc();
				if(lines == null){ //��ʻ�û�����سɹ�
					return;
				}
				//��ȡ��ǰʱ����Ҫ��ʾ�ĸ������
				for(int i = 0; i<lines.size(); i++){
					LrcLine line = lines.get(i);
					if(line.equalsTime(current)){
						String content = line.getContent();
						//����TextView
						tvPMLrc.setText(content);
						return ;
					}
				}
			}else if(action.equals(GlobalConsts.ACTION_START_PLAY)){
				//��ȡ����ǰ���ڲ��ŵ�music����
				MusicApplication app = (MusicApplication) getApplication();
				List<Music> list = app.getMusicPlayList();
				int position = app.getPosition();
				Music m = list.get(position);
				//����tvPMTitle   
				tvPMTitle.setText(m.getTitle());
				//����tvPMSinger
				tvPMSinger.setText(m.getAuthor());
				//����ivPMAlbum    
				if(m.getSongInfo()==null){
					return;
				}
				String albumPath = m.getSongInfo().getAlbum_500_500();
				Log.i("info", "albumPath:"+albumPath);
				BitmapUtils.loadBitmap(context, albumPath, 0, 0 , new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap != null){
							ivPMAlbum.setImageBitmap(bitmap);
						}else{
							ivPMAlbum.setImageResource(R.drawable.default_music_pic);
						}
					}
				});
				//����ivPMBackground
				String backgroundPath = m.getSongInfo().getArtist_480_800();
				if("".equals(backgroundPath)){ //
					backgroundPath = albumPath;
				}
				if("".equals(backgroundPath)){
					backgroundPath = m.getPic_big();
				}
				Log.i("info", "backgroundPath:"+backgroundPath);
				BitmapUtils.loadBitmap(context, backgroundPath, 100, 100, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap != null ){
							//ͼƬ�Ѿ����سɹ�   ��Ҫģ��������
							BitmapUtils.loadBluredBitmap(bitmap, 10, new BitmapCallback() {
								public void onBitmapLoaded(Bitmap bitmap) {
									//ͼƬ�Ѿ�ģ�����������
									ivPMBackground.setImageBitmap(bitmap);
								}
							});
						}else{
							ivPMBackground.setImageResource(R.drawable.default_music_background);
						}
					}
				});
				//����CircleImageView   TextView
				String picPath = m.getSongInfo().getPic_small();
				String title = m.getTitle();
				tvCMTitle.setText(title);
				BitmapUtils.loadBitmap(MainActivity.this, picPath, 50, 50, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap != null){
							ivCMPic.setImageBitmap(bitmap);
							//������ת����
							RotateAnimation anim = new RotateAnimation(0, 360, ivCMPic.getWidth()/2, ivCMPic.getHeight()/2);
							anim.setDuration(20000);
							//������ת
							anim.setInterpolator(new LinearInterpolator());
							anim.setRepeatCount(Animation.INFINITE); //һֱת
							ivCMPic.startAnimation(anim);
						}else{
							ivCMPic.setImageResource(R.drawable.ic_launcher);
						}
					}
				});
				//���¸��  
				//1> ���ظ��
				//2> �������
				//3>���ݵ�ǰʱ����ʾ��Ӧ���
				model.downloadLrc(MainActivity.this, m.getSongInfo().getLrclink(), new LrcCallback() {
					public void onLrcLoaded(List<LrcLine> lines) {
						//�Ѹ�ʼ��ϱ�������   ����application
						//Log.i("info", ""+lines);
						MusicApplication app=(MusicApplication) getApplication();
						app.setLrc(lines);
					}
				});
			}
		}
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
