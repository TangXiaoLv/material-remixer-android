package com.google.android.apps.remixer.ui;

import com.google.android.apps.remixer.TransactionListActivity;
import com.google.android.libraries.remixer.Remixer;
import com.google.android.libraries.remixer.annotation.ColorListVariableMethod;
import com.google.android.libraries.remixer.annotation.RemixerBinder;
import com.google.android.libraries.remixer.ui.view.RemixerFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class TestActivity extends FragmentActivity {

  private AppCompatTextView textView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FrameLayout frameLayout = new FrameLayout(this);
    setContentView(frameLayout);

    textView = new AppCompatTextView(this);

    textView.setText("test 1 点击跳转");
    textView.setGravity(Gravity.CENTER);
    frameLayout.addView(textView, new FrameLayout.LayoutParams(-1, -2));

    RemixerBinder.bind(this);

    RemixerFragment.newInstance().attachToShake(this, 20.0);

    textView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), TestActivity2.class);
        startActivity(intent);
      }
    });
  }

  @ColorListVariableMethod(
      global = "Primary",
      limitedToValues = {0xff00ff00, 0xff0000ff},
      title = "设置test1字体颜色"
  )
  void setText(Integer color) {
    textView.setTextColor(color);
  }

  @Override
  protected void onResume() {
    super.onResume();
    RemixerFragment remixerFragment = RemixerFragment.newInstance();
    remixerFragment.attachToShake(this, 20.0);
  }

  @Override
  protected void onPause() {
    super.onPause();
    Remixer.getInstance().onActivityDestroyed(this);
  }

  public static void start(TransactionListActivity activity) {
    Intent intent = new Intent(activity, TestActivity.class);
    activity.startActivity(intent);
  }
}
