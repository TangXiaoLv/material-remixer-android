package com.google.android.apps.remixer.ui;

import com.google.android.apps.remixer.TransactionListActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatTextView;

public class TestActivity2 extends FragmentActivity {
  private AppCompatTextView textView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = manager.beginTransaction();
    fragmentTransaction.add(android.R.id.content, new Test2Fragment(), "Test2Fragment");
    fragmentTransaction.commit();

  }

  public static void start(TransactionListActivity activity) {
    Intent intent = new Intent(activity, TestActivity.class);
    activity.startActivity(intent);
  }
}
