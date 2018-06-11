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

package com.google.android.libraries.remixer.ui;

import com.google.android.libraries.remixer.DataType;
import com.google.android.libraries.remixer.ItemListVariable;
import com.google.android.libraries.remixer.RangeVariable;
import com.google.android.libraries.remixer.Remixer;
import com.google.android.libraries.remixer.Variable;

import android.app.Application;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Remixer initialization takes care of registering data types and registering activity lifecycle
 * callbacks for Remixer.
 */
public class RemixerInitialization {

  private static final String TAG = "RemixerInitialization";
  private static boolean initialized;

  /**
   * Initializes a Remixer instance (usually {@link Remixer#getInstance()}) by registering data
   * types for it, and registers for ActivityLifecycleCallbacks with the Application {@code app}.
   *
   * <p>{@code app} can be null in case this is called from tests.
   */
  public static void initRemixer(Application app) {
    if (initialized) {
      // Guarantee that this is just called once.
      return;
    }
    initialized = true;
    if (app != null) {
      app.registerActivityLifecycleCallbacks(RemixerActivityLifecycleCallbacks.getInstance());
      AndroidUtils.init(app);
      initSetting(app);
    }

    // Boolean values only make sense in Variables, not in ItemListVariables or Range Variables.
    DataType.BOOLEAN.setLayoutIdForVariableType(
        Variable.class, R.layout.boolean_variable_widget);
    Remixer.registerDataType(DataType.BOOLEAN);

    // Color values are currently only supported in ItemListVariable. Support should be coming for
    // Variables. RangeVariable doesn't make sense for Color.
    DataType.COLOR.setLayoutIdForVariableType(
        ItemListVariable.class, R.layout.color_list_variable_widget);
    Remixer.registerDataType(DataType.COLOR);

    // Number values are only supported in ItemListVariable or RangeVariable
    DataType.NUMBER.setLayoutIdForVariableType(
        ItemListVariable.class, R.layout.item_list_variable_widget);
    DataType.NUMBER.setLayoutIdForVariableType(
        RangeVariable.class, R.layout.seekbar_range_variable_widget);
    Remixer.registerDataType(DataType.NUMBER);

    // String values are supported in Variable and ItemListVariable. Range Variable doesn't quite
    // make sens
    DataType.STRING.setLayoutIdForVariableType(
        ItemListVariable.class, R.layout.item_list_variable_widget);
    DataType.STRING.setLayoutIdForVariableType(
        Variable.class, R.layout.string_variable_widget);
    Remixer.registerDataType(DataType.STRING);
  }

  private static void initSetting(Application app) {
    try {
      InputStream in = app.getAssets().open("remixer/setting.json");
      Remixer.initSetting(readStreamToString(in));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String readStreamToString(InputStream in) {
    if (in == null) {
      return "";
    }
    ByteArrayOutputStream out = null;
    try {
      out = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = in.read(buffer)) != -1) {
        out.write(buffer, 0, length);
      }
      return out.toString("UTF-8");
    } catch (Exception e) {
      Log.e(TAG, "readStreamToString fail.", e);
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        Log.e(TAG, "readStreamToString fail.", e);
      }

      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        Log.e(TAG, "readStreamToString fail.", e);
      }
    }
    return "";
  }
}
