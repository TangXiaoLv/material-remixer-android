/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 *
 */

package com.google.android.apps.remixer;

import com.google.android.apps.remixer.ui.TestActivity;
import com.google.android.libraries.remixer.Remixer;
import com.google.android.libraries.remixer.annotation.BooleanVariableMethod;
import com.google.android.libraries.remixer.annotation.ColorListVariableMethod;
import com.google.android.libraries.remixer.annotation.RangeVariableMethod;
import com.google.android.libraries.remixer.annotation.StringListVariableMethod;
import com.google.android.libraries.remixer.ui.gesture.Direction;
import com.google.android.libraries.remixer.ui.view.RemixerTargetBinder;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.NumberFormat;

public class TransactionListActivity extends AppCompatActivity {

  private static final String SPENT_LAST_MONTH = "Spent in the last month";
  private static final String SPENT_THIS_MONTH = "Spent this month";
  private Toolbar toolbar;
  private TransactionAdapter adapter;
  private RecyclerView list;
  private RemixerTargetBinder remixerTargetBinder;
  private CollapsingToolbarLayout collapsingToolbarLayout;
  private TextView total;
  private TextView timePeriodText;
  private FloatingActionButton fab;
  private FloatingActionButton fab_l;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_transaction_list);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    list = (RecyclerView) findViewById(R.id.transactionList);
    adapter = new TransactionAdapter();
    list.setAdapter(adapter);
    collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
    total = (TextView) findViewById(R.id.totalTransactions);
    timePeriodText = (TextView) findViewById(R.id.timePeriod);
    total.setText(NumberFormat.getCurrencyInstance().format(adapter.getThisMonthTotal()));
    setSupportActionBar(toolbar);
    fab = (FloatingActionButton) findViewById(R.id.fab);

    RemixerTargetBinder.bind(this);

    fab_l = (FloatingActionButton) findViewById(R.id.fab_l);
    fab_l.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //RemixerBinder.bind(TransactionListActivity.this);
        TestActivity.start(TransactionListActivity.this);
      }
    });
    fab_l.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        String path = "/data/data/" + getPackageName() + "/remixer_local_storage.xml";
        try {
          Remixer.getInstance().updateLocalThemeFile(new FileInputStream(path));
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
        return true;
      }
    });
  }

  @ColorListVariableMethod(
      global = "Primary",
      limitedToValues = {0xFF216EB3, 0xFF191919}
  )
  void setHeaderBackgroundColor(Integer color) {
    toolbar.setBackgroundColor(color);
    collapsingToolbarLayout.setContentScrimColor(color);
    collapsingToolbarLayout.setBackgroundColor(color);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(color);
      collapsingToolbarLayout.setStatusBarScrimColor(color);
    }
  }

  @ColorListVariableMethod(
      global = "Primary",
      limitedToValues = {0xffff0000, 0xff00ff00},
      title = "设置fab参数"
  )
  void setFab(Integer color) {
    fab.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
  }

  @BooleanVariableMethod(initialValue = true)
  void showIcons(Boolean showIcons) {
    adapter.setIconsVisible(showIcons);
    LinearLayoutManager layoutManager = (LinearLayoutManager) list.getLayoutManager();
    int first = layoutManager.findFirstVisibleItemPosition();
    int last = layoutManager.findLastVisibleItemPosition();
    adapter.notifyItemRangeChanged(first, last - first + 1);
  }

  @StringListVariableMethod(
      limitedToValues = {SPENT_THIS_MONTH, SPENT_LAST_MONTH}
  )
  void setTimePeriod(String calculation) {
    timePeriodText.setText(calculation);
    float spent =
        calculation.equals(SPENT_LAST_MONTH)
            ? adapter.getLastMonthTotal()
            : adapter.getThisMonthTotal();
    total.setText(NumberFormat.getCurrencyInstance().format(spent));
  }

  @RangeVariableMethod(
      minValue = 14,
      maxValue = 80,
      increment = 0.5f,
      initialValue = 48
  )
  void setTotalTextSize(Float size) {
    total.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }
}
