package vn.gcall.gcall2;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.paolorotolo.appintro.AppIntro;


import vn.gcall.gcall2.AppIntroduction.SampleSlide;

/**
 * Created by This PC on 28/07/2016.
 * Show application manual slides
 */
public class HelpActivity extends AppIntro {
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        addSlide(SampleSlide.newInstance(R.layout.help_fragment1));
        addSlide(SampleSlide.newInstance(R.layout.help_fragment2));
        addSlide(SampleSlide.newInstance(R.layout.help_fragment3));
        addSlide(SampleSlide.newInstance(R.layout.help_fragment4));
        addSlide(SampleSlide.newInstance(R.layout.help_fragment5));


        showStatusBar(false);
        setDepthAnimation();
    }

    @Override
    public void onSkipPressed() {
        onBackPressed();
        finish();
    }

    @Override
    public void onDonePressed() {
        onBackPressed();
        finish();
    }
}

