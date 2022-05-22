package com.adamlbs.myintra.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
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

import com.adamlbs.myintra.LoginActivity;
import com.adamlbs.myintra.MyRecyclerViewAdapter;
import com.adamlbs.myintra.R;
import com.adamlbs.myintra.databinding.FragmentNotificationsBinding;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class NotificationsFragment extends Fragment {
    OkHttpClient client = new OkHttpClient();
    private FragmentNotificationsBinding binding;
    MyRecyclerViewAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // data to populate the RecyclerView with
        SharedPreferences preferences = this.getActivity().getSharedPreferences("title", Context.MODE_PRIVATE);
        set_alerts_nb(preferences, root);
        List<String> animalNames = new ArrayList<>();
        String autologin = preferences.getString("autologin", "");
        System.out.println(autologin);
        whenAsynchronousGetRequest_thenCorrect( autologin + "/user/notification/message?format=json", animalNames, root);
        // set up the RecyclerView
        return root;
    }

    public String parse_response(String test)
    {
        StringBuffer buf = new StringBuffer();
        Matcher m = Pattern.compile("\\\\u([0-9A-Fa-f]{4})").matcher(test);
        while (m.find()) {
            try {
                int cp = Integer.parseInt(Objects.requireNonNull(m.group(1)), 16);
                m.appendReplacement(buf, "");
                buf.appendCodePoint(cp);
            } catch (NumberFormatException ignored) {
            }
        }
        m.appendTail(buf);
        test = buf.toString();
        return test;
    }
    public void whenAsynchronousGetRequest_thenCorrect(String url, List<String> animalNames, View root) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray my_obj = new JSONArray(response.body().string());
                        parse_response(my_obj, animalNames, root);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }
        });
    }
    public void parse_response(JSONArray my_obj, List<String> animalNames, View root) throws JSONException {
        int j = 0;
        for (int i = my_obj.length(); j != i; j++) {
            String val = my_obj.getJSONObject(j).getString("title");
            val = parse_response(val);
            String full_val = Html.fromHtml(val).toString();
            animalNames.add(full_val);
        }
        update_view(root, animalNames);
        Log.println(Log.INFO, "resp", my_obj.getJSONObject(0).toString());

    }

    public void update_view(View root,List<String> animalNames)
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

    public void set_alerts_nb(SharedPreferences preferences, View root) {
        TextView alertnb = root.findViewById(R.id.alerts_nb);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
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
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}