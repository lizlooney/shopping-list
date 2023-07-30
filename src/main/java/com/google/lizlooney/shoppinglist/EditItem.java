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
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;

/**
 * Activity class for editing an item on the shopping list.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class EditItem extends Activity {
  public static final String ITEM_DESCRIPTION = "item_description";
  public static final String ITEM_CATEGORY = "item_category";
  public static final String ITEM_LAST_PURCHASED = "item_last_purchased";
  public static final String ITEM_AUTO_DELETE = "item_auto_delete";
  public static final String ITEM_STORES = "item_stores";
  public static final String ITEM_AISLES = "item_aisles";
  public static final String ALL_CATEGORIES = "all_categories";
  public static final String ALL_AISLES = "all_aisles";
  public static final String ALL_STORES = "all_stores";

  private static final String LOG_TAG = "ShoppingList-EditItem";
  private static final int CHOOSE_STORES_REQUEST_CODE = 1;

  private final Set<String> allCategories = new TreeSet<>();
  private final Set<String> allStores = new TreeSet<>();
  private final Set<String> allAisles = new TreeSet<>((a1, a2) -> Aisles.compareAisles(a1, a2));

  private EditText descriptionEditText;
  private Spinner categorySpinner;
  private ArrayAdapter<String> categoryAdapter;
  private LinearLayout storeAislesContainer;
  private final List<Spinner> aisleSpinners = new ArrayList<>();
  private TextView lastPurchasedTextView;
  private Button chooseStoresButton;
  private Button okButton;

  private final Map<String, String> itemStoreAisles = new TreeMap<>();

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit_item);

    descriptionEditText = findViewById(R.id.description);
    categorySpinner = findViewById(R.id.category);
    storeAislesContainer = findViewById(R.id.storeAislesContainer);
    chooseStoresButton = findViewById(R.id.chooseStores);
    lastPurchasedTextView = findViewById(R.id.lastPurchased);
    final CheckBox autoDeleteCheckBox = findViewById(R.id.autoDelete);
    Button deleteButton = findViewById(R.id.deleteNow);
    okButton = findViewById(R.id.ok);
    Button cancelButton = findViewById(R.id.cancel);

    Intent startIntent = getIntent();

    Collections.addAll(allCategories, startIntent.getStringArrayExtra(ALL_CATEGORIES));
    Collections.addAll(allStores, startIntent.getStringArrayExtra(ALL_STORES));
    Collections.addAll(allAisles, startIntent.getStringArrayExtra(ALL_AISLES));

    // description
    if (startIntent.hasExtra(ITEM_DESCRIPTION)) {
      String description = startIntent.getStringExtra(ITEM_DESCRIPTION);
      descriptionEditText.setText(description);
      descriptionEditText.setSelection(description.length());
      descriptionEditText.requestLayout();
    }
    descriptionEditText.addTextChangedListener(new TextChangeAdapter() {
      @Override
      public void textChanged(String s) {
        String description = s.trim();
        okButton.setEnabled(description.length() > 0);
      }
    });

    // category
    categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_category);
    categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_category);
    categorySpinner.setAdapter(categoryAdapter);
    Utils.updateSpinner(
        categorySpinner,
        categoryAdapter,
        allCategories,
        startIntent.getStringExtra(ITEM_CATEGORY));

    // stores and aisles
    String[] itemStores = startIntent.getStringArrayExtra(ITEM_STORES);
    if (itemStores == null) {
      itemStores = new String[0];
    }
    String[] itemAisles = startIntent.getStringArrayExtra(ITEM_AISLES);
    if (itemAisles == null) {
      itemAisles = new String[0];
    }
    if (itemStores.length == itemAisles.length) {
      for (int i = 0; i < itemStores.length; i++) {
        String store = itemStores[i];
        String aisle = itemAisles[i];
        itemStoreAisles.put(store, aisle);
      }
    }
    fillStoreAislesContainer();

    chooseStoresButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            chooseStores();
          }
        });

    // last purchased
    long lastPurchased = startIntent.getLongExtra(ITEM_LAST_PURCHASED, 0);
    if (lastPurchased == 0) {
      lastPurchasedTextView.setText("unknown");
    } else {
      lastPurchasedTextView.setText(DateFormat.getDateInstance().format(new Date(lastPurchased)));
    }

    // auto-delete
    autoDeleteCheckBox.setChecked(startIntent.getBooleanExtra(ITEM_AUTO_DELETE, false));

    // delete button
    if (startIntent.hasExtra(ITEM_DESCRIPTION)) {
      deleteButton.setOnClickListener(
          new OnClickListener() {
            @Override
            public void onClick(View view) {
              AlertDialog alertDialog =
                  new AlertDialog.Builder(EditItem.this)
                      .setTitle(getString(R.string.DeleteNow))
                      .setMessage(getString(R.string.DeleteNowConfirm))
                      .setNegativeButton(
                          android.R.string.cancel, (dialog, which) -> dialog.cancel())
                      .setPositiveButton(
                          getString(android.R.string.ok),
                          new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                              dialog.dismiss();
                              // To delete the item, set the result with no intent.
                              setResult(Activity.RESULT_OK);
                              finish();
                            }
                          })
                      .create();
              alertDialog.show();
            }
          });
    } else {
      // Can't delete the item because it hasn't been added yet.
      deleteButton.setVisibility(View.GONE);
    }

    // ok and cancel buttons
    okButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String descriptionString = descriptionEditText.getText().toString().trim();
        if (descriptionString.length() > 0) {
          Intent resultIntent = new Intent();
          resultIntent.putExtra(ITEM_DESCRIPTION, descriptionString);
          String categoryString = (String) categorySpinner.getSelectedItem();
          resultIntent.putExtra(ITEM_CATEGORY, categoryString);
          boolean autoDeleteBoolean = autoDeleteCheckBox.isChecked();
          resultIntent.putExtra(ITEM_AUTO_DELETE, autoDeleteBoolean);

          List<String> itemStores = new ArrayList<>();
          List<String> itemAisles = new ArrayList<>();
          for (Map.Entry<String, String> entry : itemStoreAisles.entrySet()) {
            itemStores.add(entry.getKey());
            itemAisles.add(entry.getValue());
          }
          String[] itemStoresArray = itemStores.toArray(new String[0]);
          resultIntent.putExtra(ITEM_STORES, itemStoresArray);
          String[] itemAislesArray = itemAisles.toArray(new String[0]);
          resultIntent.putExtra(ITEM_AISLES, itemAislesArray);

          setResult(Activity.RESULT_OK, resultIntent);
          finish();
        }
      }
    });
    cancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });

    initDisplay();
  }

  private void initDisplay() {
    if (categorySpinner.getHeight() == 0) {
      new Handler()
          .post(
              new Runnable() {
                @Override
                public void run() {
                  initDisplay();
                }
              });
      return;
    }

    categorySpinner.setDropDownVerticalOffset(categorySpinner.getHeight());
    for (Spinner aisleSpinner : aisleSpinners) {
      aisleSpinner.setDropDownVerticalOffset(aisleSpinner.getHeight());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    menu.add(getString(R.string.AddCategory))
        .setOnMenuItemClickListener(
            new OnMenuItemClickListener() {
              @Override
              public boolean onMenuItemClick(MenuItem menuItem) {
                Utils.addValueToSpinners(EditItem.this, getString(R.string.AddCategory),
                    allCategories, categorySpinner);
                return true;
              }
            });

    menu.add(getString(R.string.AddAisle))
        .setOnMenuItemClickListener(
            new OnMenuItemClickListener() {
              @Override
              public boolean onMenuItemClick(MenuItem menuItem) {
                Utils.addValueToSpinners(EditItem.this, getString(R.string.AddAisle),
                    allAisles, aisleSpinners.toArray(new Spinner[0]));
                return true;
              }
            });

    return true;
  }

  private void fillStoreAislesContainer() {
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    int storeWidth = (int) (size.x * 0.55);

    storeAislesContainer.removeAllViews();
    aisleSpinners.clear();

    for (Map.Entry<String, String> entry : itemStoreAisles.entrySet()) {
      final String store = entry.getKey();
      String aisle = entry.getValue();

      LinearLayout row = new LinearLayout(this);

      // Create a TextView for the store.
      TextView storeTextView = new TextView(this);
      Utils.setColors(storeTextView);
      storeTextView.setTextSize(20);
      storeTextView.setText(store);
      row.addView(storeTextView, new LayoutParams(storeWidth, LayoutParams.WRAP_CONTENT, 0f));

      // Create a Spinner for the aisle.
      Spinner aisleSpinner = new Spinner(this);
      aisleSpinners.add(aisleSpinner);
      ArrayAdapter<String> aisleAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_aisle);
      aisleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_aisle);
      aisleSpinner.setAdapter(aisleAdapter);
      aisleSpinner.setBackgroundTintList(ColorStateList.valueOf(0xFF00FFFF));
      aisleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
          String selectedAisle = (String) adapterView.getItemAtPosition(pos);
          itemStoreAisles.put(store, selectedAisle);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
      });
      Utils.updateSpinner(
          aisleSpinner,
          aisleAdapter,
          allAisles,
          aisle);
      row.addView(aisleSpinner, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

      // Add the row to the storeAislesContainer.
      storeAislesContainer.addView(row);
    }
  }

  private void chooseStores() {
    Intent activityIntent = new Intent();
    activityIntent.setClass(this, ChooseStores.class);
    String[] itemStoresArray = itemStoreAisles.keySet().toArray(new String[0]);
    activityIntent.putExtra(ChooseStores.ITEM_STORES, itemStoresArray);
    String[] allStoresArray = allStores.toArray(new String[0]);
    activityIntent.putExtra(ChooseStores.ALL_STORES, allStoresArray);
    try {
      startActivityForResult(activityIntent, CHOOSE_STORES_REQUEST_CODE);
    } catch (ActivityNotFoundException e) {
      Log.e(LOG_TAG, "startActivityForResult threw ActivityNotFoundException", e);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CHOOSE_STORES_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK && data != null) {
        allStores.clear();
        Collections.addAll(allStores, data.getStringArrayExtra(ChooseStores.ALL_STORES));

        String[] itemStores = data.getStringArrayExtra(ChooseStores.ITEM_STORES);
        Map<String, String> oldStoreAisles = new TreeMap<>(itemStoreAisles);
        itemStoreAisles.clear();
        for (String store : itemStores) {
          String aisle = oldStoreAisles.get(store);
          if (aisle == null) {
            aisle = "1"; // If we can't figure out something better, use aisle 1.
            // If there is an aisle with the same name as the store, use it.
            if (allAisles.contains(store)) {
              aisle = store;
            } else {
              // If there was already a store and its aisle was not the store name, use that aisle.
              for (Map.Entry<String, String> entry : oldStoreAisles.entrySet()) {
                String oldStore = entry.getKey();
                String oldStoreAisle = entry.getValue();
                if (!oldStoreAisle.equals(oldStore)) {
                  aisle = oldStoreAisle;
                  break;
                }
              }
            }
          }
          itemStoreAisles.put(store, aisle);
        }

        fillStoreAislesContainer();
      }
    }
  }
}
