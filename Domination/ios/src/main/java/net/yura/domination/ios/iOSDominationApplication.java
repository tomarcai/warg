package net.yura.domination.ios;

import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.ios.KeychainUtil;
import net.yura.lobby.client.LobbySettings;
import net.yura.lobby.util.SimplePreferences;
import net.yura.mobile.gui.DesktopPane;
import java.io.File;
import java.util.prefs.Preferences;
import apple.foundation.c.Foundation;
import apple.foundation.enums.NSSearchPathDirectory;
import apple.foundation.enums.NSSearchPathDomainMask;

public class iOSDominationApplication extends DominationMain {

    @Override
    public void initialize(DesktopPane rootpane) {
/*
        // alternative to saving in keychain is using a app group preferences
        NSUserDefaults sharedDefaults = NSUserDefaults.alloc().initWithSuiteName("group.net.yura.lobby");
        String lobbyUUID = sharedDefaults.stringForKey("uuid");
*/
        try {
            String group = "GP37R5KJ29.net.yura.lobby";
            String service = null;

            // clear
            //KeychainUtil.delete(group, service, LobbySettings.UUID_KEY);

            String keychainUUID = KeychainUtil.load(group, service, LobbySettings.UUID_KEY);
            String documentsDirectory = Foundation.NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.UserDomainMask, true).firstObject();
            Preferences prop = new SimplePreferences(new File(documentsDirectory, LobbySettings.LOBBY_OLD_SETTINGS_FILE), null);
            String appFilesUUID = prop.get(LobbySettings.UUID_KEY, null);

            // check if id exists in the keychain
            if (keychainUUID != null) {
                // if it exists then save it to the lobby file
                if (!keychainUUID.equals(appFilesUUID)) {
                    prop.put(LobbySettings.UUID_KEY, keychainUUID);
                    prop.flush();
                }
            }
            // check if lobby id exists,
            else if (appFilesUUID != null) {
                // if it exists then save it to the keychain
                KeychainUtil.save(group, service, LobbySettings.UUID_KEY, appFilesUUID);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        super.initialize(rootpane);
    }
}
