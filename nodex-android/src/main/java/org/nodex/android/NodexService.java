package org.nodex.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * NodeX Android Background Service
 */
public class NodexService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
