package com.adamlbs.myintra;

import static android.content.Context.NOTIFICATION_SERVICE;
import static java.lang.String.format;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class UploadWorker extends Worker {
    OkHttpClient client = new OkHttpClient();
    public UploadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images
        //make_notification("Quiz 2", "30");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("title", Context.MODE_PRIVATE);
        String autologin = preferences.getString("autologin", "");
        String test = autologin + "/planning/load?format=json&start=" + currentDate + "&end=" + currentDate;
        System.out.println(test);
        whenAsynchronousGetRequest_thenCorrect(autologin + "/planning/load?format=json&start=" + currentDate + "&end=" + currentDate);
        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }
    public void whenAsynchronousGetRequest_thenCorrect(String url) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray my_obj = new JSONArray(response.body().string());
                        parse_response(my_obj);
                    } catch (JSONException e) {
                        System.out.println("err");
                        e.printStackTrace();
                    }
                } else
                    System.out.println("err");
            }

            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }
        });
    }
    public void parse_response(JSONArray my_obj) throws JSONException {
        int j = 0;
        int event_nb = 0;
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("title", Context.MODE_PRIVATE);
        System.out.print(my_obj.length());
        SharedPreferences.Editor edit_pref = preferences.edit();
        for (int i = my_obj.length(); j != i; j++) {
            boolean is_viewewd =  preferences.getBoolean(my_obj.getJSONObject(j).getString("codeevent"), false);
            if (!my_obj.getJSONObject(j).getString("event_registered").equals("false") && my_obj.getJSONObject(j).getString("past").equals("false") && get_time_left_e(my_obj.getJSONObject(j).getString("start")) <= 15 && !is_viewewd ) {
                event_nb = event_nb + 1;
                preferences.getBoolean(my_obj.getJSONObject(j).getString("codeevent"), false);
                make_notification(my_obj.getJSONObject(j).getString("acti_title"), time_left(my_obj.getJSONObject(j).getString("acti_title"), my_obj.getJSONObject(j).getString("start")), my_obj.getJSONObject(j).getString("codeevent"));
                edit_pref.putBoolean(my_obj.getJSONObject(j).getString("codeevent"), true);
            }
        }
        edit_pref.apply();
    }

        public String time_left(String acti_title, String time)
    {
        DateTime targetDateTime = DateTime.parse(format("%s", time), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        DateTime now = DateTime.now();
        Period period = new Period(now, targetDateTime);
        String value = acti_title + " starts in " + period.getMinutes() + " minutes.";
        System.out.println(value);
        return value;
    }

    public int get_time_left_e(String time)
    {
        DateTime targetDateTime = DateTime.parse(format("%s", time), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        DateTime now = DateTime.now();
        Period period = new Period(now, targetDateTime);
        if (period.getHours() == 0) {
            int minutes = period.getMinutes();
            System.out.println(minutes);
            return minutes;
        }
        return 31;
    }

    public void make_notification(String acti_name, String time_left, String event_ID) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("title", Context.MODE_PRIVATE);
        NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService((NOTIFICATION_SERVICE));
        createNotificationChannel(manager);
        String channel_ID = "Planning";
        CharSequence name = "Planning Notification";
        String description = "Notification for events";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("Planning", name, importance);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_ID)
                .setSmallIcon(R.drawable.ic_notif_logo)
                .setContentTitle(acti_name + " starts soon")
                .setContentText(time_left)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        manager.notify(4, builder.build());
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Planning Notification";
            String description = "Notification for events";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Planning", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
    }

}