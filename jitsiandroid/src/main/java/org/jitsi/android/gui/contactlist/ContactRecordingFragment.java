package org.jitsi.android.gui.contactlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

import org.jitsi.R;
import org.jitsi.adapter.CommonAdapter;
import org.jitsi.adapter.CommonViewHolder;
import org.jitsi.android.gui.util.AndroidCallUtil;
import org.jitsi.android.gui.util.TimeUtils;
import org.jitsi.android.model.ContactModel;
import org.jitsi.android.util.MyCallback;
import org.jitsi.service.osgi.OSGiFragment;
import org.jitsi.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snt1231 on 2017/4/21.
 */

public class ContactRecordingFragment extends OSGiFragment{
    private ListView listView;
    private CommonAdapter<ContactModel> commonAdapter;
    private List<ContactModel> contactModels;
    private CallEndBroadcastReceiver endBroadcastReceiver;
    private ContactModel contactModel1;
    private boolean isLongClick=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view= inflater.inflate(R.layout.fragment_contact_recording,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isLongClick=true;
                final ContactModel contactModel = contactModels.get(position);
                final RelativePopupWindow popup = new RelativePopupWindow(view.getContext());
                popup.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(),R.color.transparent)));
                popup.setOutsideTouchable(true);
                popup.setContentView(LayoutInflater.from(getContext()).inflate(R.layout.popup_window_del,null));
                popup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.getContentView().findViewById(R.id.bt_del).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contactModel.delete();
                        contactModels.remove(contactModel);
                        commonAdapter.notifyDataSetChanged();
                        popup.dismiss();
                    }
                });
                popup.showOnAnchor(view, RelativePopupWindow.VerticalPosition.BELOW, RelativePopupWindow.HorizontalPosition.CENTER, false);
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isLongClick){
                    isLongClick=false;
                    return;
                }
                ContactModel contactModel = contactModels.get(position);
                //contactModel.delete();
                contactModel1=new ContactModel();
                contactModel1.setPhoneNumber(contactModel.getPhoneNumber());
                contactModel1.setContactName(contactModel.getContactName());
                contactModel1.setCallState("out");
                contactModel1.setCallTime(TimeUtils.getNowTime());
                contactModel1.save();
                IntentFilter filter=new IntentFilter();
                filter.addAction("end_call");
                endBroadcastReceiver = new CallEndBroadcastReceiver();
                getActivity().registerReceiver(endBroadcastReceiver,filter);
                String phoneNumber = contactModel.getPhoneNumber();
                AndroidCallUtil.createAndroidCall(getActivity(), view, phoneNumber);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        List<ContactModel> dbData = DataSupport.order("id desc").find(ContactModel.class);
        contactModels.clear();
        contactModels.addAll(dbData);
        commonAdapter.notifyDataSetChanged();

    }

    private void initData() {
        contactModels=new ArrayList<>();
        commonAdapter=new CommonAdapter<ContactModel>(getActivity(),R.layout.contact_list_item,contactModels) {
            @Override
            protected void fillItemData(CommonViewHolder viewHolder, int position, ContactModel item) {
                if(!StringUtils.isNullOrEmpty(item.getContactName())){
                    viewHolder.setTextForTextView(R.id.tv_tel,item.getContactName());
                }else{
                    viewHolder.setTextForTextView(R.id.tv_tel,item.getPhoneNumber());
                }

                if("in".equals(item.getCallState())){
                    viewHolder.setImageForView(R.id.iv_tel_dial,R.drawable.in_call);
                }else if("out".equals(item.getCallState())){
                    viewHolder.setImageForView(R.id.iv_tel_dial,R.drawable.out_call);
                }
                if(item.isConnectBl()){
                    viewHolder.setColorForTextView(R.id.tv_tel,getResources().getColor(R.color.black));
                }else{
                    viewHolder.setColorForTextView(R.id.tv_tel,getResources().getColor(R.color.red));
                }
                viewHolder.setTextForTextView(R.id.tv_tel_time,item.getCallTime());
                viewHolder.setTextForTextView(R.id.tv_call_times,item.getCallTimes());
                setTelLocalArea(viewHolder, item);

            }

        };
        listView.setAdapter(commonAdapter);
    }

    /**
     * 设置手机号码归属地
     * @param viewHolder
     * @param item
     */
    private void setTelLocalArea(CommonViewHolder viewHolder, ContactModel item) {
        if(item.getPhoneNumber().length()>=11){
            if(!item.isSearchLocal()){
                getLocate(item,viewHolder);
                item.setSearchLocal(true);
                item.update(item.getId());
            }else{
                viewHolder.setTextForTextView(R.id.tv_tel_local,item.getTelLocal());
            }
        }else{
            viewHolder.setTextForTextView(R.id.tv_tel_local,getString(R.string.unknown_local));
        }

    }

    private void getLocate(final ContactModel item, final CommonViewHolder viewHolder)  {
        RequestParams params=new RequestParams();
        String phoneNumber = item.getPhoneNumber();
        if(phoneNumber.startsWith("9")&&phoneNumber.length()==12){
            phoneNumber=phoneNumber.substring(1);
        }
        params.setUri("http://apicloud.mob.com/v1/mobile/address/query?key=105de5b7b0638&phone="+ phoneNumber);
        x.http().get(params,new MyCallback(getActivity(), false){
            @Override
            public void onSuccess(String result) {
                super.onSuccess(result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String province = jsonObject.optJSONObject("result").optString("province");
                    String city = jsonObject.optJSONObject("result").optString("city");
                    if(province.equals(city)){
                        province = "";
                    }
                    item.setTelLocal(province + " " + city);
                    item.update(item.getId());
                    commonAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    item.setTelLocal(getString(R.string.unknown_local));
                    item.update(item.getId());
                    commonAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                super.onError(ex, isOnCallback);
                item.setTelLocal(getString(R.string.unknown_local));
                item.update(item.getId());
                commonAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initView() {
         listView=(ListView)getView().findViewById(R.id.lv_contact_list);
    }
    class CallEndBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String call_times = intent.getStringExtra("call_times");
            if(contactModel1!=null){
                if("00:00:00".equals(call_times)||"".equals(call_times)){
                    contactModel1.setConnectBl(false);
                    contactModel1.setCallTimes("未接通");
                }else{
                    contactModel1.setConnectBl(true);
                    contactModel1.setCallTimes(call_times);
                }

                contactModel1.update(contactModel1.getId());
            }
        }
     }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(endBroadcastReceiver!=null){
            getActivity().unregisterReceiver(endBroadcastReceiver);
        }
    }

}
