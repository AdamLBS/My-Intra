package com.adamlbs.myintra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.adamlbs.myintra.databinding.ActivityHomeBinding;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.TimeUnit;

public class home extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(UploadWorker.class, 16, TimeUnit.MINUTES)
                        // Constraints
                        .build();
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(UploadWorker.class)
                        .build();

        WorkManager
                .getInstance(this)
                .enqueue(saveRequest);
        WorkManager
                .getInstance(this)
                .enqueue(uploadWorkRequest);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
    @Override
    public void onBackPressed() {

    }
}