package net.yura.domination.android.push;

import java.util.logging.Logger;
import android.content.Context;
import com.amazon.device.messaging.ADM;
import net.yura.android.push.PushRegistrar;
import net.yura.lobby.client.Connection;
import net.yura.lobby.client.PushLobbyClient;
import net.yura.lobby.mini.MiniLobbyClient;

public class ADMServerUtilities implements PushLobbyClient {

    static final Logger logger = Logger.getLogger(ADMServerUtilities.class.getName());

    public static final String ADM_CLASSNAME = "com.amazon.device.messaging.ADM";
    public static final String ADMV2_HANDLER = "com.amazon.device.messaging.ADMMessageHandlerJobBase";

    public static final boolean IS_ADM_AVAILABLE;
    public static final boolean IS_ADM_V2;

    private static boolean isClassAvailable(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static {
        IS_ADM_AVAILABLE = isClassAvailable(ADM_CLASSNAME);
        IS_ADM_V2 = IS_ADM_AVAILABLE ? isClassAvailable(ADMV2_HANDLER) : false;
    }

    /**
     * Register the app with ADM and send the registration ID to your server
     */
    private void register() {
        Context context = net.yura.android.AndroidMeApp.getContext();

        final ADM adm = new ADM(context);
        if (adm.isSupported()) {
            if(adm.getRegistrationId() == null) {
                adm.startRegister();
            }
            else {
                /* Send the registration ID for this app instance to your server. */
                /* This is a redundancy since this should already have been performed at registration time from the onRegister() callback */
                /* but we do it because our python server doesn't save registration IDs. */

                if (PushRegistrar.isRegisteredOnServer(adm.getRegistrationId())) {
                    logger.info("Already registered");
                }
                else {
                    ADMServerUtilities.registerOnLobbyServer(adm.getRegistrationId());
                }
            }
        }
    }

    /**
     * Unregister the app with ADM.
     * Your server will get notified from the SampleADMMessageHandler:onUnregistered() callback
     *
     * @see ADMIntentService#onUnregistered(Context, String)
     */
    private void unregister() {
        Context context = net.yura.android.AndroidMeApp.getContext();

        final ADM adm = new ADM(context);
        if (adm.isSupported()) {
            if(adm.getRegistrationId() != null) {
                adm.startUnregister();
            }
        }

        PushRegistrar.setRegisteredOnServer(null);
    }

    public static void registerOnLobbyServer(String registrationId) {
        MiniLobbyClient con = PushActivity.getLobbyConnection();
        if (con != null) {
            con.setPushToken(PUSH_SYSTEM_ADM, registrationId, new ADMServerUtilities(registrationId));
        }
    }

    public static void unregisterOnLobbyServer() {
        MiniLobbyClient con = PushActivity.getLobbyConnection();
        if (con != null) {
            con.setPushToken(PUSH_SYSTEM_ADM, null, new ADMServerUtilities(null));
        }
    }

    private String token;
    public ADMServerUtilities(String token) {
        this.token = token;
    }

    @Override
    public void registerDone() {
        PushRegistrar.setRegisteredOnServer(token);
    }
}
