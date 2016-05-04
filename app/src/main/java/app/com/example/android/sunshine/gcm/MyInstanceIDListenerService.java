package app.com.example.android.sunshine.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by root on 2/5/16.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Intent intent = new Intent(this,RegistrationIntentService.class);
        startService(intent);

    }
}
