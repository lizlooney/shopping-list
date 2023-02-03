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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.util.Set;
import java.util.TreeSet;

/**
 * Activity class for choosing stores for an item on the shopping list.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class ChooseStores extends Activity {
  public static final String ITEM_STORES = "item_stores";
  public static final String ALL_STORES = "all_stores";

  private static final String LOG_TAG = "ShoppingList-ChooseStores";

  private LinearLayout storesLinearLayout;
  private final Set<String> itemStores = new TreeSet<>();
  private final Set<String> allStores = new TreeSet<>();

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.choose_stores);

    storesLinearLayout = (LinearLayout) findViewById(R.id.stores);
    Button okButton = (Button) findViewById(R.id.ok);
    Button cancelButton = (Button) findViewById(R.id.cancel);

    // stores
    Intent startIntent = getIntent();
    String[] itemStoresArray = startIntent.getStringArrayExtra(ITEM_STORES);
    for (String store : itemStoresArray) {
      itemStores.add(store);
    }
    String[] allStoresArray = startIntent.getStringArrayExtra(ALL_STORES);
    for (String store : allStoresArray) {
      allStores.add(store);
      addCheckBox(store);
    }
    storesLinearLayout.requestLayout();

    // ok and cancel buttons
    okButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent resultIntent = new Intent();
        String[] itemStoresArray = itemStores.toArray(new String[0]);
        resultIntent.putExtra(ITEM_STORES, itemStoresArray);
        String[] allStoresArray = allStores.toArray(new String[0]);
        resultIntent.putExtra(ALL_STORES, allStoresArray);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
      }
    });
    cancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });
  }

  private void addCheckBox(final String store) {
    CheckBox checkBox = new CheckBox(this);
    Utils.setColors(checkBox);
    checkBox.setText(store);
    checkBox.setChecked(itemStores.contains(store));
    checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          itemStores.add(store);
        } else {
          itemStores.remove(store);
        }
      }
    });
    storesLinearLayout.addView(checkBox);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    menu.add(getString(R.string.AddStore))
        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem menuItem) {
        addStore();
        return true;
      }
    });

    return true;
  }

  private void addStore() {
    final EditText editText = new EditText(this);
    AlertDialog alertDialog = new AlertDialog.Builder(this)
        .setTitle(getString(R.string.AddStore))
        .setView(editText)
        .create();
    alertDialog.setButton(
        DialogInterface.BUTTON_NEGATIVE,
        getString(android.R.string.cancel),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            Utils.hideSoftKeyboard(editText);
          }
        });
    alertDialog.setButton(
        DialogInterface.BUTTON_POSITIVE,
        getString(android.R.string.ok),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            Utils.hideSoftKeyboard(editText);
            String store = editText.getText().toString().trim();
            if (store.length() > 0) {
              itemStores.add(store);
              if (allStores.add(store)) {
                addCheckBox(store);
                storesLinearLayout.requestLayout();
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
}
