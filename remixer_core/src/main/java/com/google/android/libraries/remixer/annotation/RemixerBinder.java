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

package com.google.android.libraries.remixer.annotation;

import com.google.android.libraries.remixer.Callback;
import com.google.android.libraries.remixer.DataType;
import com.google.android.libraries.remixer.GlobalType;
import com.google.android.libraries.remixer.ItemListVariable;
import com.google.android.libraries.remixer.Remixer;
import com.google.android.libraries.remixer.RemixerUtils;
import com.google.android.libraries.remixer.Variable;
import com.google.android.libraries.remixer.settings.GlobalSetting;

import org.omg.CORBA.Object;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.net.StandardSocketOptions;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.libraries.remixer.RemixerUtils.parseColor;

/**
 * Used to bind activities to their implicitly generated variables.
 */
public class RemixerBinder {

  private static boolean isSet = false;

  private static final java.lang.Object CONTEXT = new java.lang.Object();

  /**
   * Binds an activity's remixer to its generated variables.
   *
   * @throws RemixerBindingException When there is an issue instantiating the Binder class
   */
  @SuppressWarnings("ClassNewInstance")
  public static <T> void bind(T target) {
    if (Remixer.getRegisteredDataTypes().isEmpty()) {
      throw new IllegalStateException(
          "There are no registered data types for remixer. This indicates that you have not "
              + " initialized Remixer at all and it will fail in runtime. Please run "
              + "RemixerInitialization.initRemixer in your application class. See the Remixer README "
              + "for detailed instructions. ");
    }

    checkSetting();

    try {
      Class<?> bindingClass =
          Class.forName(target.getClass().getCanonicalName() + "_RemixerBinder");

      @SuppressWarnings("unchecked")
      Binder<T> binder = (Binder<T>) bindingClass.newInstance();
      binder.bindInstance(target);

    } catch (ClassNotFoundException ex) {
      throw new RemixerBindingException(
          "Remixer binder class can not be found or initialized for "
              + target.getClass().toString(),
          ex);
    } catch (InstantiationException ex) {
      throw new RemixerBindingException(
          "Remixer binder class can not instantiated for "
              + target.getClass().toString(),
          ex);
    } catch (IllegalAccessException ex) {
      throw new RemixerBindingException(
          "Remixer binder class can not be instantiated for "
              + target.getClass().toString(),
          ex);
    }
  }

  private static void checkSetting() {
    if (!isSet) {
      isSet = true;
      GlobalSetting setting = Remixer.getGlobalSetting();
      List<GlobalSetting.ColorSetting> colors = setting.getColors();
      addColorItem(colors);
    }
  }

  private static <T> void addColorItem(List<GlobalSetting.ColorSetting> colors) {
    if (colors != null && colors.size() > 0) {
      for (GlobalSetting.ColorSetting setting : colors) {
        String[] c = setting.getDefColors().split(",");
        Integer[] intColors = new Integer[c.length];
        for (int i = 0; i < c.length; i++) {
          intColors[i] = parseColor(c[i]);
        }

        ItemListVariable<Integer> variable = new ItemListVariable.Builder<Integer>()
            .setLimitedToValues(intColors)
            .setKey(GlobalType.GLOBAL_COLOR + setting.getType())
            .setTitle(setting.getTitle() + " [" + setting.getType() + "]")
            .setGlobal(setting.getType())
            .setContext(CONTEXT)
            .setDataType(DataType.COLOR)
            .setCallback(new Callback<Integer>() {
              @Override
              public void onValueSet(Variable<Integer> variable) {
                ArrayList<List<Variable>> variables = Remixer.getInstance().getAllVariables();
                for (List<Variable> variableList : variables) {
                  for (Variable v : variableList) {
                    if (!v.isDestroy()
                        && !v.getKey().startsWith("#GLOBAL#")
                        && RemixerUtils.equals(v.getGlobal(), variable.getGlobal())
                        && "__DataTypeColor__".equals(v.getDataType().getName())) {
                      v.setValue(variable.getSelectedValue());
                    }
                  }
                }
              }
            }).build();
        Remixer.getInstance().addItem(variable);
      }
    }
  }

  /**
   * Interface that autogenerated binders implement.
   */
  public interface Binder<T> {

    /**
     * Bind an activity's remixer instance to its generated variables.
     */
    void bindInstance(T activity);
  }
}
