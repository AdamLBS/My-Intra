package com.adamlbs.myintra.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.adamlbs.myintra.AboutActivity;
import com.adamlbs.myintra.LoginActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

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
  //      homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}