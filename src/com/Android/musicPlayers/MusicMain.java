package com.Android.musicPlayers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MusicMain extends Activity implements OnCompletionListener, OnErrorListener, Runnable {
	protected static final int SEARCH_MUSIC_SUCCESS = 0;// 搜索成功标记
	private TextView music_name, music_songer, cur, totaltimes;
	private ImageButton go, list, lovemusic;//
	private SeekBar seekBar1;//进度条
	private static final String MUSIC_PATH = "/data/music";//音乐存放列表
	private List<String> musicFileList;//播放文件名列表
	private List<String> musicDirList;//播放文件路径列表
	private MediaPlayer mp;
	private int currIndex = 0;// 表示当前播放的音乐索引
	private Timer timer;
	// 定义当前播放器的状态״̬
	private static final int IDLE = 0;
	private static final int PAUSE = 1;
	private static final int START = 2;
	//定义音乐播放顺序
	private static final int ORDER = 0;
	private static final int RANDO = 1;
	private static final int LOOP = 2;
	private int loopIndex = 0;
	private int currState = IDLE; // 当前播放器的状态
	//定义线程池（同时只能有一个线程运行）
	ExecutorService es = Executors.newSingleThreadExecutor();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		Intent intent=getIntent();
		musicFileList = new ArrayList<String>();
		musicDirList = new ArrayList<String>();
		search(new File(MUSIC_PATH));
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		mp.setOnErrorListener(this);
		initView();
		setName(musicFileList.get(0));
		list.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent i = new Intent(MusicMain.this, LocalMain.class);
				Bundle bundle = new Bundle();
				if (mp.isPlaying()) {
					mp.stop();
					bundle.putBoolean("T", true);
				}
				bundle.putLong("c", loopIndex);
				bundle.putLong("time", mp.getCurrentPosition());
				bundle.putLong("In", currIndex);
				i.putExtras(bundle);
				mp.release();
				finish();
				stopTimer();
				startActivity(i);
				overridePendingTransition(0, 0);
			}
		});
		Bundle value = intent.getExtras();
		if (value != null) {
			int time = (int) value.getLong("time");
			currIndex = (int) value.getLong("In");
			if (value.containsKey("T")) {
				play();
				mp.seekTo(time);
			}else {
				play();
				mp.seekTo(time);
				play();
			}
			loopIndex = (int) value.getLong("c");
		}
		if (loopIndex == 0) {
			lovemusic.setImageResource(R.drawable.for1);
		} else if (loopIndex == 1) {
			lovemusic.setImageResource(R.drawable.for3);
		} else if (loopIndex == 2) {
			lovemusic.setImageResource(R.drawable.for2);
		}
		lovemusic.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				switch (loopIndex) {
				case ORDER:
					loopIndex = RANDO;
					lovemusic.setImageResource(R.drawable.for3);
					break;
				case RANDO:
					loopIndex = LOOP;
					lovemusic.setImageResource(R.drawable.for2);
					break;
				case LOOP:
					loopIndex = ORDER;
					lovemusic.setImageResource(R.drawable.for1);
					break;
				}
			}
		});
		getProgress();
		seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				//获取进度条的进度
				int p = seekBar.getProgress();
				//将进度条的进度赋值给歌曲
				mp.seekTo(p);
				//开始音乐继续获取歌曲的进度
				getProgress();
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				//取消timer任务
				stopTimer();
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					mp.seekTo(progress);
				}
			}
		});
	}
	//更新TextView
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			cur.setText(formatTime(msg.what));
		}
	}; 
	private Handler handlertwo = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			totaltimes.setText(formatTime(msg.what));
		}
	}; 
	//时间格式化工具
	private String formatTime(int length){
		Date date = new Date(length);
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		String totalTime = sdf.format(date);
		return totalTime;
	} 
	private void initView() {
		//显示菜单栏返回键
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		//隐藏电量运营商
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		music_name = (TextView) findViewById(R.id.music_name);
		music_songer = (TextView) findViewById(R.id.music_songer);
		cur = (TextView) findViewById(R.id.cur);
		totaltimes = (TextView) findViewById(R.id.times);
		go = (ImageButton) findViewById(R.id.go);
		list = (ImageButton) findViewById(R.id.list);
		lovemusic = (ImageButton) findViewById(R.id.lovemusic);
		seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
	}
	//菜单栏返回键功能
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, LocalMain.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Bundle bundle = new Bundle();
			if (mp.isPlaying()) {
				mp.stop();
				bundle.putBoolean("T", true);
			}
			bundle.putLong("c", loopIndex);
			bundle.putLong("time", mp.getCurrentPosition());
			bundle.putLong("In", currIndex);
			intent.putExtras(bundle);
			mp.release();
			stopTimer();
			finish();
			startActivity(intent);
			overridePendingTransition(0, 0);
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	//计时器
	private void getProgress() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				//获取歌曲的进度
				seekBar1.setMax(mp.getDuration());
				int p = mp.getCurrentPosition();
				//将获取歌曲的进度赋值给seekbar
				seekBar1.setProgress(p);
				handler.sendEmptyMessage(mp.getCurrentPosition());//获取当前的点击位置
				handlertwo.sendEmptyMessage(mp.getDuration());
			}
		}, 0, 1000);
	}
	public void stopTimer()
	{
		if(timer!=null)
		{
			timer.cancel();
		}
	}
	//设置歌名歌手
	public void setName(String fileName) {
		//要求文件名为 《歌手 - 歌名》 或 《歌名》 格式  注：有空格
		System.out.println(fileName);
		if (fileName != null && !fileName.trim().equals("")) {
			String[] all = fileName.split(" - ");
			if (all.length == 2) {
				music_name.setText(all[1]);
				//分割歌手名
				String[] songer = all[0].split(",");
				if (songer.length > 2) {
					music_songer.setText(songer[0] + "," + songer[1]);
				}else {
					music_songer.setText(all[0]);
				}
			}else {
				music_name.setText(all[0]);
				music_songer.setText("未知歌手");
			}
		}else {
			music_name.setText("未知歌曲");
			music_songer.setText("未知歌手");
		}
	}
	private void search(File dir) {
		File[] files = dir.listFiles();//获取当前文件夹下的所有文件和文件夹
		for (File file : files) {
			if (file.isDirectory()) {//如果path表示的是一个目录则返回true
				search(file);
			}else if (file.getAbsolutePath().endsWith(".mp3")) {
				this.musicFileList.add(file.getName());
				this.musicDirList.add(file.getAbsolutePath());
				Log.i(file.getName(), file.getAbsolutePath());
			}
		}
	}
	private void play() {
		switch (currState) {
		case IDLE:
			start();
			break;
		case PAUSE:
			mp.pause();
			go.setImageResource(R.drawable.go);
			currState = START;
			break;
		case START:
			mp.start();
			go.setImageResource(R.drawable.stop);
			currState = PAUSE;
		}
	}
	private void start() {
		if (musicDirList.size() > 0 && currIndex < musicDirList.size()) {
			String SongPath = musicDirList.get(currIndex);
			mp.reset();
			try {
				mp.setDataSource(SongPath);
				mp.prepare();
				mp.start();
				go.setImageResource(R.drawable.stop);
				es.execute(this);
				setName(musicFileList.get(currIndex));
				currState = PAUSE;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show();
		}
	}
	//上一首
	private void previous() {
		if((currIndex-1)>=0){
			currIndex--;
			start();
		}else{
			currIndex  = musicDirList.size() - 1;
			start();
		}
	}
	//下一首
	private void next() {
		if(currIndex+1<musicDirList.size()){
			currIndex++;
			start();
		}else{
			currIndex = 0;
			start();
		}
	}
	//播放按钮
	public void play(View v){
		play();
	}
	//上一首按钮
	public void previous(View v){
		previous();
	}
	//下一首按钮
	public void next(View v){
		next();
	}
	//监听器，选择音乐播放方式，播放下一首
	public void onCompletion(MediaPlayer mp) {
		if (loopIndex == ORDER) {
			if(musicDirList.size()>0){
				next();
			}else{
				Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show();
			}
		}else if (loopIndex == RANDO) {
			if(musicDirList.size() > 0){
				Random random=new Random();
				//  返回一个伪随机数，它是取自此随机数生成器序列的、在 0（包括）和指定值（不包括）之间均匀分布的 int 值。
				int randomInt=random.nextInt(musicDirList.size());  
				currIndex = randomInt;
				start();
			}
		}else if (loopIndex == LOOP) {
			play();
			play();
		}
	}
	//当播放异常时触发
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}
	public void run() {
	}
}