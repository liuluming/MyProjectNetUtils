package org.jitsi.android.gui.call;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jitsi.R;
import org.jitsi.adapter.CommonAdapter;
import org.jitsi.adapter.CommonViewHolder;
import org.jitsi.android.gui.util.AndroidCallUtil;
import org.jitsi.android.gui.util.TimeUtils;
import org.jitsi.android.model.CallPhoneNumberModel;
import org.jitsi.android.model.ContactModel;
import org.jitsi.android.model.SortModel;
import org.jitsi.android.model.TelBottomButtonModel;
import org.jitsi.android.util.ContactTools;
import org.jitsi.android.util.DensityUtils;
import org.jitsi.service.osgi.OSGiDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snt1231 on 2017/4/5.
 */

public class CallDialogFragment extends OSGiDialogFragment {
    private GridView gv_phone_number;
    private ImageView iv_tel_del;
    private ImageView iv_do_call;
    private TextView tv_display_number;
    private SoundPool sp;
    private int music;
    private ImageView iv_tel_back;
    private GridView gv_bottom_button;
    private ContactModel contactModel;
    private CallEndBroadcastReceiver endBroadcastReceiver;
    private ContactModel exist;
    private MyItemClick itemClick;
    private boolean isLongClick = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Animations_Dialog_fragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        //必须放在onresume 才能填充屏幕
        setDialogLayout(getDialog());
    }

    private void setDialogLayout(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            window.setDimAmount(0.0f);
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_call_phone, container, false);
        initView(view);
        initData();
        initEvent();
        return view;
    }

    StringBuilder telNumber = new StringBuilder();

    private void initEvent() {
        itemClick = new MyItemClick();
        gv_phone_number.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                CallPhoneNumberModel model = (CallPhoneNumberModel) parent.getItemAtPosition(position);
                if ("0".equals(model.getNumber())) {
                    isLongClick = true;
                    if (tv_display_number.getVisibility() == View.GONE) {
                        tv_display_number.setVisibility(View.VISIBLE);
                        getActivity().getActionBar().hide();
                    }
                    telNumber.append(model.getAlphabet());
                    tv_display_number.setText(telNumber.toString());
                }
                return false;
            }
        });
        gv_phone_number.setOnItemClickListener(itemClick);
        gv_bottom_button.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        dismissAllowingStateLoss();
                        break;
                    case 1:
                        String tel = tv_display_number.getText().toString();
                        if ("".equals(tel) || tel == null) {
                            Toast.makeText(getActivity(), R.string.please_input_tel, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        IntentFilter filter = new IntentFilter();
                        filter.addAction("end_call");
                        endBroadcastReceiver = new CallEndBroadcastReceiver();
                        getActivity().registerReceiver(endBroadcastReceiver, filter);
                        AndroidCallUtil
                                .createAndroidCall(
                                        getActivity(),
                                        iv_do_call,
                                        tel);
                        contactModel = new ContactModel();
                        contactModel.setPhoneNumber(tel);
                        SortModel contactPhone = ContactTools.isContactPhone(tel, getActivity());
                        if(contactPhone!=null){
                            contactModel.setContactName(contactPhone.getName());
                        }
                        contactModel.setCallState("out");
                        contactModel.setCallTime(TimeUtils.getNowTime());
                        contactModel.save();
                        tv_display_number.setText("");
                        tv_display_number.setVisibility(View.GONE);
                        telNumber=new StringBuilder();
                        getActivity().getActionBar().show();

                        break;
                    case 2:
                        if (telNumber.length() == 0) {
                            return;
                        }
                        telNumber.deleteCharAt(telNumber.length() - 1);
                        if (telNumber.length() == 0) {
                            tv_display_number.setVisibility(View.GONE);
                            getActivity().getActionBar().show();
                        }
                        tv_display_number.setText(telNumber.toString());
                        break;
                }
            }
        });
    }


    private void initData() {
        sp = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        music = sp.load(getActivity(), R.raw.click_tel_number, 1);
        List<CallPhoneNumberModel> modelList = new ArrayList<CallPhoneNumberModel>();
        initModelList(modelList);
        CommonAdapter<CallPhoneNumberModel> commonAdapter = new CommonAdapter<CallPhoneNumberModel>(getActivity(), R.layout.call_phone_number_item, modelList) {
            @Override
            protected void fillItemData(CommonViewHolder viewHolder, int position, CallPhoneNumberModel item) {
                View contentView = viewHolder.getContentView();
                ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
                layoutParams.width = DensityUtils.dip2px(getActivity(), 60);
                layoutParams.height = DensityUtils.dip2px(getActivity(), 60);
                contentView.setLayoutParams(layoutParams);
                viewHolder.setTextForTextView(R.id.tv_number, item.getNumber());
                viewHolder.setTextForTextView(R.id.tv_alphabet, item.getAlphabet());
            }
        };
        gv_phone_number.setAdapter(commonAdapter);
        List<TelBottomButtonModel> telButtonList = new ArrayList<TelBottomButtonModel>();
        TelBottomButtonModel t1 = new TelBottomButtonModel();
        t1.setDrawableId(R.drawable.iv_tel_back);
        telButtonList.add(t1);
        TelBottomButtonModel t2 = new TelBottomButtonModel();
        t2.setDrawableId(R.drawable.call);
        telButtonList.add(t2);
        TelBottomButtonModel t3 = new TelBottomButtonModel();
        t3.setDrawableId(R.drawable.iv_telnumber_del);
        telButtonList.add(t3);
        CommonAdapter<TelBottomButtonModel> telButtonAdapter = new CommonAdapter<TelBottomButtonModel>(getActivity(), R.layout.tel_bottom_button_item, telButtonList) {
            @Override
            protected void fillItemData(CommonViewHolder viewHolder, int position, TelBottomButtonModel item) {
                viewHolder.setImageForView(R.id.iv_button, item.getDrawableId());
            }
        };
        gv_bottom_button.setAdapter(telButtonAdapter);
    }

    private void initModelList(List<CallPhoneNumberModel> modelList) {
        for (int i = 1; i < 13; i++) {
            CallPhoneNumberModel model = new CallPhoneNumberModel();
            model.setNumber(i + "");
            switch (i) {
                case 1:
                    model.setAlphabet("&");
                    break;
                case 2:
                    model.setAlphabet("ABC");
                    break;
                case 3:
                    model.setAlphabet("DEF");
                    break;
                case 4:
                    model.setAlphabet("GHI");
                    break;
                case 5:
                    model.setAlphabet("JKL");
                    break;
                case 6:
                    model.setAlphabet("MNO");
                    break;
                case 7:
                    model.setAlphabet("PQRS");
                    break;
                case 8:
                    model.setAlphabet("TUV");
                    break;
                case 9:
                    model.setAlphabet("WXYZ");
                    break;
                case 10:
                    model.setNumber("*");
                    model.setAlphabet("(P)");
                    break;
                case 11:
                    model.setNumber("0");
                    model.setAlphabet("+");
                    break;
                case 12:
                    model.setNumber("#");
                    model.setAlphabet("(w)");
                    break;
            }
            modelList.add(model);
        }
    }

    private void initView(View view) {
        gv_phone_number = (GridView) view.findViewById(R.id.gv_phone_number);
        tv_display_number = (TextView) getActivity().findViewById(R.id.tv_display_number);
        gv_bottom_button = (GridView) view.findViewById(R.id.gv_bottom_button);
    }

    class CallEndBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            dismissAllowingStateLoss();
            String call_times = intent.getStringExtra("call_times");
           /* if(exist!=null){
                exist.setCallTimes(call_times);
                exist.update(exist.getId());
            }else{*/
            if (contactModel != null) {
                if ("00:00:00".equals(call_times)||"".equals(call_times)) {
                    contactModel.setConnectBl(false);
                    contactModel.setCallTimes("未接通");
                } else {
                    contactModel.setConnectBl(true);
                    contactModel.setCallTimes(call_times);
                }

                contactModel.update(contactModel.getId());
            }

            // }


        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (endBroadcastReceiver != null) {
            getActivity().unregisterReceiver(endBroadcastReceiver);
            endBroadcastReceiver=null;
        }

    }

    class MyItemClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isLongClick) {
                isLongClick = false;
                return;
            }
            sp.play(music, 1, 1, 0, 0, 1);
            CallPhoneNumberModel model = (CallPhoneNumberModel) parent.getItemAtPosition(position);
            telNumber.append(model.getNumber());
            getActivity().getActionBar().hide();
            if (tv_display_number.getVisibility() == View.GONE) {
                tv_display_number.setVisibility(View.VISIBLE);
            }
            tv_display_number.setText(telNumber.toString());
        }
    }
}
