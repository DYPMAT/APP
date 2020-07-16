package com.Android.musicPlayers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MyAppMainActivity extends Activity implements OnCompletionListener, OnErrorListener, Runnable {
	private ImageButton music_me;
	public static List<Activity> activityList = new LinkedList(); 
	private String[] songnames = new String[]{"鸟之诗", "I Want My Tears Back"};
	private String[] songer = new String[]{"Lia", "Nightwish"};
	private int[] imgIds = new int[]{R.drawable.song1, R.drawable.song2};
	private static final String MUSIC_PATH = "/data/music";//音乐存放列表
	private List<String> musicFileList;//播放文件名列表
	private List<String> musicDirList;//播放文件路径列表
	private List<String> net;
	private MediaPlayer mp;
	private MediaPlayer me;
	private String path;
	private int currIndex = 0;// 表示当前播放的音乐索引
	private static final int IDLE = 0;
	private static final int PAUSE = 1;
	private static final int START = 2;
	//定义音乐播放顺序
	private static final int ORDER = 0;
	private static final int RANDO = 1;
	private static final int LOOP = 2;
	private int loopIndex = 0;
	private int netIndex = 0;
	private int currState = IDLE; // 当前播放器的状态
	private int State = IDLE; // 当前播放器的状态
	//定义线程池（同时只能有一个线程运行）
	ExecutorService es = Executors.newSingleThreadExecutor();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//显示菜单栏返回键
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		//隐藏电量运营商
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		music_me = (ImageButton) findViewById(R.id.music_me);
		ListView listView = (ListView) findViewById(R.id.listView1);
		music_me.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent i = new Intent(MyAppMainActivity.this, LocalMain.class);
				Bundle bundle = new Bundle();
				if (mp.isPlaying()) {
					mp.stop();
					bundle.putBoolean("T", true);
					bundle.putLong("time", mp.getCurrentPosition());
					bundle.putLong("In", currIndex);
					bundle.putLong("c", loopIndex);
				}
				if (me != null && me.isPlaying()) {
					me.release();
				}
				i.putExtras(bundle);
				mp.release();
				finish();
				startActivity(i);
				overridePendingTransition(0, 0);
			}
		});
		List<Map<String, Object>> listitem = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < songnames.length; i++) {
			Map<String, Object> showitem = new HashMap<String, Object>();
			showitem.put("tou", imgIds[i]);
			showitem.put("songnames", songnames[i]);
			showitem.put("songer", songer[i]);
			listitem.add(showitem);
		}
		//创建一个simpleAdapter
		SimpleAdapter myAdapter = new SimpleAdapter(
				getApplicationContext(), 
				listitem, 
				R.layout.list_view, 
				new String[]{"tou", "songnames", "songer"}, 
				new int[]{R.id.imgtou, R.id.songname, R.id.songer});
		listView.setAdapter(myAdapter);
		musicFileList = new ArrayList<String>();
		musicDirList = new ArrayList<String>();
		net = new ArrayList<String>();
		net.add("http://192.168.1.3:8080/music/s1.mp3");
		net.add("http://192.168.1.3:8080/music/s2.mp3");
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mp.isPlaying()) {
					play();
				}
				netIndex = position;
				newplay();
			}
		});
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		mp.setOnErrorListener(this);
		search(new File(MUSIC_PATH));
		Intent intent = getIntent();
		Bundle value = intent.getExtras();
		if (value != null) {
			int time = (int) value.getLong("time");
			currIndex = (int) value.getLong("In");
			loopIndex = (int) value.getLong("c");
			if (value.containsKey("T")) {
				play();
				mp.seekTo(time);
			}else {
				play();
				mp.seekTo(time);
				play();
			}
		}
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//菜单栏返回键功能
		case android.R.id.home:
			Intent startMain = new Intent(Intent.ACTION_MAIN); 
			startMain.addCategory(Intent.CATEGORY_HOME); 
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(startMain); 
			System.exit(0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	//查音乐
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
	//播放
	private void play() {
		switch (currState) {
		case IDLE:
			start();
			break;
		case PAUSE:
			mp.pause();
			currState = START;
			break;
		case START:
			mp.start();
			currState = PAUSE;
		}
	}
	//播放
	private void newplay() {
		switch (State) {
		case IDLE:
			net();
			break;
		case PAUSE:
			me.pause();
			currState = START;
			break;
		case START:
			me.start();
			currState = PAUSE;
		}
	}
	private void net() {
		if (net.size() > 0 && netIndex < net.size()) {
			me = MediaPlayer.create(MyAppMainActivity.this, Uri.parse(net.get(netIndex)));
			me.start();
			State = PAUSE;
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
				es.execute(this);
				currState = PAUSE;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show();
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
