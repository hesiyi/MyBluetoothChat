package com.example.mybluetoothchat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceInfo {
	// 表情图片ID
	public int[] mfacesId = new int[] {
			R.drawable.f000, R.drawable.f001, R.drawable.f002, R.drawable.f003,
			R.drawable.f004, R.drawable.f005, R.drawable.f006, R.drawable.f007,
			R.drawable.f008, R.drawable.f009, R.drawable.f010, R.drawable.f011,
			R.drawable.f012, R.drawable.f013, R.drawable.f014, R.drawable.f015,
			R.drawable.f016, R.drawable.f017, R.drawable.f018, R.drawable.f019,
			R.drawable.f020, R.drawable.f021, R.drawable.f022, R.drawable.f023 };
	public String[] facesStr = new String[] { "笑脸", "微笑", "流汗", "偷笑", "拜拜",
			"敲打", "擦汗", "猪头", "玫瑰", "哭泣", "大哭", "憨笑", "耍酷", "大闹", "委屈", "狗屎",
			"炸弹", "菜刀", "憨厚", "色相", "害羞", "大兵", "大吐", "好感" };
	private Context mContext;

	public FaceInfo(Context context) {
		mContext = context;
	}

	/* 编辑框填入图片 */
	public SpannableStringBuilder ImagistSpan(String tag) {
		Pattern p = Pattern.compile("#\\{(.+?)\\}");
		Matcher m = p.matcher(tag);
		SpannableStringBuilder ss = null;
		ss = new SpannableStringBuilder(tag);
		int position;
		while (m.find()) {
			String find = m.group(1);
			position = 0;
			for (String face : facesStr) {
				if (find.compareTo(face) == 0) {
					break;
				}
				position++;
			}
			Drawable drawable = mContext.getResources().getDrawable(
					mfacesId[position]);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth() / 2,
					drawable.getIntrinsicHeight() / 2);
			ss.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM),
					m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return ss;
	}

}
