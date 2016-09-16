package vn.gcall.gcall2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.paolorotolo.appintro.AppIntro;

import vn.gcall.gcall2.AppIntroduction.SampleSlide;

public class Intro extends AppIntro {

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        addSlide(SampleSlide.newInstance(R.layout.intro_fragment1));
        addSlide(SampleSlide.newInstance(R.layout.intro_fragment2));
        addSlide(SampleSlide.newInstance(R.layout.intro_fragment3));
        addSlide(SampleSlide.newInstance(R.layout.intro_fragment4));


        showStatusBar(false);
        setDepthAnimation();
    }

    @Override
    public void onSkipPressed() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}
