package com.adamlbs.myintra.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import static java.lang.String.format;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adamlbs.myintra.MyRecyclerViewAdapter;
import com.adamlbs.myintra.R;
import com.adamlbs.myintra.databinding.FragmentDashboardBinding;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormat;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class DashboardFragment extends Fragment {
    OkHttpClient client = new OkHttpClient();
    private FragmentDashboardBinding binding;
    MyRecyclerViewAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
       // System.out.println(currentDate);
        SharedPreferences preferences = this.getActivity().getSharedPreferences("title", Context.MODE_PRIVATE);
        String autologin = preferences.getString("autologin", "");
        String test = autologin + "/planning/load?format=json&start=" + currentDate + "&end=" + currentDate;
        System.out.println(test);
        whenAsynchronousGetRequest_thenCorrect(autologin + "/planning/load?format=json&start=" + currentDate + "&end=" + currentDate, root);
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
            if (!my_obj.getJSONObject(j).getString("event_registered").equals("false") && my_obj.getJSONObject(j).getString("past").equals("false") ) {
                event_nb = event_nb + 1;
                Log.println(Log.INFO, "Module", my_obj.getJSONObject(j).getString("acti_title"));
                Log.println(Log.INFO, "Module", "fin :" + my_obj.getJSONObject(j).getString("end"));
                planning.add(my_obj.getJSONObject(j).getString("acti_title") + '\n' + get_time_left(my_obj.getJSONObject(j).getString("start")));
                update_view(root, planning,event_nb);
            }
          //  String val = my_obj.getJSONObject(j).getString("title");
            //val = parse_response(val);
            //String full_val = Html.fromHtml(val).toString();
            //animalNames.add(full_val);
        }

    }
    public String get_time_left(String time) {
        DateTime targetDateTime = DateTime.parse(format("%s", time), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        DateTime now = DateTime.now();
        Period period = new Period(now, targetDateTime);
        String value = "In " + period.getHours() + " hours and " + period.getMinutes() + " minutes";
        return value;
    }
    public void update_view(View root,List<String> animalNames, int event_nb)
    {
        Thread thread = new Thread(){
            public void run(){
                if(!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        TextView event = root.findViewById(R.id.events_nb);
                        event.setText(event_nb + " events planned");
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