/*
 * Copyright 2023 Google LLC
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

package com.google.lizlooney.shoppinglist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.util.Collection;

/**
 * Class of useful static metnods.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class Utils {
  private Utils() {
  }

  public static void showSoftKeyboard(EditText editText) {
    if (!editText.isFocused()) {
      editText.requestFocus();
    }
    InputMethodManager imm =
        (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  }

  public static void hideSoftKeyboard(EditText editText) {
    InputMethodManager imm =
        (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
  }

  public static void setColors(CheckBox checkBox) {
    checkBox.setTextColor(Color.WHITE);
    int[][] states =
        new int[][] {
          new int[] {android.R.attr.state_checked}, new int[] {-android.R.attr.state_checked}
        };
    int[] colors = new int[] {Color.GREEN, Color.CYAN};
    checkBox.setButtonTintList(new ColorStateList(states, colors));
  }

  public static void setColors(LinearLayout linearLayout) {}

  public static void setColors(TextView textView) {
    textView.setTextColor(Color.WHITE);
  }

  public static void addValueToSpinners(Context context, String title, final Collection<String> values, final Spinner... spinners) {
    final EditText editText = new EditText(context);
    AlertDialog alertDialog =
        new AlertDialog.Builder(context).setTitle(title).setView(editText).create();
    alertDialog.setButton(
        DialogInterface.BUTTON_NEGATIVE,
        context.getString(android.R.string.cancel),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            Utils.hideSoftKeyboard(editText);
          }
        });
    alertDialog.setButton(
        DialogInterface.BUTTON_POSITIVE,
        context.getString(android.R.string.ok),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            Utils.hideSoftKeyboard(editText);
            String value = editText.getText().toString().trim();
            if (value.length() > 0) {
              values.add(value);
              boolean selectValue = spinners.length > 1;
              for (Spinner spinner : spinners) {
                addValueToSpinner(value, spinner, selectValue);
              }
            }
          }
        });
    alertDialog.setOnShowListener(
        new OnShowListener() {
          @Override
          public void onShow(DialogInterface dialog) {
            Utils.showSoftKeyboard(editText);
          }
        });
    alertDialog.show();
  }

  @SuppressWarnings("unchecked")
  private static void addValueToSpinner(String value, Spinner spinner, boolean selectValue) {
    SpinnerAdapter spinnerAdapter = spinner.getAdapter();
    if (spinnerAdapter instanceof ArrayAdapter) {
      ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerAdapter;

      // Check whether the value is already present.
      int position = adapter.getPosition(value);
      if (position == -1) {
        // The value is not already present... add it now.
        adapter.add(value);
        spinner.setEnabled(adapter.getCount() > 0);
        position = adapter.getCount() - 1;
      }
      if (selectValue) {
        // Select the new (or already present) value.
        spinner.setSelection(position);
      }
    }
  }

  public static void updateSpinner(
      Spinner spinner, ArrayAdapter<String> adapter, Collection<String> values, String currentValue) {
    adapter.clear();
    if (values.isEmpty()) {
      adapter.add("---");
    } else {
      adapter.addAll(values);
    }
    spinner.setEnabled(adapter.getCount() > 0);
    int position = (currentValue != null) ? adapter.getPosition(currentValue) : -1;
    if (position != -1) {
      spinner.setSelection(position);
    } else {
      spinner.setSelection(0);
    }
  }
}
