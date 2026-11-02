package net.yura.domination.android.push;

import java.util.HashMap;
import java.util.Map;
import javax.microedition.midlet.MIDlet;
import net.yura.android.push.PushRegistrar;
import net.yura.domination.R;
import net.yura.lobby.client.PushLobbyClient;
import net.yura.lobby.mini.MiniLobbyClient;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.amazon.device.messaging.ADMMessageHandlerJobBase;

/**
 *
 * com.amazon.device.messaging.ADMMessageHandlerJobBase For handling messages on the latest Fire devices
 * com.amazon.device.messaging.ADMMessageHandlerBase 	For handling messages on older Fire devices
 *
 * @see com.google.android.gcm.demo.app.GCMIntentService
 */
public class ADMIntentService extends ADMMessageHandlerJobBase {

    public ADMIntentService() {
        super(); // AndroidMeApp.getContext().getString(R.string.app_id)
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        ADMServerUtilities.logger.info("Device registered: regId = "+registrationId);
        ADMServerUtilities.registerOnLobbyServer(registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        ADMServerUtilities.logger.info("Device unregistered = " + registrationId);
        if (PushRegistrar.isRegisteredOnServer(registrationId)) {
            ADMServerUtilities.unregisterOnLobbyServer();
        }
        else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            ADMServerUtilities.logger.info("Ignoring unregister callback");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onRegistrationError(final Context context, final String string) {
        ADMServerUtilities.logger.info("ADMMessageHandlerJobBase:onRegistrationError " + string);
    }

    /**
     * @see MiniLobbyClient#notify(net.yura.lobby.model.Game, boolean)
     */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String msg=null, gameId=null, options=null;
        if (bundle != null) {
            msg = bundle.getString(PushLobbyClient.MESSAGE);
            gameId = bundle.getString(PushLobbyClient.GAME_ID);
            options = bundle.getString(PushLobbyClient.OPTIONS);
        }

        String message = msg==null?"Received message":msg;
        ADMServerUtilities.logger.info(message);
        // notifies user
        Map<String, Object> extras = new HashMap();
        if (gameId != null) {
            extras.put(PushLobbyClient.GAME_ID, gameId);
        }
        if (options != null) {
            extras.put(PushLobbyClient.OPTIONS, options);
        }
        MIDlet.showNotification(context.getString(R.string.app_name), message, R.drawable.icon, -1, extras);
    }

    @Override
    protected void onSubscribe(final Context context, final String topic) {
        ADMServerUtilities.logger.info("onSubscribe: " + topic);
    }

    @Override
    protected void onSubscribeError(final Context context, final String topic, final String errorId) {
        ADMServerUtilities.logger.info("onSubscribeError: errorId: " + errorId + " topic: " + topic);
    }

    @Override
    protected void onUnsubscribe(final Context context, final String topic) {
        ADMServerUtilities.logger.info("onUnsubscribe: " + topic);
    }

    @Override
    protected void onUnsubscribeError(final Context context, final String topic, final String errorId) {
        ADMServerUtilities.logger.info("onUnsubscribeError: errorId: " + errorId + " topic: " + topic);
    }
}
