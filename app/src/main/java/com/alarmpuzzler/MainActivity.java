//Tristan Becker 20020816
//159336 Assignment 3

/*
Notes
THis app is designed to create alarms that force the user to solve a small math equation or answer a trivia question to disable so th user wakes up properly

The alarm does not always start at the start of minute it can take a few seconds
The trivia alarm type has not been implemented yet so i have set it up to just use Math puzzles
*/

package com.alarmpuzzler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.processphoenix.ProcessPhoenix;

public class MainActivity extends AppCompatActivity {

    TimePicker alarmTimePicker;
    AlarmManager alarmManager;
    int alarmNumber = 0;
    List<Alarm> alarmList = new ArrayList<>();
    List<PendingIntent> pendingIntentList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission();
        }

        NotificationHelper.createNotificationChannel(this);

        // Check if the activity came fro a notification and if so what type of alarm is encoded in
        if (getIntent().hasExtra("alarmType")) {
            String alarmType = getIntent().getStringExtra("alarmType");
            if ("Math".equals(alarmType)) {
                openMathAlarmView();
            } else if ("Trivia".equals(alarmType)) {
                openMathAlarmView();
            }
        } else {
            // else open the app as usual
            loadAlarms();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    private void loadAlarms() {
        setContentView(R.layout.activity_main);


        //Adds a listener for the floating action button to add extra alarms
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(add_alarm -> {
            addAlarm();
        });
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        String alarmsJson = sharedPreferences.getString("alarms", null);

        // takes the serialized list of alarms and rebuilds the alarmlist
        if (alarmsJson != null) {
            alarmList = deserializeAlarmList(alarmsJson);
        }


        //populates the list of alarms and makes them clickable to delete them
        for (int i = 0; i < alarmList.size(); i++) {
            String time;
            final int position = i;
            TextView textView = new TextView(this);
            Alarm currentAlarm = alarmList.get(i);
            long minute = (currentAlarm.getTime() / (1000 * 60)) % 60;
            long hour = (currentAlarm.getTime() / (1000 * 60 * 60)) % 24;
            if (hour < 12){
                hour = hour +1;
                time = String.format("%d:%02d PM", hour, minute);
            }
            else{
                hour = hour - 11;
                time = String.format("%d:%02d AM", hour, minute);
            }

            String output = "Time: " + time + "\nMessage: " + currentAlarm.getMessage() + "\nAlarm Type: " + currentAlarm.getAlarmType();
            textView.setText(output);

            textView.setTextColor(Color.BLACK);
            textView.setBackgroundColor(Color.LTGRAY);
            textView.setPadding(20, 20, 20, 20);
            textView.setTextSize(25);

            textView.setBackgroundResource(R.drawable.border);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            layoutParams.setMargins(10, 10, 10, 16);

            textView.setLayoutParams(layoutParams);

            textView.setOnClickListener(view -> {
                Snackbar.make(view, "Are you sure you want to delete this alarm?", Snackbar.LENGTH_LONG)
                        .setAction("Delete", v -> deleteAlarm(position))
                        .show();
            });

            linearLayout.addView(textView);
        }
    }

    //Adds new alarms and saves them to the alarm list
    private void addAlarm() {
        setContentView(R.layout.set_alarm);
        alarmTimePicker = findViewById(R.id.alarmTimePicker);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Button submit_button = findViewById(R.id.button_submit);
        submit_button.setOnClickListener(view -> {
            long time;
            String message;
            String alarmType = "default";
            Toast.makeText(MainActivity.this, "ALARM ADDED", Toast.LENGTH_SHORT).show();

            int hour = alarmTimePicker.getCurrentHour();
            int minute = alarmTimePicker.getCurrentMinute();

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, hour);
            alarmTime.set(Calendar.MINUTE, minute);
            alarmTime.set(Calendar.SECOND, 0);

            time = alarmTime.getTimeInMillis();

            // checks to make sure the day is correct
            if (System.currentTimeMillis() > time) {
                alarmTime.add(Calendar.DAY_OF_YEAR, 1);
                time = alarmTime.getTimeInMillis();
            }
            EditText messageText = findViewById(R.id.editText_alarmmessage);
            message = messageText.getText().toString().trim();
            RadioButton mathButton = findViewById(R.id.radioButton_math);
            RadioButton triviaButton = findViewById(R.id.radioButton_Trivia);
            if (mathButton.isChecked()) {
                alarmType = "Math";
            } else if (triviaButton.isChecked()) {
                alarmType = "Trivia";
            }


            //creates new intent for the alarm and saves the message and type as extras to pass through
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("message", message);
            intent.putExtra("alarmType", alarmType);
            Log.d("MainActivity", "Alarm Type: " + alarmType);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,alarmNumber, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmNumber++;
            pendingIntentList.add(pendingIntent);
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

            Alarm alarm = new Alarm(time, message, alarmType);
            alarmList.add(alarm);
            String alarmsJson = serializeAlarmList(alarmList);
            // Saves the alarms to SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("alarms", alarmsJson);
            editor.apply();
            loadAlarms();
        });
    }

    private void deleteAlarm(int position) {
        if (position >= 0 && position < alarmList.size()) {
            // Removes the alarm from the list
            Alarm deletedAlarm = alarmList.remove(position);

            // Cancels the PendingIntent
            if (position < pendingIntentList.size()) {
                PendingIntent canceledIntent = pendingIntentList.remove(position);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(canceledIntent);
            }

            // Serialize the new list
            String alarmsJson = serializeAlarmList(alarmList);

            SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("alarms", alarmsJson);
            editor.apply();

            // refresh the display
            loadAlarms();

            Toast.makeText(MainActivity.this, "Alarm Deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Invalid alarm position", Toast.LENGTH_SHORT).show();
        }
    }

    private String serializeAlarmList(List<Alarm> alarmList) {
        Gson gson = new Gson();
        return gson.toJson(alarmList);
    }

    private List<Alarm> deserializeAlarmList(String alarmsJson) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Alarm>>() {
        }.getType();
        return gson.fromJson(alarmsJson, type);
    }


    // Creates a math equation and when solved cancels the alarm
    private void openMathAlarmView() {
        setContentView(R.layout.math_puzzle);

        Random rand = new Random();

        int rand1 = rand.nextInt(100);
        int rand2 = rand.nextInt(100);
        int rand3 = rand.nextInt(100);
        int total = rand1 + rand2 + rand3;
        String totalString = String.valueOf(total);
        TextView textView = findViewById(R.id.textView_mathequation);

        textView.setText(rand1 + " + " + rand2 + " + " + rand3 + " =");

        Button submitmath_button = findViewById(R.id.button_submitmath);
        submitmath_button.setOnClickListener(view -> {
            EditText answer = findViewById(R.id.editTextNumber);
            String answerString = String.valueOf(answer.getText());
            if(answerString.equals(totalString)){
                ProcessPhoenix.triggerRebirth(getApplicationContext());
            }
            else{
                openMathAlarmView();
            }

        });

    }

    private void openTriviaAlarmView() {
        setContentView(R.layout.trivia_puzzle);
        Button submitTrivia_button = findViewById(R.id.button_submitTrivia);
        submitTrivia_button.setOnClickListener(view -> {
            ProcessPhoenix.triggerRebirth(getApplicationContext());
        });
    }
}