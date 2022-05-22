package com.adamlbs.myintra.ui.profil;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.adamlbs.myintra.R;
import com.adamlbs.myintra.databinding.FragmentProfileBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.io.InputStream;
import java.util.ArrayList;

public class ProfilFragment extends Fragment {
    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SharedPreferences preferences = this.requireActivity().getSharedPreferences("title", Context.MODE_PRIVATE);

        new DownloadImageTask((ImageView) root.findViewById(R.id.imageprofil))
                .execute(preferences.getString("autologin", "") + "/file/userprofil/profilview/" + preferences.getString("address", "") + ".jpg");
        final TextView textView = root.findViewById(R.id.my_name);
        final TextView city = root.findViewById(R.id.city);
        final TextView credits = root.findViewById(R.id.credits_value);
        final TextView gpa = root.findViewById(R.id.gpa_value);
        final TextView semester = root.findViewById(R.id.semester_value);
        final TextView log_time = root.findViewById(R.id.log_time_val);
        String channel = (preferences.getString("title", ""));
        String city_name = "City : " + (preferences.getString("city","").toUpperCase() + '\n' + "Promo : " +  (preferences.getString("promo","").toUpperCase()));
        String credits_value = (preferences.getString("credits", ""));
        String gpa_value = (preferences.getString("gpa", ""));
        credits.setText(credits_value);
        textView.setText(channel);
        city.setText(city_name);
        gpa.setText(gpa_value);
        semester.setText(preferences.getString("semester_code",""));
        log_time.setText(preferences.getString("log_time","0.0"));
        checkStatus();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    private void checkStatus() {
        View root = binding.getRoot();
        PieChart pieChart = (PieChart) root.findViewById(R.id.chart);
        PieDataSet pieDataSet = new PieDataSet(getData(),"");
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
        pieDataSet.setColors(Color.rgb(0, 255, 0), Color.rgb(128, 128, 128));
        PieData pieData = new PieData(pieDataSet);
        Description description = pieChart.getDescription();
        description.setEnabled(false);
        pieData.setValueTextSize(13f);
        pieChart.setDrawHoleEnabled(true);
        pieData.setValueTextColor(Color.WHITE);
        pieChart.setData(pieData);
        pieChart.animateXY(2000, 2000);
    }
    private ArrayList<PieEntry> getData(){
        SharedPreferences preferences = this.requireActivity().getSharedPreferences("title", Context.MODE_PRIVATE);
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(preferences.getFloat("active_time",0)));
        entries.add(new PieEntry(preferences.getFloat("idle_time",0)));
        return entries;
    }

}