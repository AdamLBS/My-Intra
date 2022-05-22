package com.adamlbs.myintra;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    String autologin;
    String address;
    String full_value;
    String my_response = null;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageButton login = findViewById(R.id.login_button);
        EditText myAutoLogin = findViewById(R.id.autologinlink);
        EditText myAddress = findViewById(R.id.adress);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                address = myAddress.getText().toString().toLowerCase();
                address.replaceAll("\\s+$", "");
                autologin = myAutoLogin.getText().toString();
                System.out.println(address);
                System.out.println(autologin);
                full_value = autologin;
                try {
                    check_value(view);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void print_toast(String val) {
        Thread thread = new Thread(){
            public void run(){
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LoginActivity.this, val, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
        thread.start();
    }

    public void check_value(View view) throws IOException, JSONException {
        full_value = autologin + "/user/" + address + "/?format=json";
        whenAsynchronousGetRequest_thenCorrect();
        JSONObject jObject;
     /*   if (isValidUrl(full_value))
        try {
            jObject = new JSONObject(my_response);
            JSONArray jArray = jObject.getJSONArray("gpa");
            JSONObject jObj = jArray.getJSONObject(0);
            String gpa = jObj.getString("gpa");
            System.out.println(gpa);
            Context context = getApplicationContext();
            CharSequence text = "Connection successful!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            set_values(jObject, gpa, full_value, autologin, address);

        } catch (JSONException e) {
            print_toast("Error");
            e.printStackTrace();
        }*/
    }

    public void compute_val()
    {
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(my_response);
            JSONArray jArray = jObject.getJSONArray("gpa");
            JSONObject jObj = jArray.getJSONObject(0);
            String gpa = jObj.getString("gpa");
            System.out.println(gpa);
            print_toast("Connection successful");
            set_values(jObject, gpa, full_value, autologin, address);
            startActivity(new Intent(LoginActivity.this, home.class));
        } catch (JSONException e) {
            print_toast("Error");
            e.printStackTrace();
        }
    }

    public void set_log_time(JSONObject MyObj)
    {
        try {
            JSONObject jObj2 = MyObj.getJSONObject("nsstat");
            SharedPreferences.Editor editor = getSharedPreferences("title", MODE_PRIVATE).edit();
            String log_time = jObj2.getString("active");
            float active_time = jObj2.getInt("active");
            float idle_time = jObj2.getInt("idle");
            editor.putString("log_time", log_time);
            editor.putFloat("active_time", active_time);
            editor.putFloat("idle_time", idle_time);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    public void set_values(JSONObject MyObj, String gpa, String full_value, String autologin, String address) throws JSONException {
        String title = MyObj.getString("title");
        String semester_code = MyObj.getString("semester_code");
        String credits = MyObj.getString("credits");
        String promo = MyObj.getString("promo");
        JSONArray jArray = MyObj.getJSONArray("groups");
        set_log_time(MyObj);
        JSONObject jObj = jArray.getJSONObject(0);
        String city = jObj.getString("title");
        System.out.println(city);
        SharedPreferences.Editor editor = getSharedPreferences("title", MODE_PRIVATE).edit();
        editor.putString("title", title);
        editor.putString("city", city);
        editor.putString("promo", promo);
        editor.putString("gpa", gpa);
        editor.putString("credits", credits);
        editor.putString("semester_code", semester_code);
        editor.putString("full_value", full_value);
        editor.putString("autologin", autologin);
        editor.putString("address", address);
        editor.apply();
        System.out.println(title);
        System.out.println(promo);

    }

    String run(String url) throws IOException {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();

        }
    }
    private boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url.toLowerCase());
        return m.matches();
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Planning Notification";
            String description = "Notification for events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Planning", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void whenAsynchronousGetRequest_thenCorrect() {
        if (isValidUrl(full_value)) {
            Request request = new Request.Builder()
                    .url(full_value)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        print_toast("Error");
                    } else {
                        my_response = response.body().string();
                        Log.println(Log.INFO, "resp", my_response);
                        System.out.println(my_response);
                        compute_val();
                    }
                }
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    print_toast("error");
                }
            });
        }
    }

}