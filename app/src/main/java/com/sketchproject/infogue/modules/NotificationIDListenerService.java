package com.sketchproject.infogue.modules;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Sketch Project Studio
 * Created by Angga on 07/05/2016 15.07.
 */
public class NotificationIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent i = new Intent(this, RegistrationIntentService.class);
        startService(i);
    }
}
