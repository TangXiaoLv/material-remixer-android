package com.google.android.libraries.remixer.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class CustomDialog extends AppCompatDialog {

  private String header;
  private DialogInputListener listener;
  private final FrameLayout content;
  private final AppCompatTextView title;

  public CustomDialog(Context context) {
    super(context, R.style.TransparentDialog);

    //初始化
    content = new FrameLayout(context);
    GradientDrawable drawable = new GradientDrawable();
    drawable.setCornerRadius(AndroidUtils.dp(8));
    drawable.setColor(0xffffffff);
    content.setBackground(drawable);

    title = new AppCompatTextView(context);
    title.setTextColor(0xff4A4A4A);
    title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
    title.setGravity(Gravity.CENTER);
    content.addView(title, LayoutHelper.createFrame(212, LayoutHelper.WRAP_CONTENT,
        Gravity.CENTER_HORIZONTAL, 0, 24, 0, 0));

    final AppCompatEditText input = new AppCompatEditText(context);
    input.setHintTextColor(0xff8E8E93);
    input.setSingleLine(true);
    input.setTextColor(0xff222222);
    input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
    drawable = new GradientDrawable();
    drawable.setCornerRadius(AndroidUtils.dp(2));
    drawable.setColor(0xfff3f5f9);
    input.setBackground(drawable);
    content.addView(input, LayoutHelper.createFrame(222, -2,
        Gravity.CENTER_HORIZONTAL, 0, 68, 0, 0));

    final AppCompatTextView cancel = new AppCompatTextView(context);
    cancel.setText("取消");
    cancel.setTextColor(0xff4A4A4A);
    cancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
    cancel.setGravity(Gravity.CENTER);
    content.addView(cancel, LayoutHelper.createFrame(135, 50, Gravity.BOTTOM));
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cancel();
      }
    });

    AppCompatTextView confirm = new AppCompatTextView(context);
    confirm.setText("确定");
    confirm.setTextColor(0xffff3b2f);
    confirm.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
    confirm.setGravity(Gravity.CENTER);
    content.addView(confirm, LayoutHelper.createFrame(135, 50, Gravity.BOTTOM | Gravity.END));
    confirm.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (listener != null) {
          listener.onInputChange(input.getText().toString());
        }
        cancel();
      }
    });

    //水平线
    View line = new View(getContext());
    line.setBackgroundColor(0xffD6D6D6);
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, 1);
    params.setMargins(0, AndroidUtils.dp(120), 0, 0);
    content.addView(line, params);

    //垂直线
    line = new View(getContext());
    line.setBackgroundColor(0xffD6D6D6);
    params = new FrameLayout.LayoutParams(1, -1);
    params.setMargins(0, AndroidUtils.dp(120), 0, 0);
    params.gravity = Gravity.CENTER_HORIZONTAL;
    content.addView(line, params);

    //设置view
    setContentView(content, LayoutHelper.createFrame(270, 174));
    setCancelable(true);
    setCanceledOnTouchOutside(true);
  }

  public void setTitle(String title) {
    this.header = title;
  }

  public void setOnDialogInputListener(DialogInputListener listener) {
    this.listener = listener;
  }

  public void show() {
    title.setText(header);
    super.show();
  }

  public interface DialogInputListener {
    void onInputChange(String input);
  }
}
