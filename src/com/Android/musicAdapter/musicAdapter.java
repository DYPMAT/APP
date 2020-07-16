package com.Android.musicAdapter;

import java.util.LinkedList;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class musicAdapter extends BaseAdapter {

	private LinkedList<musicData> mData;
	private Context mContext;

	public musicAdapter(LinkedList<musicData> mData, Context mContext) {
		super();
		this.mData = mData;
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(mContext).inflate(R.layout.simple_list_item_2,parent,false);
		TextView musicName = (TextView) convertView.findViewById(R.id.text1);
		TextView songerName = (TextView) convertView.findViewById(R.id.text2);
		musicName.setTextSize(14);
		songerName.setTextSize(12);
		musicName.setTextColor(android.graphics.Color.WHITE);
		songerName.setTextColor(android.graphics.Color.WHITE);
		musicName.setText(mData.get(position).getMusicName());
		songerName.setText(mData.get(position).getSongerName());
		return convertView;
	}

}
