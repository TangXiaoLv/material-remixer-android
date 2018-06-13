package com.google.android.apps.remixer.ui;

import com.google.android.libraries.remixer.annotation.ColorListVariableMethod;
import com.google.android.libraries.remixer.ui.LayoutHelper;
import com.google.android.libraries.remixer.ui.view.RemixerTargetBinder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class Test2Fragment extends Fragment {
    private AppCompatTextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      LinearLayout frameLayout = new LinearLayout(container.getContext());
      frameLayout.setOrientation(LinearLayout.VERTICAL);

      textView = new AppCompatTextView(container.getContext());
      textView.setText("test 2");
      textView.setGravity(Gravity.CENTER);
      frameLayout.addView(textView, new LinearLayout.LayoutParams(-1, -2));

      AppCompatButton button = new AppCompatButton(getContext());
      button.setTextSize(18);
      button.setText("配置台");
      //frameLayout.addView(button, LayoutHelper.createLinear(-1, -2));

      RemixerTargetBinder.bind(this);
      return frameLayout;
    }


    @ColorListVariableMethod(
        global = "Primary",
        limitedToValues = {0xffff0000, 0xff0000ff},
        title = "设置test2字体颜色"
    )
    void setText2(Integer color) {
      textView.setTextColor(color);
    }
  }
