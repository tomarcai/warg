package net.yura.domination.android.push;

import android.content.Intent;
import android.os.Bundle;
import com.amazon.device.messaging.ADMMessageHandlerBase;
import net.yura.android.push.PushRegistrar;
import net.yura.domination.R;
import net.yura.lobby.client.PushLobbyClient;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.midlet.MIDlet;

public class ADMLegacyIntentService extends ADMMessageHandlerBase {

    public ADMLegacyIntentService() {
        super(ADMLegacyIntentService.class.getName());
    }

    /**
     * Class constructor, including the className argument.
     *
     * @param className The name of the class.
     */
    public ADMLegacyIntentService(final String className) {
        super(className);
    }

    /** {@inheritDoc} */
    @Override
    protected void onMessage(final Intent intent) {
        ADMServerUtilities.logger.info("ADMMessageHandlerBase:onMessage " + intent);

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
        MIDlet.showNotification(getString(R.string.app_name), message, R.drawable.icon, -1, extras);
    }

    /** {@inheritDoc} */
    @Override
    protected void onRegistrationError(final String string) {
        ADMServerUtilities.logger.info("ADMMessageHandlerBase:onRegistrationError " + string);
    }

    /** {@inheritDoc} */
    @Override
    protected void onRegistered(final String registrationId) {
        ADMServerUtilities.logger.info("ADMMessageHandlerBase:onRegistered " + registrationId);

        /* Register the app instance's registration ID with your server. */
        ADMServerUtilities.logger.info("Device registered: regId = "+registrationId);
        ADMServerUtilities.registerOnLobbyServer(registrationId);
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnregistered(final String registrationId) {
        ADMServerUtilities.logger.info("ADMMessageHandlerBase:onUnregistered " + registrationId);

        /* Unregister the app instance's registration ID with your server. */
        if (PushRegistrar.isRegisteredOnServer(registrationId)) {
            ADMServerUtilities.unregisterOnLobbyServer();
        }
        else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            ADMServerUtilities.logger.info("Ignoring unregister callback");
        }
    }
    @Override
    protected void onSubscribe(final String topic) {
        ADMServerUtilities.logger.info("onSubscribe: " + topic);
    }

    @Override
    protected void onSubscribeError(final String topic, final String errorId) {
        ADMServerUtilities.logger.info("onSubscribeError: errorId: " + errorId + " topic: " + topic);
    }

    @Override
    protected void onUnsubscribe(final String topic) {
        ADMServerUtilities.logger.info("onUnsubscribe: " + topic);
    }

    @Override
    protected void onUnsubscribeError(final String topic, final String errorId) {
        ADMServerUtilities.logger.info("onUnsubscribeError: errorId: " + errorId + " topic: " + topic);
    }
}
