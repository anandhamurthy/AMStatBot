package com.amstatbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.amstatbot.Adapters.WelcomeAdapter;
import com.amstatbot.Login.SplashActivity;
import com.amstatbot.Models.Welcome;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager Pager;
    private WelcomeAdapter welcomeAdapter;
    private TabLayout Indicator;
    private Button GetStarted;
    Animation Animation ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (restorePrefData()) {

            Intent mainActivity = new Intent(getApplicationContext(), SplashActivity.class );
            startActivity(mainActivity);
            finish();

        }

        setContentView(R.layout.activity_welcome);

        GetStarted = findViewById(R.id.welcome_get_started);
        Indicator = findViewById(R.id.welcome_indicator);
        Animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation);

        final List<Welcome> mList = new ArrayList<>();
        mList.add(new Welcome("Ask AMStatBot","Ask any Company's Stock with AMStatBot.",R.drawable.bot));
        mList.add(new Welcome("Swipe For Graph","Swipe the Chat Message to get Live Graph of Company.",R.drawable.swipe));
        mList.add(new Welcome("Stock News","Ask updated Stock NEWS with AMStatBot.", R.drawable.news));

        Pager =findViewById(R.id.welcome_viewpager);
        welcomeAdapter = new WelcomeAdapter(this, mList);
        Pager.setAdapter(welcomeAdapter);

        Indicator.setupWithViewPager(Pager);

        Indicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == mList.size()-1) {
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        GetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(getApplicationContext(), SplashActivity.class);
                startActivity(mainActivity);
                savePrefsData();
                finish();
            }
        });

    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        Boolean isIntroActivityOpnendBefore = pref.getBoolean("isIntroOpnend",false);
        return  isIntroActivityOpnendBefore;
    }

    private void savePrefsData() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpnend",true);
        editor.commit();


    }
    private void loadLastScreen() {

        GetStarted.setVisibility(View.VISIBLE);
        Indicator.setVisibility(View.INVISIBLE);
        GetStarted.setAnimation(Animation);
    }
}