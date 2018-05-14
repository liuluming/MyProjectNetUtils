package synertone.com.satnet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.xutils.http.RequestParams;
import org.xutils.x;

import synertone.com.satnet.model.AccountModel;
import synertone.com.satnet.utils.BitmapUtil;
import org.jitsi.android.util.MyCallback;
import synertone.com.satnet.utils.SharedPreferenceManager;
import synertone.com.satnet.utils.XTHttpUtil;

/**
 * Created by snt1231 on 2017/3/27.
 */

public class startActivity extends BaseActivity{
    private boolean firstLoad;
    private ImageView iv_app_start;
    private String mResult;
    private String mToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initView();
        initData();
    }

    private void goToLoginActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);

    }

    private void initData() {
        Bitmap bitmap= BitmapUtil.readBitMap(mContext,R.drawable.iv_app_start);
        iv_app_start.setImageBitmap(bitmap);
    }

    private void initView() {
        iv_app_start= (ImageView) findViewById(R.id.iv_app_start);
    }

    private void queryTokenStatusPost() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sessionToken", mToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestParams params = new RequestParams(XTHttpUtil.POST_TOKEN_STATUS);
        params.setAsJsonContent(true);
        params.setBodyContent(jsonObject.toString());
        x.http().post(params, new MyCallback(mContext, false) {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    mResult = jsonObject.getString("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mResult.equals("0")) {
                    goToHomeActivity();
                } else  {
                    goToLoginActivity();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                goToLoginActivity();
            }
        });
    }

    private void goToHomeActivity() {
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AccountModel accountModel=new AccountModel();
                accountModel.setSessionToken(mToken);
                accountModel.setSubscriberId( SharedPreferenceManager.getString(mContext, "subscriberId"));
                accountModel.setmStrPassW( SharedPreferenceManager.getString(mContext, "passwd"));
                SatnetApplication.accountModel=accountModel;
                Intent intent = new Intent(mContext, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        },1500);


    }
    @Override
    protected void start(BundleContext osgiContext)
            throws Exception
    {
        super.start(osgiContext);

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                firstLoad= SharedPreferenceManager.getBoolean(mContext,"firstLoad");
                mToken = SharedPreferenceManager.getString(mContext, "mToken");
                if(!firstLoad){
                    if(mToken==null){
                        goToLoginActivity();
                    }else{
                        queryTokenStatusPost();
                    }
                }else{
                    // 程序第一次安装，跳转到引导界面
                    SharedPreferenceManager.saveBoolean(mContext,"firstLoad",false);
                    Intent intent = new Intent(getApplicationContext(), GuidePagesActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
