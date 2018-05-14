package org.jitsi.android.gui.contactlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import org.jitsi.R;
import org.jitsi.adapter.SortAdapter;
import org.jitsi.android.gui.util.AndroidCallUtil;
import org.jitsi.android.gui.util.TimeUtils;
import org.jitsi.android.gui.widgets.MyListView;
import org.jitsi.android.gui.widgets.SideBar;
import org.jitsi.android.model.ContactModel;
import org.jitsi.android.model.SortModel;
import org.jitsi.android.util.ContactTools;

import java.util.List;

public class ContactFrament extends Fragment {
	private MyListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;
	private View mView;
	private List<SortModel> sourceDateList;
	private ContactModel contactModel;
	private CallEndBroadcastReceiver endBroadcastReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_contact, container, false);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initView();
		initContacts();
	}
	private void initContacts() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				//if(ContactTools.getSourceDateList().size()<1){
					ContactTools.initContacts(getActivity());
					mHandler.sendEmptyMessage(1);
				//}
			}
		}).start();
	}

	public Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
				case 1:
					if(adapter!=null){
						adapter.updateListView(ContactTools.getSourceDateList());
					}
					break;
			}
		}
	};

	public void initView() {
		sideBar = (SideBar) mView.findViewById(R.id.sidrbar);
		dialog = (TextView) mView.findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		sortListView = (MyListView) mView.findViewById(R.id.country_lvcountry);
		sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
                if(adapter==null){
					return;
				}
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position+1);
				}

			}
		});
		sourceDateList = ContactTools.getSourceDateList();
		adapter = new SortAdapter(getActivity(), sourceDateList);
		sortListView.setAdapter(adapter);
		sortListView.setTopRefresh(false);
		sortListView.setonRefreshListener(new MyListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						sortListView.onRefreshComplete();
					}
				}, 500);

			}
		});
		sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				SortModel sortModel = sourceDateList.get(position-1);
					contactModel=new ContactModel();
				    contactModel.setContactName(sortModel.getName());
					contactModel.setPhoneNumber(ContactTools.getDialprefix(getActivity())+sortModel.getId());
					contactModel.setCallState("out");
					contactModel.setCallTime(TimeUtils.getNowTime());
					contactModel.save();
				IntentFilter filter=new IntentFilter();
				filter.addAction("end_call");
				endBroadcastReceiver = new CallEndBroadcastReceiver();
				getActivity().registerReceiver(endBroadcastReceiver,filter);
				AndroidCallUtil
						.createAndroidCall(
								getActivity(),
								view,
								contactModel.getPhoneNumber());

			}
		});

	}
	class CallEndBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String call_times = intent.getStringExtra("call_times");
			if(contactModel!=null){
				if("00:00:00".equals(call_times)||"".equals(call_times)){
					contactModel.setConnectBl(false);
					contactModel.setCallTimes("未接通");
				}else{
					contactModel.setConnectBl(true);
					contactModel.setCallTimes(call_times);
				}

				contactModel.update(contactModel.getId());
			}
		}
	}
	@Override
	public void onResume() {
		super.onResume();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(endBroadcastReceiver!=null){
			getActivity().unregisterReceiver(endBroadcastReceiver);
		}

	}
}

