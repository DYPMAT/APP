package com.Android.musicPlayers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.Android.musicAdapter.musicAdapter;
import com.Android.musicAdapter.musicData;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LocalMain extends Activity implements OnCompletionListener, OnErrorListener, OnItemClickListener, Runnable {
	protected static final int SEARCH_MUSIC_SUCCESS = 0;// 搜索成功标记
	private TextView name, songer;
	private ImageButton go;
	private Button button;
	private ListView listView;
	private static final String MUSIC_PATH = "/data/music";//音乐存放列表
	private List<String> musicFileList;//播放文件名列表
	private List<String> musicDirList;//播放文件路径列表
	//ListView适配器
	private List<musicData> list = null;
	private musicAdapter mAdapter = null;
	private MediaPlayer mp;
	private int currIndex = 0;// 表示当前播放的音乐索引
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
		setContentView(R.layout.local_main);
		musicFileList = new ArrayList<String>();
		musicDirList = new ArrayList<String>();
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		mp.setOnErrorListener(this);
		initView();
		search(new File(MUSIC_PATH));
		setName(musicFileList.get(0));
		list = new LinkedList<musicData>();
		for (int i = 0; i < musicFileList.size(); i++) {
			list.add(new musicData(retuSongName(musicFileList.get(i)), retuSonger(musicFileList.get(i)), musicDirList.get(i)));
		}
		mAdapter = new musicAdapter((LinkedList<musicData>)list, LocalMain.this);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent(LocalMain.this, MusicMain.class);
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
				finish();
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		});
		Intent intent = getIntent();
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
	}
	private void initView() {
		//显示菜单栏返回键
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		//隐藏电量运营商
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		name = (TextView) findViewById(R.id.name);
		songer = (TextView) findViewById(R.id.songer);
		go = (ImageButton) findViewById(R.id.go);
		button = (Button) findViewById(R.id.musicbutton);
		listView = (ListView) findViewById(R.id.listView1);
	}
	//菜单栏返回键功能
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MyAppMainActivity.class);
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
			finish();
			startActivity(intent);
			overridePendingTransition(0, 0);
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_app_main, menu);//这里是调用menu文件夹中的main.xml，在登陆界面label右上角的三角里显示其他功能
		return true;
	}
	//设置歌名歌手
	public void setName(String fileName) {
		//要求文件名为 《歌手 - 歌名》 或 《歌名》 格式  注：有空格
		System.out.println(fileName);
		if (fileName != null && !fileName.trim().equals("")) {
			String[] all = fileName.split(" - ");
			if (all.length >= 2) {
				name.setText(all[all.length - 1]);
				//分割歌手名
				String[] son = all[0].split(",");
				if (son.length > 2) {
					songer.setText(son[0] + "," + son[1] + "...");
				}else {
					songer.setText(all[0]);
				}
			}else {
				name.setText(all[0]);
				songer.setText("未知歌手");
			}
		}else {
			name.setText("未知歌曲");
			songer.setText("未知歌手");
		}
	}
	//设置歌名
	public String retuSongName(String fileName) {
		if (fileName != null && !fileName.trim().equals("")) {
			String[] all = fileName.split(" - ");
			if (all.length >= 2) {
				return all[all.length - 1];
			}else {
				return all[0];
			}
		}else {
			return "未知歌曲";
		}
	}
	//设置歌手
	public String retuSonger(String fileName) {
		if (fileName != null && !fileName.trim().equals("")) {
			String[] all = fileName.split(" - ");
			if (all.length >= 2) {
				//分割歌手名
				String[] son = all[0].split(",");
				if (son.length > 2) {
					return all[0] + "," + son[1];
				}else {
					return all[0];
				}
			}else {
				return "未知歌手";
			}
		}else {
			return "未知歌手";
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
				//				initSeekBar();
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
	//ListView触发控件
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		currIndex = position;
		start(); 
	}
}