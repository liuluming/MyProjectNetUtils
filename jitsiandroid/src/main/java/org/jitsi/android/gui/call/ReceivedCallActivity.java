/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.android.gui.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallState;
import net.java.sip.communicator.service.protocol.event.CallChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallChangeListener;
import net.java.sip.communicator.service.protocol.event.CallPeerEvent;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.call.CallManager;

import org.jitsi.R;
import org.jitsi.android.gui.util.TimeUtils;
import org.jitsi.android.model.ContactModel;
import org.jitsi.android.model.SortModel;
import org.jitsi.android.util.ContactTools;
import org.jitsi.service.osgi.OSGiActivity;

/**
 * The <tt>ReceivedCallActivity</tt> is the activity that corresponds to the
 * screen shown on incoming call.
 *
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class ReceivedCallActivity
        extends OSGiActivity
        implements CallChangeListener {
    /**
     * The logger
     */
    private final static Logger logger =
            Logger.getLogger(ReceivedCallActivity.class);

    /**
     * The identifier of the call.
     */
    private String callIdentifier;

    /**
     * The corresponding call.
     */
    private Call call;
    private Bundle extras;
    private CallEndBroadcastReceiver endBroadcastReceiver;
    private ContactModel contactModel;

    /**
     * {@inheritDoc}
     */
    public void onAttachedToWindow() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        + WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    /**
     * Called when the activity is starting. Initializes the call identifier.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.received_call);

        TextView displayNameView
                = (TextView) findViewById(R.id.calleeDisplayName);
        TextView addressView
                = (TextView) findViewById(R.id.calleeAddress);

        ImageView avatarView
                = (ImageView) findViewById(R.id.calleeAvatar);

        extras = getIntent().getExtras();

        displayNameView.setText(
                extras.getString(CallManager.CALLEE_DISPLAY_NAME));

        addressView.setText(extras.getString(CallManager.CALLEE_ADDRESS));

        byte[] avatar = extras.getByteArray(CallManager.CALLEE_AVATAR);
        if (avatar != null) {
            Bitmap bitmap
                    = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
            avatarView.setImageBitmap(bitmap);
        }

        callIdentifier = extras.getString(CallManager.CALL_IDENTIFIER);
        call = CallManager.getActiveCall(callIdentifier);
        if (call == null) {
            logger.error("There is no call with ID: " + callIdentifier);
            finish();
            return;
        }

        ImageView hangupView = (ImageView) findViewById(R.id.hangupButton);

        hangupView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*if (extras != null) {
                    saveCallInfo(false);

                }*/
                hangupCall();
            }
        });

        final ImageView callButton = (ImageView) findViewById(R.id.callButton);

        callButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              /*  if (extras != null) {
                    saveCallInfo(true);
                }*/
                answerCall(call, false);
            }
        });
    }

   /* private void saveCallInfo(boolean bl) {
        String tel = extras.getString(CallManager.CALLEE_ADDRESS);
        String[] split = tel.split("@");
        if(split!=null&&split.length>0){
            ContactModel exist = AndroidCallUtil.isExist(tel);
            if(exist!=null){
                exist.setCallTime(TimeUtils.getNowTime());
                exist.update(exist.getId());
            }else{
                ContactModel model = new ContactModel();
                model.setPhoneNumber(split[0]);
                model.setConnectBl(bl);
                model.setCallState("in");
                model.setCallTime(TimeUtils.getNowTime());
                model.save();
            }

        }
    }*/

    /**
     * Method mapped to answer button's onClick event
     *
     * @param v the answer with video button's <tt>View</tt>
     */
    public void onAnswerWithVideoClicked(View v) {
        if (call != null) {
            logger.trace("Answer call with video");
            answerCall(call, true);
        }
    }

    /**
     * Answers the given call and launches the call user interface.
     *
     * @param call     the call to answer
     * @param useVideo indicates if video shall be used
     */
    private void answerCall(final Call call, boolean useVideo) {
        CallManager.answerCall(call, useVideo);

        runOnUiThread(new Runnable() {
            public void run() {
                Intent videoCall
                        = VideoCallActivity
                        .createVideoCallIntent(
                                ReceivedCallActivity.this,
                                callIdentifier);
                startActivity(videoCall);
                //finish();
                String tel = extras.getString(CallManager.CALLEE_ADDRESS);
                String[] split = tel.split("@");
                if(split!=null&&split.length>0){
                    contactModel=new ContactModel();
                    contactModel.setConnectBl(true);
                    contactModel.setCallTime(TimeUtils.getNowTime());
                    String telNumber = split[0];
                    SortModel contactPhone = ContactTools.isContactPhone(telNumber, ReceivedCallActivity.this);
                    if(contactPhone!=null){
                        contactModel.setContactName(contactPhone.getName());
                    }
                    contactModel.setPhoneNumber(telNumber);
                    contactModel.setCallState("in");
                    contactModel.save();
                    IntentFilter filter=new IntentFilter();
                    filter.addAction("end_call");
                    endBroadcastReceiver = new CallEndBroadcastReceiver();
                    registerReceiver(endBroadcastReceiver,filter);
                }


            }
        });
    }
    class CallEndBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String call_times = intent.getStringExtra("call_times");
           /* if(exist!=null){
                exist.setCallTimes(call_times);
                exist.update(exist.getId());
            }else{*/
            if(contactModel!=null){
                contactModel.setCallTimes(call_times);
                contactModel.update(contactModel.getId());
            }

            // }
             finish();

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (call.getCallState().equals(CallState.CALL_ENDED)) {
            finish();
        } else {
            call.addCallChangeListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        if (call != null) {
            call.removeCallChangeListener(this);
        }

        super.onPause();
    }

    /**
     * Hangs up the call and finishes this <tt>Activity</tt>.
     */
    private void hangupCall() {
        CallManager.hangupCall(call);
        //finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Hangs up the call when back is pressed as this Activity will be
            // not displayed again.
            hangupCall();

            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Indicates that a new call peer has joined the source call.
     *
     * @param evt the <tt>CallPeerEvent</tt> containing the source call
     *            and call peer.
     */
    public void callPeerAdded(CallPeerEvent evt) {

    }

    /**
     * Indicates that a call peer has left the source call.
     *
     * @param evt the <tt>CallPeerEvent</tt> containing the source call
     *            and call peer.
     */
    public void callPeerRemoved(CallPeerEvent evt) {

    }

    /**
     * Indicates that a change has occurred in the state of the source call.
     *
     * @param evt the <tt>CallChangeEvent</tt> instance containing the source
     *            calls and its old and new state.
     */
    public void callStateChanged(CallChangeEvent evt) {
        if (evt.getNewValue().equals(CallState.CALL_ENDED)) {
            String tel = extras.getString(CallManager.CALLEE_ADDRESS);
            String[] split = tel.split("@");
            if(split!=null&&split.length>0){
                ContactModel contactModel=new ContactModel();
                contactModel.setConnectBl(false);
                contactModel.setCallTime(TimeUtils.getNowTime());
                contactModel.setCallTimes("未接通");
                String telNumber = split[0];
                SortModel contactPhone = ContactTools.isContactPhone(telNumber, this);
                if(contactPhone!=null){
                    contactModel.setContactName(contactPhone.getName());
                }
                contactModel.setPhoneNumber(telNumber);
                contactModel.setCallState("in");
                contactModel.save();
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(endBroadcastReceiver!=null){
            unregisterReceiver(endBroadcastReceiver);
        }
    }
}

