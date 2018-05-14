package org.jitsi.android.model;

import org.litepal.crud.DataSupport;

/**
 * Created by snt1231 on 2017/4/21.
 */

public class ContactModel extends DataSupport{
    private String phoneNumber;
    private String callTime;//拨号时间点
    private String callState; //in 打入 out 打出
    private boolean connectBl; //true 接通 false 未接听
    private long id;
    private String callTimes;//通话总时长
    private boolean isSearchLocal=false;
    private String telLocal;//归属地
    private String contactName;//联系人姓名

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getTelLocal() {
        return telLocal;
    }

    public void setTelLocal(String telLocal) {
        this.telLocal = telLocal;
    }

    public boolean isSearchLocal() {
        return isSearchLocal;
    }

    public void setSearchLocal(boolean searchLocal) {
        isSearchLocal = searchLocal;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getCallState() {
        return callState;
    }

    public void setCallState(String callState) {
        this.callState = callState;
    }

    public boolean isConnectBl() {
        return connectBl;
    }

    public void setConnectBl(boolean connectBl) {
        this.connectBl = connectBl;
    }

    public String getCallTimes() {
        return callTimes;
    }

    public void setCallTimes(String callTimes) {
        this.callTimes = callTimes;
    }
}
