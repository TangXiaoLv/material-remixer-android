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

package com.google.android.libraries.remixer.storage;

import com.google.android.libraries.remixer.GlobalType;
import com.google.android.libraries.remixer.Remixer;
import com.google.android.libraries.remixer.RemixerUtils;
import com.google.android.libraries.remixer.Variable;
import com.google.android.libraries.remixer.serialization.GsonProvider;
import com.google.android.libraries.remixer.serialization.StoredVariable;
import com.google.android.libraries.remixer.sync.LocalValueSyncing;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.id.list;
import static com.google.android.libraries.remixer.DataType.KEY_COLOR;

/**
 * A {@link com.google.android.libraries.remixer.sync.SynchronizationMechanism} that stores values
 * in a SharedPreferences object.
 */
public class FileStorage extends LocalValueSyncing {

  private static final String PREFERENCES_FILE_NAME = "remixer_local_storage";
  private static final String PREFERENCES_THEME_FILE_NAME = "remixer_theme_default.xml";
  private final Context applicationContext;
  private SharedPreferences preferences;
  private final Gson gson;

  @SuppressWarnings("all")
  public FileStorage(Context context) {
    applicationContext = context.getApplicationContext();
    try {
      String exists = "/data/data/" + context.getPackageName() + "/shared_prefs/" + PREFERENCES_THEME_FILE_NAME;
      File existsFile = new File(exists);
      if (!existsFile.exists()) {
        InputStream in = context.getAssets().open("remixer/" + PREFERENCES_THEME_FILE_NAME);
        String dst = "/data/data/" + context.getPackageName() + "/shared_prefs/" + PREFERENCES_THEME_FILE_NAME;
        RemixerUtils.copy(in, new File(dst));
      }
      preferences = context.getSharedPreferences("remixer_theme_default", Context.MODE_PRIVATE);
    } catch (Exception e) {
      //igone
    }

    if (preferences == null) {
      preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }
    gson = GsonProvider.getInstance();
    for (Object data : preferences.getAll().values()) {
      // Assume all objects are actually JSON strings.
      StoredVariable<?> variable = gson.fromJson(data.toString(), StoredVariable.class);
      serializableRemixerContents.addItem(variable);
    }
  }

  public void update(InputStream themeFileIn) {
    Map<String, Object> kv = new HashMap<>();

    InputStream xmlIn = null;
    try {
      //覆盖本地theme文件
      String dst = "/data/data/" + applicationContext.getPackageName() + "/shared_prefs/" + PREFERENCES_THEME_FILE_NAME;
      File desF = new File(dst);
      RemixerUtils.copy(themeFileIn, desF);
      preferences = applicationContext.getSharedPreferences("remixer_theme_default", Context.MODE_PRIVATE);

      //读取文件
      XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
      parser.setInput(xmlIn = new FileInputStream(desF), "utf-8");//设置数据源编码
      int eventCode = parser.getEventType();//获取事件类型
      while (eventCode != XmlPullParser.END_DOCUMENT) {
        switch (eventCode) {
          case XmlPullParser.START_TAG://开始读取某个标签
            if ("string".equals(parser.getName())) {
              kv.put(parser.getAttributeValue(0), parser.nextText());
            }
            break;
        }
        eventCode = parser.next();
      }
    } catch (Exception e) {
      //igone
    } finally {
      RemixerUtils.close(xmlIn);
    }

    if (kv.size() > 0) {
      for (Object data : kv.values()) {
        // Assume all objects are actually JSON strings.
        StoredVariable<?> variable = gson.fromJson(data.toString(), StoredVariable.class);
        serializableRemixerContents.updateItem(variable);
      }

      ArrayList<List<Variable>> variables = Remixer.getInstance().getAllVariables();
      for (List<Variable> variable : variables) {
        for (Variable v : variable) {
          if (!v.getKey().startsWith("#GLOBAL#")) {
            updateVariable(v);
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void updateVariable(Variable variable) {
    StoredVariable storedVariable = serializableRemixerContents.getItem(variable.getKey());
    // Check the value for updates.
    if (RemixerUtils.equals(KEY_COLOR, storedVariable.getDataType())) {
      StoredVariable global = serializableRemixerContents.getItem(GlobalType.GLOBAL_COLOR + variable.getGlobal());
      if (global != null) {
        storedVariable = global;
      }
    }
    variable.setValueWithoutNotifyingOthers(
        variable.getDataType().getConverter().toRuntimeType(storedVariable.getSelectedValue()));
  }

  private void writeVariable(final String key) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(key, gson.toJson(serializableRemixerContents.getItem(key)));
    editor.apply();
  }

  @Override
  public void onAddingVariable(Variable variable) {
    boolean isAdded = serializableRemixerContents.getItem(variable.getKey()) != null;
    super.onAddingVariable(variable);
    if (!isAdded) {
      writeVariable(variable.getKey());
    }
  }

  @Override
  public void onValueChanged(Variable variable) {
    super.onValueChanged(variable);
    writeVariable(variable.getKey());
    //TODO 非配置模式
  }

  @Override
  public void onUpdateLocalFile(InputStream in) {
    update(in);
  }
}
