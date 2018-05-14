package org.jitsi.android.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;

public class DensityUtils {
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

	public static float getDensity(Activity context) {
		DisplayMetrics metrics = new DisplayMetrics();
		Display display = context.getWindowManager().getDefaultDisplay();
		display.getMetrics(metrics);
		return metrics.density;
	}
	
	public static float getDensityDpi(Activity context) {
		DisplayMetrics metrics = new DisplayMetrics();
		Display display = context.getWindowManager().getDefaultDisplay();
		display.getMetrics(metrics);
		return metrics.densityDpi;
	}
}
