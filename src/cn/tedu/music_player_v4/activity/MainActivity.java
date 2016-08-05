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
		//控件初始化
		setViews();
		//给viewPager设置适配器
		setViewPagerAdapter();
		//实现tab标签与viewpager的联动
		setListeners();
		//绑定Service
		bindMusicService();
		//注册组件
		registComponent();
	}
	
	/**
	 * 注册各种组件
	 */
	private void registComponent() {
		//注册广播接收器
		receiver = new UpdateMusicInfoReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_START_PLAY);
		filter.addAction(GlobalConsts.ACTION_UPDATE_PROGRESS);
		this.registerReceiver(receiver, filter);
	}

	/**
	 * 绑定service
	 */
	private void bindMusicService() {
		Intent intent = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {
			//异常断开时 执行
			public void onServiceDisconnected(ComponentName name) {
			}
			//当与service绑定成功后 执行
			public void onServiceConnected(ComponentName name, IBinder service) {
				musicBinder = (MusicBinder) service;
				//绑定成功后  把musicBinder 给Fragment
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
		//解除与Service的绑定
		this.unbindService(conn);
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	/**
	 * 监听
	 */
	private void setListeners() {
		//给搜索列表添加事件监听
		lvSearchResult.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//把搜索列表当成当前的播放列表存入app
				MusicApplication app = (MusicApplication) getApplication();
				app.setMusicPlayList(searchMusicList);
				app.setPosition(position);
				//获取当前点击的Music对象
				final Music m = searchMusicList.get(position);
				String songId = m.getSong_id();
				//通过songId查询这首歌曲的基本信息 SongInfo
				model.getSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> url, SongInfo info) {
						m.setUrls(url);
						m.setSongInfo(info);
						//调用musicBinder的playMusic方法播放音乐
						String musicUrl = url.get(0).getShow_link();
						musicBinder.playMusic(musicUrl);
					}
				});
			}
		});
		
		//给seekbar添加拖拽监听
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
		
		//给rlPM
		rlPlayMusic.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		//滑动viewpager 控制 导航栏
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {
				switch (position) {
				case 0:
					Log.i("info","滚到了第1页..");
					rbNew.setChecked(true);
					break;
				case 1:
					Log.i("info","滚到了第2页..");
					rbHot.setChecked(true);
					break;
				}
			}
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		//点击导航 控制viewpager
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioNew:
					Log.i("info", "选择了radioNew..");
					viewPager.setCurrentItem(0);
					break;
				case R.id.radioHot:
					Log.i("info", "选择了radioHot..");
					viewPager.setCurrentItem(1);
					break;
				}
			}
		});
	}
	
	/**
	 * 监听
	 */
	public void doClick(View view){
		switch (view.getId()) {
		case R.id.tvDownLoad: //点击下载后
			Log.i("info", "点击了下载.....");
			download();
			break;
		case R.id.btnSearchMusic: //搜索音乐
			Log.i("info", "点击了搜索音乐.....");
			searchMusic();
			break;
		case R.id.btnCancel: //收起搜索界面
			rlSearchMusic.setVisibility(View.INVISIBLE);
			TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -rlPlayMusic.getHeight());
			anim.setDuration(300);
			rlSearchMusic.startAnimation(anim);
			break;
		case R.id.btnSearch:  //显示搜索界面
			Log.i("info", "点击了显示搜索界面.....");
			rlSearchMusic.setVisibility(View.VISIBLE);
			anim = new TranslateAnimation(0, 0, -rlPlayMusic.getHeight(), 0);
			anim.setDuration(300);
			rlSearchMusic.startAnimation(anim);
			break;
		case R.id.ivPMPre:  //上一曲
			MusicApplication app=(MusicApplication) getApplication();
			app.preMusic();
			final Music m = app.getCurrentMusic();
			//加载m的基本信息
			model.getSongInfoBySongId(m.getSong_id(), new SongInfoCallback() {
				public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
					m.setUrls(urls);
					m.setSongInfo(info);
					musicBinder.playMusic(m.getUrls().get(0).getShow_link());
				}
			});
			break;
		case R.id.ivPMPlay:  //暂停或播放
			musicBinder.playOrPause();
			break;
		case R.id.ivPMNext:
			app=(MusicApplication) getApplication();
			app.nextMusic();
			final Music m2 = app.getCurrentMusic();
			//先加载音乐的基本信息 
			model.getSongInfoBySongId(m2.getSong_id(), new SongInfoCallback() {
				public void onSongInfoLoaded(List<SongUrl> url, SongInfo info) {
					m2.setUrls(url);
					m2.setSongInfo(info);
					musicBinder.playMusic(m2.getUrls().get(0).getShow_link());
				}
			});
			break;
		case R.id.ivCMPic: //显示播放界面
			showRlPlayMusic();
			break;
		}
	}
	
	/**
	 * 下载当前音乐
	 */
	private void download() {
		MusicApplication app = (MusicApplication) getApplication();
		final Music m = app.getCurrentMusic();
		final List<SongUrl> urls = m.getUrls();
		//把集合中的数据 解析为字符串数组
		String[] data = new String[urls.size()];
		for(int i = 0; i<urls.size(); i++){
			SongUrl url = urls.get(i);
			double fs = 100.0*Integer.parseInt(url.getFile_size())/1024/1024;
			//fs :  123.234234234
			data[i]=Math.floor(fs)/100+"M";
		}
		//弹出AlertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("选择版本")
			.setItems(data, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SongUrl url = urls.get(which);
					String filelink = url.getShow_link();
					Log.i("info", "下载文件:"+filelink);
					//启动Service执行下载操作
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
	 * 搜索音乐
	 */
	private void searchMusic() {
		//1.  获取关键字
		String key = etKeyword.getText().toString();
		if(key.equals("")){
			Toast.makeText(this, "请输入关键字", Toast.LENGTH_SHORT).show();
			return;
		}
		//2.  根据关键字  查询相关结果  http  Model
		model.searchMusic(key, 1, new Callback() {
			public void onMusicListLoaded(List<Music> musics) {
				//3.  List<Music>
				searchMusicList = musics;
				//4.  更新Adapter
				searchMusicAdapter = new SearchResultAdapter(MainActivity.this, musics);
				lvSearchResult.setAdapter(searchMusicAdapter);
			}
		});
	}

	/**
	 * 当用户点击了返回按钮时执行
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
	 * 显示播放界面
	 */
	private void showRlPlayMusic() {
		rlPlayMusic.setVisibility(View.VISIBLE);
		TranslateAnimation anim = new TranslateAnimation(0, 0, rlPlayMusic.getHeight(), 0);
		anim.setDuration(300);
		rlPlayMusic.startAnimation(anim);
	}

	/**
	 * 隐藏播放界面
	 */
	private void hideRlPlayMusic(){
		rlPlayMusic.setVisibility(View.INVISIBLE);
		TranslateAnimation anim = new TranslateAnimation(0, 0, 0, rlPlayMusic.getHeight());
		anim.setDuration(300);
		rlPlayMusic.startAnimation(anim);
	}
	
	/**
	 * 给viewPager设置适配器
	 */
	private void setViewPagerAdapter() {
		//构建Fragment数据源
		fragments = new ArrayList<Fragment>();
		//向fragments集合中添加Fragment
		fragments.add(new NewMusicFragment());
		fragments.add(new HotMusicFragment());
		pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
	}

	/**
	 * 控件初始化
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
	 * 编写viewPager的Adapter
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
	 * 接受用于更新音乐进度的广播接收器
	 */
	class UpdateMusicInfoReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			//音乐已经开始播放
			if(action.equals(GlobalConsts.ACTION_UPDATE_PROGRESS)){
				//更新音乐的播放进度  seekbar textView
				int current=intent.getIntExtra("current", 0);
				int total=intent.getIntExtra("total", 0);
				seekBar.setMax(total);
				seekBar.setProgress(current);
				//更新两个textView
				tvPMCurrentTime.setText(sdf.format(new Date(current)));
				tvPMTotalTime.setText(sdf.format(new Date(total)));
				//更新歌词信息
				MusicApplication app = (MusicApplication) getApplication();
				List<LrcLine> lines = app.getLrc();
				if(lines == null){ //歌词还没有下载成功
					return;
				}
				//获取当前时间需要显示的歌词内容
				for(int i = 0; i<lines.size(); i++){
					LrcLine line = lines.get(i);
					if(line.equalsTime(current)){
						String content = line.getContent();
						//设置TextView
						tvPMLrc.setText(content);
						return ;
					}
				}
			}else if(action.equals(GlobalConsts.ACTION_START_PLAY)){
				//获取到当前正在播放的music对象
				MusicApplication app = (MusicApplication) getApplication();
				List<Music> list = app.getMusicPlayList();
				int position = app.getPosition();
				Music m = list.get(position);
				//更新tvPMTitle   
				tvPMTitle.setText(m.getTitle());
				//更新tvPMSinger
				tvPMSinger.setText(m.getAuthor());
				//更新ivPMAlbum    
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
				//更新ivPMBackground
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
							//图片已经加载成功   需要模糊化处理
							BitmapUtils.loadBluredBitmap(bitmap, 10, new BitmapCallback() {
								public void onBitmapLoaded(Bitmap bitmap) {
									//图片已经模糊化处理完成
									ivPMBackground.setImageBitmap(bitmap);
								}
							});
						}else{
							ivPMBackground.setImageResource(R.drawable.default_music_background);
						}
					}
				});
				//更新CircleImageView   TextView
				String picPath = m.getSongInfo().getPic_small();
				String title = m.getTitle();
				tvCMTitle.setText(title);
				BitmapUtils.loadBitmap(MainActivity.this, picPath, 50, 50, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap != null){
							ivCMPic.setImageBitmap(bitmap);
							//启动旋转动画
							RotateAnimation anim = new RotateAnimation(0, 360, ivCMPic.getWidth()/2, ivCMPic.getHeight()/2);
							anim.setDuration(20000);
							//匀速旋转
							anim.setInterpolator(new LinearInterpolator());
							anim.setRepeatCount(Animation.INFINITE); //一直转
							ivCMPic.startAnimation(anim);
						}else{
							ivCMPic.setImageResource(R.drawable.ic_launcher);
						}
					}
				});
				//更新歌词  
				//1> 下载歌词
				//2> 解析歌词
				//3>根据当前时间显示相应歌词
				model.downloadLrc(MainActivity.this, m.getSongInfo().getLrclink(), new LrcCallback() {
					public void onLrcLoaded(List<LrcLine> lines) {
						//把歌词集合保存起来   存入application
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
