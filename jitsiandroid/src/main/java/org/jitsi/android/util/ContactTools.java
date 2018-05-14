package org.jitsi.android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import com.github.promeg.pinyinhelper.Pinyin;
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict;

import org.jitsi.R;
import org.jitsi.android.JitsiApplication;
import org.jitsi.android.model.PinyinComparator;
import org.jitsi.android.model.SortModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactTools {

	private static List<SortModel> SourceDateList=new ArrayList<SortModel>();
	private static PinyinComparator pinyinComparator = new PinyinComparator();
	/**联系人显示名称**/
	private static final int PHONES_DISPLAY_NAME_INDEX = 0;
	/**电话号码**/
	private static final int PHONES_NUMBER_INDEX = 1;
	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, Phone.PHOTO_ID, Phone.CONTACT_ID};

	public static List<SortModel> getSourceDateList(){
		return SourceDateList;
	}

	public static void initContacts(final Context mContext) {
		List<SortModel> list = getPhoneContacts(mContext);
		SourceDateList.clear();
		SourceDateList.addAll(list);
		Collections.sort(SourceDateList, pinyinComparator);
	}

	/**得到手机通讯录联系人信息**/
	private static List<SortModel> getPhoneContacts(Context mContext) {
		List<SortModel> mSortList = new ArrayList<SortModel>();
		ContentResolver resolver = mContext.getContentResolver();

		// 获取手机联系人
		Cursor phoneCursor =resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);

		if (phoneCursor != null) {
			Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(JitsiApplication.getGlobalContext())));
			while (phoneCursor.moveToNext()) {

				SortModel sortModel = new SortModel();

				//得到手机号码
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				//当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				phoneNumber =  formatParentID(phoneNumber);//去掉电话号码内的+86 86
				//得到联系人名称
				String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
				Bitmap contactPhoto = null;
				sortModel.setName(contactName);
				sortModel.setId(phoneNumber);
				String pinyin = Pinyin.toPinyin(contactName,"");
				String sortString ;
				if (!TextUtils.isEmpty(pinyin) && pinyin.length() >= 1){
					sortString = pinyin.substring(0, 1).toUpperCase();
				}
				else {
					sortString = "";
				}

				if (sortString.matches("[A-Z]")) {
					sortModel.setSortLetters(sortString.toUpperCase());
				} else {
					sortModel.setSortLetters("#");
				}
				sortModel.setBitmap(contactPhoto);
				mSortList.add(sortModel);
			}
			phoneCursor.close();
		}
		return mSortList;
	}
	/**
	 *
	 * @author zhangbin
	 * @2016-1-31
	 * @param phoneNum
	 * @descript:去掉+86或86
	 */
	private static String formatParentID(String phoneNum){
		if(phoneNum.startsWith("+86")){
			phoneNum = phoneNum.substring(3, phoneNum.length());
		}else if(phoneNum.startsWith("86")){
			phoneNum = phoneNum.substring(2, phoneNum.length());
		}
		return phoneNum;
	}
	public static SortModel isContactPhone(String tel,Context mContext){
		List<SortModel> list = getPhoneContacts(mContext);
		if(list!=null){
			for(SortModel sortModel:list){
				if(sortModel.getId().equals(tel)||(ContactTools.getDialprefix(mContext)+sortModel.getId()).equals(tel)){
					return  sortModel;
				}
			}
		}
		return null;
	}
  public static String getDialprefix(Context mContext){
	 return mContext.getString(R.string.dial_prefix);
  }
}
