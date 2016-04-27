package com.example.mybluetoothchat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter{
	//表情图片ID
	private int[] mfacesId;
	//上下文环境
	private Context mContext;

	public GalleryAdapter(Context context,int[] facesid) {
		mContext=context;
		mfacesId=facesid;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mfacesId.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView img = new ImageView(mContext);
		img.setImageResource(mfacesId[position]);
		img.setScaleType(ImageView.ScaleType.FIT_XY);
		img.setLayoutParams(new GridView.LayoutParams(60, 60));
		// 设置显示比例类
		img.setScaleType(ImageView.ScaleType.FIT_CENTER);
		return img;
	}

}

