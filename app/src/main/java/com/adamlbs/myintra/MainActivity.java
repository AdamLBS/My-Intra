package com.adamlbs.myintra;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        check_login();


    }

    public void build_app(){
        String first = "A simple <font color='#7293E1'>App</font> for <font color='#7293E1'>Epitech's</font> students.<br/><br/>For <font color='#7293E1'> alerts</font>, <font color='#7293E1'> schedule</font> and <font color='#7293E1'>projects</font>.";
        setContentView(R.layout.activity_main);
        TextView text = (TextView) findViewById(R.id.text_feat);
        text.setText(Html.fromHtml(first));
        FloatingActionButton arrow = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        SharedPreferences preferences = this.getSharedPreferences("title", Context.MODE_PRIVATE);

        if (preferences.getInt("run", 0) != 0)
            print_toast("Error");
    }
    public void check_login() {
        SharedPreferences preferences = this.getSharedPreferences("title", Context.MODE_PRIVATE);
           String full_value = preferences.getString("full_value", "none");
        System.out.println(full_value);
        RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, full_value,
                    response -> {
                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONArray jArray = jObject.getJSONArray("gpa");
                            JSONObject jObj = jArray.getJSONObject(0);
                            String gpa = jObj.getString("gpa");
                            System.out.println(gpa);
                            Context context = getApplicationContext();
                            CharSequence text = "Connection successful!";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                            set_values(jObject, gpa, full_value, preferences.getString("autologin", ""), preferences.getString("address", ""));
                            startActivity(new Intent(MainActivity.this, home.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> build_app());
            queue.add(stringRequest);
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
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    public void set_values(JSONObject MyObj, String gpa, String full_value, String autologin, String address) throws JSONException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String title = MyObj.getString("title");
        String semester_code = MyObj.getString("semester_code");
        String credits = MyObj.getString("credits");
        String promo = MyObj.getString("promo");
        JSONArray jArray = MyObj.getJSONArray("groups");
        JSONObject jObj = jArray.getJSONObject(0);
        String city = jObj.getString("title");
        System.out.println(city);
        set_log_time(MyObj);
        SharedPreferences.Editor editor = getSharedPreferences("title", MODE_PRIVATE).edit();
        editor.putString("title", title);
        editor.putString("city", city);
        editor.putString("promo", promo);
        editor.putString("gpa", gpa);
        editor.putString("credits", credits);
        editor.putString("semester_code",semester_code);
        editor.putString("full_value", full_value);
        editor.putString("autologin", autologin);
        editor.putString("address", address);
        editor.putInt("run", 1);
        editor.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
        editor.putInt("month", calendar.get(Calendar.MONTH));
        editor.putInt("year", calendar.get(Calendar.YEAR));
        editor.apply();
        System.out.println(title);
        System.out.println(promo);
    }
    public void print_toast(String val) {
        Context context = getApplicationContext();
        CharSequence text = val;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}