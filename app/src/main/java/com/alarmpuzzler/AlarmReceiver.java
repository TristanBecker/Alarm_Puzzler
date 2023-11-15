package com.alarmpuzzler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 1000, 1000, 1000};
        vibrator.vibrate(vibrationPattern, -1);

        String message = intent.getStringExtra("message");
        String alarmType = intent.getStringExtra("alarmType");
        Log.d("AlarmReceiver", "Received - Message: " + message + ", Alarm Type: " + alarmType);
        NotificationHelper.showNotification(context, "Alarm", message, alarmType, 1);


        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();
    }

}

