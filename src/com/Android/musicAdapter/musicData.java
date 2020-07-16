package com.Android.musicAdapter;

public class musicData {
	private String musicName;
	private String songerName;
	private String musicPath;

	public musicData() {
		super();
	}

	public musicData(String musicName, String songerName, String musicPath) {
		super();
		this.musicName = musicName;
		this.songerName = songerName;
		this.musicPath = musicPath;
	}

	public String getMusicName() {
		return musicName;
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getSongerName() {
		return songerName;
	}

	public void setSongerName(String songerName) {
		this.songerName = songerName;
	}

	public String getMusicPath() {
		return musicPath;
	}

	public void setMusicPath(String musicPath) {
		this.musicPath = musicPath;
	}
}
