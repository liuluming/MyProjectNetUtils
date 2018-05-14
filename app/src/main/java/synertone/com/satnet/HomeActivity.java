package synertone.com.satnet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import org.jitsi.android.gui.Jitsi;

import java.util.ArrayList;
import java.util.List;

import synertone.com.satnet.activity.person.PersonalActivity;
import synertone.com.satnet.adapter.CommonAdapter;
import synertone.com.satnet.adapter.CommonViewHolder;
import synertone.com.satnet.model.HomeMenuModel;
import synertone.com.satnet.utils.ScreenUtil;
import synertone.com.satnet.utils.VoipUtils;
import synertone.com.satnet.view.AmwellImageCycleView;
import synertone.com.satnet.view.MyGridView;

import static synertone.com.satnet.view.AmwellImageCycleView.ImageCycleViewListener;

public class HomeActivity extends BaseActivity {

    private AmwellImageCycleView amwell_ad;
    private int bannerHeight;
    private MyGridView mgv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        setBannerHeight();
        initBanner();
        initMenu();
        initEvent();
    }

    private void initEvent() {
        mgv_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent mIntent = new Intent(mContext, PersonalActivity.class);
                        startActivity(mIntent);
                        break;
                    case 1:
                        AndPermission.with(HomeActivity.this)
                                .requestCode(100)
                                .permission(Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS)
                                .rationale(new RationaleListener() {
                                    @Override
                                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                                        AndPermission.rationaleDialog(mContext, rationale)
                                                .show();
                                    }
                                })
                                .callback(new PermissionListener() {
                                    @Override
                                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                        if (requestCode == 100) {
                                            handleVOIP();
                                        }
                                    }

                                    @Override
                                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {

                                    }
                                })
                                .start();

                        break;
                }
            }
        });
    }

    private void handleVOIP() {
        int accountCount = VoipUtils.getAccountCount();
        if (accountCount == 0) {
            showRegisterVoipDialog();
        } else {
            Intent intent = new Intent(mContext, Jitsi.class);
            startActivity(intent);
        }
    }

    private void showRegisterVoipDialog() {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.getWindow().setContentView(R.layout.dialog_voip_login);
        final EditText et_account = (EditText) dialog.getWindow().findViewById(R.id.et_account);
        final EditText et_password = (EditText) dialog.getWindow().findViewById(R.id.et_password);
        dialog.getWindow().findViewById(R.id.dialog_tv_cencel)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
        dialog.findViewById(R.id.dialog_tv_sure).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        loginVoip(et_account.getText().toString(), et_password.getText().toString());

                    }
                });
    }

    private void loginVoip(String account, String password) {
        VoipUtils.handleVOIPLogin((Activity) mContext, getBundlecontext(), account, password);
    }

    private void initMenu() {
        List<HomeMenuModel> homeMenuModels = new ArrayList<>();
        homeMenuModels.add(new HomeMenuModel("个人中心", R.drawable.icon_gerenzhognxin));
        homeMenuModels.add(new HomeMenuModel("语音电话", R.drawable.iv_voip));
        CommonAdapter<HomeMenuModel> commonAdapter = new CommonAdapter<HomeMenuModel>(mContext, R.layout.home_menu_item, homeMenuModels) {
            @Override
            protected void fillItemData(CommonViewHolder viewHolder, int position, HomeMenuModel item) {
                viewHolder.setTextForTextView(R.id.tv_title, item.getTitle());
                viewHolder.setImageForView(R.id.iv_content, item.getImageResId());
            }
        };
        mgv_content.setAdapter(commonAdapter);
    }

    private void setBannerHeight() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            bannerHeight = (int) (ScreenUtil.getHight(mContext) * 0.3);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bannerHeight = (int) (ScreenUtil.getHight(mContext) * 0.4);
        }
        initHeight();
    }

    private void initBanner() {
        amwell_ad.setImageResources(null, mAdCycleViewListener);
        if (amwell_ad != null) {
            amwell_ad.startImageCycle();
        }
    }

    private void initHeight() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) amwell_ad.getLayoutParams();
        params.height = bannerHeight;
        amwell_ad.setLayoutParams(params);
    }

    private void initView() {
        amwell_ad = (AmwellImageCycleView) findViewById(R.id.amwell_ad);
        mgv_content = (MyGridView) findViewById(R.id.mgv_content);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            application.finishAllAct();
        }
        return super.onKeyDown(keyCode, event);
    }

    private ImageCycleViewListener mAdCycleViewListener = new ImageCycleViewListener() {

        @Override
        public void displayImage(String imageURL, ImageView imageView) {

        }

        @Override
        public void onImageClick(int position, View imageView, int type) {

        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setBannerHeight();
    }
}
