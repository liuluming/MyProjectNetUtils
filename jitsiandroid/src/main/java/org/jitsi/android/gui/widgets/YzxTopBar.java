package org.jitsi.android.gui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jitsi.R;
import org.jitsi.android.util.DensityUtils;

public class YzxTopBar extends RelativeLayout {
	private LayoutInflater layoutInflater;
	private View myView;
	private LinearLayout ll_conver;
	private TextView tv_message;
	private TextView tv_tele;

	public YzxTopBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		layoutInflater = LayoutInflater.from(context);
		myView = layoutInflater.inflate(R.layout.view_topbar_yzx,null);
		ll_conver = (LinearLayout) myView.findViewById(R.id.ll_conver);
		tv_message = (TextView) myView.findViewById(R.id.tv_message);
		tv_tele = (TextView) myView.findViewById(R.id.tv_tele);
		addView(myView,new LayoutParams(DensityUtils.dip2px(context,200),DensityUtils.dip2px(context,40)));
	}



	public void setMessageBackgroudResource(int resid) {
		tv_message.setBackgroundResource(resid);
	}

	public void setTeleBackgroudResource(int resid) {
		tv_tele.setBackgroundResource(resid);
	}

	public void setMessageColor(int color) {
		tv_message.setTextColor(color);
	}

	public void setTeleColor(int color) {
		tv_tele.setTextColor(color);
	}

	public void setMessageOnclickListener(OnClickListener clickListener) {
		tv_message.setOnClickListener(clickListener);
	}

	public void setTeleOnclickListener(OnClickListener clickListener) {
		tv_tele.setOnClickListener(clickListener);
	}
}
