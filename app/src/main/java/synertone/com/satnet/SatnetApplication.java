package synertone.com.satnet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.multidex.MultiDex;

import org.jitsi.android.JitsiApplication;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import synertone.com.satnet.model.AccountModel;
import synertone.com.satnet.utils.CrashHandler;

/**
 * Created by snt1231 on 2017/3/27.
 */

public class SatnetApplication extends JitsiApplication {
    private List<Activity> actList;
    public static AccountModel accountModel;
    public static Typeface fontXiti;//表示细体字体
    //public static Typeface fontPutu;//表示常规字体
    @Override
    public void onCreate() {
        super.onCreate();
        actList = new ArrayList<>();
        x.Ext.init(this);
        initFontType();
        CrashHandler crashHandler=CrashHandler.getInstance();
        crashHandler.init(this);
    }

    private void initFontType() {
        fontXiti = Typeface.createFromAsset(getAssets(),
                "fonts/xiti.otf");
      /*  fontPutu = Typeface.createFromAsset(getAssets(),
                "fonts/changgui.otf");*/
    }

    public void addAct(Activity act) {
        this.actList.add(act);
    }

    public void removeAct(Activity act) {
        this.actList.remove(act);
    }
    public void finishAllAct() {
        for (Activity act : actList) {
            if (act != null) {
                act.finish();
            }
        }
        actList.clear();
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
