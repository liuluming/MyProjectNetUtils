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

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import org.jitsi.*;
import org.jitsi.android.*;
import org.jitsi.service.osgi.*;
import org.osgi.framework.*;
public class LauncherActivity
    extends OSGiActivity
{
    /**
     * Argument that holds an <tt>Intent</tt> that will be started once OSGi
     * startup is finished.
     */
    public static final String ARG_RESTORE_INTENT = "ARG_RESTORE_INTENT";
    /**
     * Intent instance that will be called once OSGi startup is finished.
     */
    private Intent restoreIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Request indeterminate progress for splash screen
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        if(OSGiService.isShuttingDown())
        {
            switchActivity(ShutdownActivity.class);
            return;
        }

        setProgressBarIndeterminateVisibility(true);
        setContentView(R.layout.main);

        // Starts fade in animation
        ImageView myImageView
                = (ImageView) findViewById(R.id.loadingImage);
        Animation myFadeInAnimation
                = AnimationUtils.loadAnimation(this, R.anim.fadein);
        if(myImageView!=null){
            myImageView.startAnimation(myFadeInAnimation);
        }

        // Get restore Intent and display "Restoring..." label
        Intent intent = getIntent();
        if(intent != null)
            this.restoreIntent = intent.getParcelableExtra(ARG_RESTORE_INTENT);
        View restoreView = findViewById(R.id.restoring);
        if(restoreView!=null){
            restoreView.setVisibility(
                    restoreIntent != null ? View.VISIBLE : View.GONE);
        }

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
                if(restoreIntent != null)
                {
                    // Starts restore intent
                    startActivity(restoreIntent);
                    finish();
                }
                else
                {
                    // Start home screen Activity
                    switchActivity(JitsiApplication.getHomeScreenActivityClass());
                }
            }
        });
    }

}
