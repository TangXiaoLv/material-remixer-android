/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.remixer.ui.view;

import com.google.android.libraries.remixer.GlobalType;
import com.google.android.libraries.remixer.Remixer;
import com.google.android.libraries.remixer.RemixerUtils;
import com.google.android.libraries.remixer.Variable;
import com.google.android.libraries.remixer.ui.AndroidUtils;
import com.google.android.libraries.remixer.ui.LayoutHelper;
import com.google.android.libraries.remixer.ui.R;
import com.google.android.libraries.remixer.ui.gesture.Direction;
import com.google.android.libraries.remixer.ui.gesture.GestureListener;
import com.google.android.libraries.remixer.ui.gesture.ShakeListener;

import android.content.Intent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that shows all Remixes for the current activity. It's very easy to use:
 *
 * <pre><code>
 * class MyActivity extends FragmentActivity {
 *   // ...
 *
 *   protected void onCreate(Bundle savedInstanceState) {
 *     // ...
 *     RemixerFragment remixerFragment = RemixerFragment.newInstance();
 *     // Attach it to a button.
 *     remixerFragment.attachToButton(this, button);
 *     // Have remixer show up on 3 finger swipe up.
 *     remixerFragment.attachToGesture(this, Direction.UP, 3);
 *   }
 * }
 * </code></pre>
 */
