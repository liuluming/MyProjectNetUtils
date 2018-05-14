package synertone.com.satnet.utils;

import android.app.Activity;
import android.content.Intent;

import net.java.sip.communicator.service.gui.AccountRegistrationWizard;
import net.java.sip.communicator.service.protocol.AccountManager;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;

import org.jitsi.android.gui.AndroidGUIActivator;
import org.jitsi.android.gui.Jitsi;
import org.jitsi.android.gui.util.AndroidUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import synertone.com.satnet.HomeActivity;
import synertone.com.satnet.R;
import synertone.com.satnet.SatnetApplication;
import synertone.com.satnet.view.ToastUtil;

/**
 * Created by snt1231 on 2017/5/25.
 */

public class VoipUtils {
    public static int getAccountCount() {
        BundleContext osgiContext = AndroidGUIActivator.bundleContext;
        if(osgiContext == null)
        {
            // If OSGI has not started show splash screen as home
            //return LauncherActivity.class;
            ToastUtil.doToast(" OSGI has not started");
        }

        AccountManager accountManager
                = ServiceUtils.getService(osgiContext, AccountManager.class);

        // If account manager is null it means that OSGI has not started yet
        if(accountManager == null)
            ToastUtil.doToast(" OSGI has not started");//return LauncherActivity.class;

        return accountManager.getStoredAccounts().size();
    }
    public static  void handleVOIPLogin(Activity mContext,BundleContext bundleContext) {
        ProtocolProviderService protocolProvider
                = signIn(SatnetApplication.accountModel.getSubscriberId(), SatnetApplication.accountModel.getmStrPassW(), "SIP",mContext,bundleContext);

        if (protocolProvider != null)
        {
            //addAndroidAccount(protocolProvider);
            ToastUtil.doToast("VOIP登录成功");
            Intent showContactsIntent = new Intent(mContext,HomeActivity.class);
            mContext.startActivity(showContactsIntent);
            mContext.finish();
        }
    }
    public static  void handleVOIPLogin(Activity mContext,BundleContext bundleContext,String account,String password) {
        ProtocolProviderService protocolProvider
                = signIn(account,password, "SIP",mContext,bundleContext);

        if (protocolProvider != null)
        {
            //addAndroidAccount(protocolProvider);
            ToastUtil.doToast("VOIP登录成功");
            Intent showContactsIntent = new Intent(mContext,Jitsi.class);
            mContext.startActivity(showContactsIntent);
        }
    }
    private static ProtocolProviderService signIn(String userName,
                                                  String password,
                                                  String protocolName, Activity mContext, BundleContext bundleContext)
    {
       // BundleContext bundleContext = getBundlecontext();
        Logger logger = Logger.getLogger(Jitsi.class);

        ServiceReference<?>[] accountWizardRefs = null;
        try
        {
            accountWizardRefs = bundleContext.getServiceReferences(
                    AccountRegistrationWizard.class.getName(),
                    null);
        }
        catch (InvalidSyntaxException ex)
        {
            // this shouldn't happen since we're providing no parameter string
            // but let's log just in case.
            logger.error(
                    "Error while retrieving service refs", ex);
        }

        // in case we found any, add them in this container.
        if (accountWizardRefs == null)
        {
            logger.error("No registered registration wizards found");
            return null;
        }

        if (logger.isDebugEnabled())
            logger.debug("Found " + accountWizardRefs.length
                    + " already installed providers.");

        AccountRegistrationWizard selectedWizard = null;

        for (int i = 0; i < accountWizardRefs.length; i++)
        {
            AccountRegistrationWizard accReg
                    = (AccountRegistrationWizard) bundleContext
                    .getService(accountWizardRefs[i]);
            if (accReg.getProtocolName().equals(protocolName))
            {
                selectedWizard = accReg;
                break;
            }
        }
        if(selectedWizard == null)
        {
            logger.warn("No wizard found for protocol name: "+protocolName);
            return null;
        }
        try
        {
            selectedWizard.setModification(false);
            return selectedWizard.signin(userName, password);
        }
        catch (OperationFailedException e)
        {
            logger.error("Sign in operation failed.", e);

            if (e.getErrorCode() == OperationFailedException.ILLEGAL_ARGUMENT)
            {
                AndroidUtils.showAlertDialog(
                        mContext,
                        R.string.service_gui_LOGIN_FAILED,
                        R.string.service_gui_USERNAME_NULL);
            }
            else if (e.getErrorCode()
                    == OperationFailedException.IDENTIFICATION_CONFLICT)
            {
                AndroidUtils.showAlertDialog(
                        mContext,
                        R.string.service_gui_LOGIN_FAILED,
                        R.string.service_gui_USER_EXISTS_ERROR);
            }
            else if (e.getErrorCode()
                    == OperationFailedException.SERVER_NOT_SPECIFIED)
            {
                AndroidUtils.showAlertDialog(
                        mContext,
                        R.string.service_gui_LOGIN_FAILED,
                        R.string.service_gui_SPECIFY_SERVER);
            }
            else
            {
                AndroidUtils.showAlertDialog(
                        mContext,
                        R.string.service_gui_LOGIN_FAILED,
                        R.string.service_gui_ACCOUNT_CREATION_FAILED);
            }
        }
        catch (Exception e)
        {
            logger.error("Exception while adding account: "+e.getMessage(), e);
            AndroidUtils.showAlertDialog(
                    mContext,
                    R.string.service_gui_ERROR,
                    R.string.service_gui_ACCOUNT_CREATION_FAILED);
        }
        return null;
    }
}
