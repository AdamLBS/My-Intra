package com.adamlbs.myintra.ui.home;

import static android.content.Context.MODE_PRIVATE;

import static java.lang.String.format;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adamlbs.myintra.AboutActivity;
import com.adamlbs.myintra.LoginActivity;
import com.adamlbs.myintra.MyRecyclerViewAdapter;
import com.adamlbs.myintra.R;
import com.adamlbs.myintra.databinding.FragmentHomeBinding;
import com.adamlbs.myintra.home;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsActivity;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class HomeFragment extends Fragment {
    OkHttpClient client = new OkHttpClient();
    MyRecyclerViewAdapter adapter;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

       final TextView textView = binding.name;
       final TextView alertnb = root.findViewById(R.id.alerts_nb);
        SharedPreferences preferences = this.getActivity().getSharedPreferences("title", Context.MODE_PRIVATE);
        String channel = (preferences.getString("title", ""));
       textView.setText(channel);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, preferences.getString("autologin","") + "/user/" + preferences.getString("address", "") + "/notification/message/?format=json",
                response -> {
                    JSONArray jObject = null;
                    try {
                        jObject = new JSONArray(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int value = jObject.length();
                    String nbOfAlerts = String.valueOf(value);
                    System.out.println(nbOfAlerts);
                    alertnb.setText(nbOfAlerts + " alerts");
                }, error -> System.out.println("err"));
        queue.add(stringRequest);
        String projectRequest = preferences.getString("autologin","") +  "/module/board/?format=json&start=" + currentDate + "&end=" + currentDate;
        Log.println(Log.DEBUG, "DEV", projectRequest);
  //      homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        whenAsynchronousGetRequest_thenCorrect(projectRequest, root);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void whenAsynchronousGetRequest_thenCorrect(String url, View root) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray my_obj = new JSONArray(response.body().string());
                        parse_response(my_obj, root);
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
    public void parse_response(JSONArray my_obj, View root) throws JSONException {
        int j = 0;
        int event_nb = 0;
        List<String> planning = new ArrayList<>();
        for (int i = my_obj.length(); j != i; j++) {
            if (!my_obj.getJSONObject(j).getString("registered").equals("0")) {
                event_nb = event_nb + 1;
                Log.println(Log.INFO, "Module", my_obj.getJSONObject(j).getString("acti_title"));
                DateTime targetDateTime = DateTime.parse(format("%s", my_obj.getJSONObject(j).getString("end_acti")), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
                DateTime now = DateTime.now();
                Period period = new Period(now, targetDateTime);
                PeriodFormatter daysHoursMinutes = new PeriodFormatterBuilder()
                        .appendMonths()
                        .appendSuffix(" month", " months")
                        .appendSeparator(", ")
                        .appendDays()
                        .appendSuffix(" day", " days")
                        .appendSeparator(" and ")
                        .appendMinutes()
                        .appendSuffix(" minute", " minutes")
                        .toFormatter();
                Log.println(Log.DEBUG, "Dev", my_obj.getJSONObject(j).getString("acti_title") + "\n Ends in " + daysHoursMinutes.print(period));
                Log.println(Log.INFO, "Module", "fin :" + my_obj.getJSONObject(j).getString("end_acti"));
                planning.add(my_obj.getJSONObject(j).getString("acti_title") + "\n Ends in " + daysHoursMinutes.print(period));
                update_view(root, planning, event_nb);
            }
        }

    }
    public void update_view(View root, List<String> animalNames, int event_nb)
    {
        Thread thread = new Thread(){
            public void run(){
                if(!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        adapter = new MyRecyclerViewAdapter(getActivity(), animalNames);
                        recyclerView.setAdapter(adapter);                    }
                });
            }
        };
        thread.start();
    }
}