public class RemixerFragment
    extends BottomSheetDialogFragment {

  public static final String REMIXER_TAG = "Remixer";
  // 195ms is a good time for elements leaving the screen.
  // https://material.io/guidelines/motion/duration-easing.html#duration-easing-common-durations
  protected static final int COLLAPSE_DRAWER_DURATION = 195;

  // 225ms is a good time for elements entering the screen.
  // https://material.io/guidelines/motion/duration-easing.html#duration-easing-common-durations
  protected static final int EXPAND_DRAWER_DURATION = 225;

  private Remixer remixer;
  private ShakeListener shakeListener;
  private ImageView expandSharingOptionsButton;
  private Button sharedStatusButton;
  private boolean isShowingDrawer = false;
  private RemixerShareDrawer shareDrawer;
  private LinearLayout tags;
  private FrameLayout lists;
  private File shareFile;

  public RemixerFragment() {
    remixer = Remixer.getInstance();
  }

  public static RemixerFragment newInstance() {
    return new RemixerFragment();
  }

  private boolean isAddingFragment = false;
  private final Object syncLock = new Object();

  private SensorEventListener sensorEventListener;

  /**
   * Attach this instance to {@code button}'s OnClick, so that clicking the button shows this
   * fragment.
   *
   * <p><b>Notice this will replace the button's OnClickListener</b>
   */
  public void attachToButton(final FragmentActivity activity, Button button) {
    button.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View view) {
        showRemixer(activity.getSupportFragmentManager(), REMIXER_TAG);
      }
    });
  }

  /**
   * @return whether the fragment was shown or not.
   */

  public void showRemixer(FragmentManager manager, String tag) {
    synchronized (syncLock) {
      if (!isAddingFragment && !isAdded()) {
        isAddingFragment = true;
        show(manager, tag);
      }
    }
  }

  // TODO(nicksahler): Generalize to attaching to any SensorEventListener

  /**
   * Attach this instance to a shake gesture and show fragment when magnitude exceeds {@code
   * threshold}
   */
  public void attachToShake(final FragmentActivity activity, final double threshold) {
    shakeListener = new ShakeListener(activity, threshold, this);
    shakeListener.attach();
  }

  /**
   * Detach from a shake gesture
   */
  public void detachFromShake() {
    shakeListener.detach();
    shakeListener = null;
  }

  /**
   * Attach this instance to a swipe gesture with {@code numberOfFingers} numbers in direction
   * {@code direction} on the {@code activity}, so that performing the gesture will show this
   * fragment.
   *
   * <p><b>Notice this will replace the activity's root view's OnTouchListener</b>
   */
  public void attachToGesture(
      FragmentActivity activity, Direction direction, int numberOfFingers) {
    GestureListener.attach(activity, direction, numberOfFingers, this);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_remixer_list, container, false);
    ImageView closeButton = (ImageView) view.findViewById(R.id.closeButton);
    shareDrawer = (RemixerShareDrawer) view.findViewById(R.id.shareDrawer);
    isShowingDrawer = false;
    ShareDrawerOnClickListener listener = new ShareDrawerOnClickListener();
    expandSharingOptionsButton = (ImageView) view.findViewById(R.id.expandSharingOptionsButton);
    sharedStatusButton = (Button) view.findViewById(R.id.sharedStatusButton);
    expandSharingOptionsButton.setOnClickListener(listener);
    sharedStatusButton.setOnClickListener(listener);
    closeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        RemixerFragment.this.getFragmentManager().beginTransaction().remove(RemixerFragment.this).commit();
      }
    });

    tags = view.findViewById(R.id.listTags);
    createTag("定制", "1");
    createTag("全局颜色", "2");

    List<Variable> defVariable = new ArrayList<>();
    List<Variable> colorsVariable = new ArrayList<>();
    ArrayList<List<Variable>> vLists = remixer.getAllVariables();
    for (List<Variable> list : vLists) {
      for (Variable v : list) {
        if (v.getKey().startsWith(GlobalType.GLOBAL_COLOR)) {
          colorsVariable.add(v);
        } else {
          defVariable.add(v);
        }
      }
    }

    lists = view.findViewById(R.id.listContainer);
    createList("1", defVariable);
    createList("2", colorsVariable);

    setTagsListener();
    return view;
  }

  private void setTagsListener() {
    final int count = tags.getChildCount();
    for (int i = 0; i < count; i++) {
      final View view = tags.getChildAt(i);
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          for (int i = 0; i < count; i++) {
            tags.getChildAt(i).setBackgroundColor(0xffffffff);
          }
          v.setBackgroundColor(getResources().getColor(R.color.variableListBackground));

          String tag = (String) v.getTag();
          int count = lists.getChildCount();
          for (int i = 0; i < count; i++) {
            View child = lists.getChildAt(i);
            if (tag.equals(child.getTag())) {
              child.setVisibility(View.VISIBLE);
            } else {
              child.setVisibility(View.GONE);
            }
          }
        }
      });
    }
    tags.getChildAt(0).performClick();
  }

  private void createTag(String title, String tag) {
    TextView head = new TextView(getContext());
    head.setText(title);
    head.setTextSize(14);
    head.setTextColor(0xff222222);
    head.setGravity(Gravity.CENTER);
    head.setTag(tag);
    tags.addView(head, LayoutHelper.createLinear(0, -1, 1.0f));
  }

  private void createList(String tag, List<Variable> variables) {
    RecyclerView recyclerView = new RecyclerView(getContext());
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    recyclerView.setPadding(
        AndroidUtils.dp(16),
        AndroidUtils.dp(16),
        AndroidUtils.dp(16),
        AndroidUtils.dp(16)
    );
    recyclerView.setBackgroundColor(getResources().getColor(R.color.variableListBackground));
    RemixerAdapter adapter = new RemixerAdapter(variables);
    recyclerView.setAdapter(adapter);
    recyclerView.setTag(tag);
    lists.addView(recyclerView);
  }

  @Override
  public void onResume() {
    isAddingFragment = false;
    super.onResume();
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  public void attachToFab(final FragmentActivity activity, FloatingActionButton fab) {
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showRemixer(activity.getSupportFragmentManager(), REMIXER_TAG);
      }
    });
  }

  private class ShareDrawerOnClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {
      /*isShowingDrawer = !isShowingDrawer;
      if (isShowingDrawer) {
        expandSharingOptionsButton.setImageResource(R.drawable.ic_expand_less_black_24dp);
        expandSharingOptionsButton.setContentDescription(
            getResources().getString(R.string.collapse_sharing_options_drawer));
        expandShareDrawer();
      } else {
        expandSharingOptionsButton.setImageResource(R.drawable.ic_expand_more_black_24dp);
        expandSharingOptionsButton.setContentDescription(
            getResources().getString(R.string.expand_sharing_options_drawer));
        collapseShareDrawer();
      }*/
      StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
      StrictMode.setVmPolicy(builder.build());
      String src = "/data/data/" + getContext().getPackageName() + "/shared_prefs/remixer_local_storage.xml";
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/xml");
      shareFile = new File(getContext().getExternalCacheDir(), "theme_default.xml");
      try {
        RemixerUtils.copy(new File(src), shareFile);
      } catch (Exception e) {
        //igone
      }

      if (shareFile.exists()) {
        if (Build.VERSION.SDK_INT >= 24) {
          try {
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                getActivity(), "com.google.android.libraries.remixer.ui.provider", shareFile));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          } catch (Exception igone) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
            igone.printStackTrace();
          }
        } else {
          intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
        }
        startActivityForResult(Intent.createChooser(intent, "分享配置文件"), 500);
      }
    }
  }

  private void collapseShareDrawer() {
    final int initialHeight = shareDrawer.getMeasuredHeight();
    shareDrawer.setVisibility(View.VISIBLE);
    Animation a = new Animation() {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (interpolatedTime == 1) {
          shareDrawer.setVisibility(View.GONE);
        } else {
          shareDrawer.getLayoutParams().height =
              initialHeight - (int) (initialHeight * interpolatedTime);
          shareDrawer.requestLayout();
        }
      }

      @Override
      public boolean willChangeBounds() {
        return true;
      }
    };
    a.setDuration(COLLAPSE_DRAWER_DURATION);
    shareDrawer.startAnimation(a);
  }


  private void expandShareDrawer() {
    shareDrawer.measure(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    final int targetHeight = shareDrawer.getMeasuredHeight();
    shareDrawer.setVisibility(View.VISIBLE);
    // Workaround for API < 21
    shareDrawer.getLayoutParams().height = 1;
    Animation a = new Animation() {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        shareDrawer.getLayoutParams().height =
            interpolatedTime == 1
                ? LinearLayout.LayoutParams.WRAP_CONTENT
                : (int) (targetHeight * interpolatedTime);
        shareDrawer.requestLayout();
      }

      @Override
      public boolean willChangeBounds() {
        return true;
      }
    };
    a.setDuration(EXPAND_DRAWER_DURATION);
    shareDrawer.startAnimation(a);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 500) {
      if (shareFile != null) {
        shareFile.delete();
      }
    }
  }
}
