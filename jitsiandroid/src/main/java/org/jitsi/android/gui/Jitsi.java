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
package org.jitsi.android.gui;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.came.viewbguilib.ButtonBgUi;

import net.java.sip.communicator.util.Logger;

import org.jitsi.R;
import org.jitsi.android.gui.call.CallDialogFragment;
import org.jitsi.android.gui.contactlist.ContactFrament;
import org.jitsi.android.gui.contactlist.ContactRecordingFragment;
import org.jitsi.android.gui.fragment.ActionBarStatusFragment;
import org.jitsi.android.gui.menu.MainMenuActivity;
import org.jitsi.android.gui.util.AndroidUtils;
import org.jitsi.android.gui.widgets.NoScrollViewPager;
import org.jitsi.android.gui.widgets.YzxTopBar;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * The main <tt>Activity</tt> for Jitsi application.
 *
 * @author Damian Minkov
 * @author Lyubomir Marinov
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class Jitsi
        extends MainMenuActivity {
    /**
     * The logger
     */
    private static final Logger logger = Logger.getLogger(Jitsi.class);

    /**
     * The action that will show contacts.
     */
    public static final String ACTION_SHOW_CONTACTS = "org.jitsi.show_contacts";

    /**
     * The action that will show chat with contact given
     * in <tt>CONTACT_EXTRA</tt>.
     */
    //public static final String ACTION_SHOW_CHAT ="org.jitsi.show_chat";

    /**
     * Contact argument used to show the chat.
     * It must be the <tt>MetaContact</tt> UID string.
     */
    //public static final String CONTACT_EXTRA = "org.jitsi.chat.contact";

    /**
     * A call back parameter.
     */
    public static final int OBTAIN_CREDENTIALS = 1;

    /**
     * The main view fragment containing the contact list and also the chat in
     * the case of a tablet interface.
     */
    private ContactRecordingFragment contactListFragment;

    /**
     * Variable caches instance state stored for example on on rotate event to
     * prevent from recreating the contact list after rotation.
     * It is passed as second argument of {@link #handleIntent(Intent, Bundle)}
     * when called from {@link #onNewIntent(Intent)}.
     */
    private Bundle instanceState;
    private YzxTopBar yzxTopBar;
    private NoScrollViewPager mViewPager;
    private List<Fragment> mTabs = new ArrayList<Fragment>();
    private ContactFrament mContactFrament;
    private ButtonBgUi bt_dial;

    /**
     * Called when the activity is starting. Initializes the corresponding
     * call interface.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Checks if OSGi has been started and if not starts
        // LauncherActivity which will restore this Activity
        // from it's Intent.
        if (postRestoreIntent()) {
            return;
        }
        setContentView(R.layout.main_view);
        initView();
        initData();
        initEvent();
        setOverflowShowingAlways();
       // boolean isTablet = AndroidUtils.isTablet();

        if (savedInstanceState == null) {
            // Inserts ActionBar functionality
            if (AndroidUtils.hasAPI(11)) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(new ActionBarStatusFragment(), "action_bar")
                        .commit();
            }

           /* if (isTablet) {
                // OTR menu padlock
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(new OtrFragment(), "otr_fragment")
                        .commit();
            }*/
        }

        handleIntent(getIntent(), savedInstanceState);
    }

    private void initData() {
        yzxTopBar.setMessageBackgroudResource(R.drawable.message_view_press_bg);
        yzxTopBar.setMessageColor(getResources().getColor(R.color.white));
        contactListFragment = new ContactRecordingFragment();
        mTabs.add(contactListFragment);
        // 构造通讯录Fragment
        mContactFrament = new ContactFrament();
        mTabs.add(mContactFrament);
        FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mTabs.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mTabs.get(arg0);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                return super.instantiateItem(container, position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                super.destroyItem(container, position, object);
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.setNoScroll(true);
    }

    private void initEvent() {
        bt_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing()) {
                    showCallDialog();
                }

            }
        });
        yzxTopBar.setMessageOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通话记录
                mViewPager.setCurrentItem(0, false);
                yzxTopBar.setTeleBackgroudResource(R.drawable.tele_view_bg);
                yzxTopBar.setTeleColor(getResources().getColor(R.color.black));
                yzxTopBar.setMessageBackgroudResource(R.drawable.message_view_press_bg);
                yzxTopBar.setMessageColor(getResources().getColor(R.color.white));
                //isTeleFragment = true;
                bt_dial.setVisibility(View.VISIBLE);
            }
        });
        yzxTopBar.setTeleOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 通讯录
                bt_dial.setVisibility(View.GONE);
                mViewPager.setCurrentItem(1, false);
                yzxTopBar.setTeleBackgroudResource(R.drawable.tele_view_press_bg);
                yzxTopBar.setTeleColor(getResources().getColor(R.color.white));
                yzxTopBar.setMessageBackgroudResource(R.drawable.message_view_bg);
                yzxTopBar.setMessageColor(getResources().getColor(R.color.black));
                //isTeleFragment = false;
            }
        });
    }

    CallDialogFragment callDialogFragment;

    private void showCallDialog() {
        if (callDialogFragment == null) {
            callDialogFragment = new CallDialogFragment();
        }
        callDialogFragment.show(getSupportFragmentManager(), "Jitsi");
    }

    private void initView() {
        bt_dial = (ButtonBgUi) findViewById(R.id.bt_dial);
         yzxTopBar= (YzxTopBar) findViewById(R.id.actionBar_chat);
        mViewPager= (NoScrollViewPager) findViewById(R.id.viewpager);
    }

    /**
     * Called when new <tt>Intent</tt> is received(this <tt>Activity</tt> is
     * launched in <tt>singleTask</tt> mode.
     *
     * @param intent new <tt>Intent</tt> data.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, instanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.instanceState = savedInstanceState;
    }

    @Override
    protected void onPause() {
        super.onPause();

        instanceState = null;
    }

    /**
     * Decides what should be displayed based on supplied <tt>Intent</tt> and
     * instance state.
     *
     * @param intent             <tt>Activity</tt> <tt>Intent</tt>.
     * @param savedInstanceState <tt>Activity</tt> instance state.
     */
    private void handleIntent(Intent intent, Bundle savedInstanceState) {
        String action = intent.getAction();

        if (savedInstanceState != null) {
            // Restores the contact list fragment
            return;
        }

        if (Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            logger.warn("Search intent not handled for query: " + query);
        } else
        // Both show contact and show chat actions are handled here
        // else if(ACTION_SHOW_CONTACTS.equals(action)
        //        || ACTION_SHOW_CHAT.equals(action))
        {
            // Show contacts request
        }
    }



    /**
     * Called when an activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        synchronized (this) {
            BundleContext bundleContext = getBundlecontext();
            if (bundleContext != null)
                try {
                    stop(bundleContext);
                } catch (Throwable t) {
                    logger.error(
                            "Error stopping application:"
                                    + t.getLocalizedMessage(), t);
                    if (t instanceof ThreadDeath)
                        throw (ThreadDeath) t;
                }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent accountLoginIntent) {
        super.onActivityResult(requestCode, resultCode, accountLoginIntent);

        switch (requestCode) {
            case OBTAIN_CREDENTIALS:
                if (resultCode == RESULT_OK) {
                    System.err.println("ACCOUNT DATA STRING===="
                            + accountLoginIntent.getDataString());
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        disableABCShowHideAnimation(getActionBar());
        return super.onCreateOptionsMenu(menu);
    }

    private static void disableABCShowHideAnimation(ActionBar actionBar) {
        try
        {
            actionBar.getClass().getDeclaredMethod("setShowHideAnimationEnabled", boolean.class).invoke(actionBar, false);

        } catch (Exception exception)
        {
            try {
                Field mActionBarField = actionBar.getClass().getSuperclass().getDeclaredField("mActionBar");
                mActionBarField.setAccessible(true);
                Object icsActionBar = mActionBarField.get(actionBar);
                Field mShowHideAnimationEnabledField = icsActionBar.getClass().getDeclaredField("mShowHideAnimationEnabled");
                mShowHideAnimationEnabledField.setAccessible(true);
                mShowHideAnimationEnabledField.set(icsActionBar, false);
                Field mCurrentShowAnimField = icsActionBar.getClass().getDeclaredField("mCurrentShowAnim");
                mCurrentShowAnimField.setAccessible(true);
                mCurrentShowAnimField.set(icsActionBar, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}