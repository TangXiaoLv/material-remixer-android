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

package com.google.android.libraries.remixer.sync;

import com.google.android.libraries.remixer.GlobalType;
import com.google.android.libraries.remixer.Remixer;
import com.google.android.libraries.remixer.RemixerUtils;
import com.google.android.libraries.remixer.Variable;
import com.google.android.libraries.remixer.serialization.SerializableRemixerContents;
import com.google.android.libraries.remixer.serialization.StoredVariable;

import java.util.List;

import static com.google.android.libraries.remixer.DataType.KEY_COLOR;

/**
 * A purely-local implementation of a Synchronization Mechanism. This handles keeping values in sync
 * locally.
 */
public class LocalValueSyncing extends SynchronizationMechanismAdapt {

  protected SerializableRemixerContents serializableRemixerContents =
      new SerializableRemixerContents();
  private Remixer remixer;

  @Override
  public void setRemixerInstance(Remixer remixer) {
    this.remixer = remixer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onAddingVariable(Variable variable) {
    serializableRemixerContents.addItem(variable);
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

  @Override
  @SuppressWarnings("unchecked")
  public void onValueChanged(Variable variable) {
    serializableRemixerContents.setValue(variable);
    List<Variable> itemList = remixer.getVariablesWithKey(variable.getKey());
    for (Variable item : itemList) {
      if (item != variable) {
        item.setValueWithoutNotifyingOthers(
            variable.getDataType().getConverter().toRuntimeType(variable.getSelectedValue()));
      }
    }
  }

  @Override
  public void onContextChanged(Object currentContext) {
    // Nothing to do here, this class does not care which is the current context.
  }

  @Override
  public void onContextRemoved(Object currentContext) {
    // Nothing to do here, this class does not care which is the current context.
  }
}
