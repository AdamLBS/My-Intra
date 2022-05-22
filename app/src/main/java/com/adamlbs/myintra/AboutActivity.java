package com.adamlbs.myintra;

import android.content.Context;

import androidx.annotation.NonNull;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;

public class AboutActivity extends MaterialAboutActivity {

    @Override
    @NonNull
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {

        MaterialAboutCard card = new MaterialAboutCard.Builder()
                // Configure card here
                .build();

        return new MaterialAboutList.Builder()
                .addCard(card)
                .build();
    }

    @Override
    protected CharSequence getActivityTitle() {
        return "About";
    }

}