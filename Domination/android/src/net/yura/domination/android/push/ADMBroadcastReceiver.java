package net.yura.domination.android.push;

import com.amazon.device.messaging.ADMMessageReceiver;

public class ADMBroadcastReceiver extends ADMMessageReceiver {

    public static final int JOB_ID = 1324124;

    public ADMBroadcastReceiver() {
        super(ADMLegacyIntentService.class);
        if(ADMServerUtilities.IS_ADM_V2) {
            registerJobServiceClass(ADMIntentService.class, JOB_ID);
        }
    }
}
