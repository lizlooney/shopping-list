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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity class for the shopping list.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class ShoppingList extends Activity {
  public static final String LOG_TAG = "ShoppingList";

  private static final boolean DEBUG = false;

  private static final int EDIT_ITEM_REQUEST_CODE = 1;

  private static final String IMPORT_FILE_NAME = "ShoppingList.in";
  private static final String EXPORT_FILE_NAME = "ShoppingList.out";

  final static String STORE_FILTER_ALL = "<All Stores>";
  final static String STORE_FILTER_MISSING = "<Missing Store>";

  private Storage storage = new Storage();
  /**
   * All the items, not just the ones currently displayed.
   */
  private final List<Item> allItems = new ArrayList<>();
  private final Object allItemsLock = new Object();
  /**
   * All the categories that are used in items.
   */
  private final Categories allCategories = new Categories();
  /**
   * All the aisles that are used in items.
   */
  private final Aisles allAisles = new Aisles();
  /**
   * All the stores that are used in items.
   */
  private final Stores allStores = new Stores();

  /**
   * The items being displayed.
   */
  private final List<Item> displayedItems = new ArrayList<>();
  /**
   * Comparator used to sort the items being displayed.
   */
  private Comparator<Item> comparator;

  /**
   * The current display mode.
   */
  private DisplayMode displayMode;
  /**
   * The current store filter.
   */
  private String storeFilter;

  private TextView displayModeTextView;
  private Spinner storeFilterSpinner;
  private boolean spinnersDropDownVerticalOffsetDone;
  private ArrayAdapter<String> storeFilterAdapter;
  private LinearLayout searchArea;
  private EditText searchBox;
  private TextView secondHeading;
  private LinearLayout itemsContainer;

  private Item itemBeingEdited;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.shopping_list);

    storage.init(this);

    displayModeTextView = findViewById(R.id.displayMode);
    displayModeTextView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        toggleDisplayMode();
      }
    });
    storeFilterSpinner = findViewById(R.id.storeFilter);
    storeFilterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        String selectedStoreFilter = (String) adapterView.getItemAtPosition(pos);
        setStoreFilter(selectedStoreFilter);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    });

    searchArea = findViewById(R.id.searchArea);
    searchBox = findViewById(R.id.searchBox);
    searchBox.addTextChangedListener(new TextChangeAdapter() {
      @Override
      public void textChanged(String s) {
        updateDisplay();
      }
    });
    Button searchClearButton = findViewById(R.id.searchClear);
    searchClearButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        searchBox.setText("");
      }
    });
    Button searchHideButton = findViewById(R.id.searchHide);
    searchHideButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        stopSearch();
      }
    });

    TextView firstHeading = findViewById(R.id.firstHeading);
    firstHeading.setPaintFlags(firstHeading.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    secondHeading = findViewById(R.id.secondHeading);
    secondHeading.setPaintFlags(secondHeading.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

    itemsContainer = findViewById(R.id.items);

    loadFromStorage();

    storeFilterAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_large_right);
    storeFilterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_large_right);
    storeFilterSpinner.setAdapter(storeFilterAdapter);

    updateComparator();
    updateDisplay();
  }

  private void loadFromStorage() {
    synchronized (allItemsLock) {
      storage.loadItems(allItems);
      if (DEBUG) {
        Log.d(LOG_TAG, "Loaded " + allItems.size() + " items from storage");
      }
      allStores.loadStores(allItems);
      allAisles.loadAisles(allItems);
      allCategories.loadCategories(allItems);
    }
    displayMode = storage.loadDisplayMode();
    storeFilter = storage.loadStoreFilter();
    if (storeFilter == null || storeFilter.isEmpty()) {
      storeFilter = STORE_FILTER_ALL;
    }
  }

  private void setSpinnersDropDownVerticalOffset() {
    if (spinnersDropDownVerticalOffsetDone) {
      return;
    }
    if (storeFilterSpinner.getHeight() == 0) {
      new Handler()
          .post(
              new Runnable() {
                @Override
                public void run() {
                  setSpinnersDropDownVerticalOffset();
                }
              });
      return;
    }

    storeFilterSpinner.setDropDownVerticalOffset(storeFilterSpinner.getHeight());
    spinnersDropDownVerticalOffsetDone = true;
  }

  @Override
  public boolean onSearchRequested() {
    if (searchArea.getVisibility() != View.VISIBLE) {
      startSearch();
    } else {
      stopSearch();
    }
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    menu.clear();

    if (displayMode == DisplayMode.PLANNING) {
      // Search
      if (searchArea.getVisibility() != View.VISIBLE) {
        menu.add(getString(R.string.ShowSearch))
            .setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem menuItem) {
                    startSearch();
                    return true;
                  }
                });
      }
    }

    // Refresh
    menu.add(getString(R.string.Refresh))
        .setOnMenuItemClickListener(
            new OnMenuItemClickListener() {
              @Override
              public boolean onMenuItemClick(MenuItem menuItem) {
                updateDisplay();
                return true;
              }
            });

    // Add Item...
    menu.add(getString(R.string.AddItem))
        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem menuItem) {
        editItem(null);
        return true;
      }
    });

    if (displayMode == DisplayMode.SHOPPING) {
      // Clear Checked Items
      menu.add(getString(R.string.ClearCheckedItems))
          .setOnMenuItemClickListener(
              new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                  for (final Item item : displayedItems) {
                    if (item.getState() == ItemState.IN_SHOPPING_CART) {
                      if (item.getAutoDelete()) {
                        synchronized (allItemsLock) {
                          allItems.remove(item);
                        }
                        storage.deleteItem(item);
                      } else {
                        item.setState(ItemState.DONT_NEED);
                        storage.saveItem(item);
                      }
                    }
                  }
                  updateDisplay();
                  return true;
                }
              });
    }

    if (displayMode == DisplayMode.PLANNING) {
      // Import Items
      File importFile = new File(Environment.getExternalStorageDirectory().getPath(), IMPORT_FILE_NAME);
      if (importFile.exists() && importFile.isFile()) {
        menu.add(getString(R.string.ImportItems))
            .setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem menuItem) {
                    importFromFile();
                    return true;
                  }
                });
      }

      // Export Items
      menu.add(getString(R.string.ExportItems))
          .setOnMenuItemClickListener(
              new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                  exportToFile();
                  return true;
                }
              });
    }

    return true;
  }

  private void toggleDisplayMode() {
    switch (displayMode) {
      default:
      case PLANNING:
        displayMode = DisplayMode.SHOPPING;
        break;
      case SHOPPING:
        displayMode = DisplayMode.PLANNING;
        break;
    }
    storage.saveDisplayMode(displayMode);

    updateComparator();
    updateDisplay();
  }

  private void setStoreFilter(String newStoreFilter) {
    if (!Objects.equals(storeFilter, newStoreFilter)) {
      storeFilter = newStoreFilter;
      storage.saveStoreFilter(storeFilter);

      updateComparator();
      updateDisplay();
    }
  }

  private void updateComparator() {
    switch (displayMode) {
      default:
      case PLANNING:
        comparator = ItemComparators.SORT_FOR_PLANNING;
        break;
      case SHOPPING:
        comparator = ItemComparators.sortForShopping(storeFilter);
        break;
    }
  }

  private void updateDisplay() {
    synchronized (allItemsLock) {
      if (itemsContainer.getWidth() == 0) {
        new Handler().post(new Runnable() {
          @Override
          public void run() {
            updateDisplay();
          }
        });
        return;
      }
      long startTimeDebug = System.currentTimeMillis();

      itemsContainer.removeAllViews();
      displayedItems.clear();
      Stores neededStores = new Stores();

      String searchText;
      if (searchArea.getVisibility() == View.VISIBLE) {
        searchText = searchBox.getText().toString().trim().toLowerCase(Locale.getDefault());
        if (searchText.length() == 0) {
          searchText = null;
        }
      } else {
        searchText = null;
      }

      displayModeTextView.setText(DisplayMode.toString(this, displayMode));

      switch (displayMode) {
        default:
        case PLANNING:
          secondHeading.setText(R.string.CategoryHeading);
          for (Item item : allItems) {
            if (itemShouldBeShown(item, searchText)) {
              displayedItems.add(item);
            }
          }
          break;

        case SHOPPING:
          secondHeading.setText(R.string.AisleHeading);
          for (Item item : allItems) {
            if (item.getState() == ItemState.DONT_NEED) {
              continue;
            }
            neededStores.add(item);
            if (itemShouldBeShown(item, searchText)) {
              displayedItems.add(item);
            }
          }
          break;
      }

      Collections.sort(displayedItems, comparator);

      for (final Item item : displayedItems) {
        LinearLayout row = new LinearLayout(this);
        Utils.setColors(row);

        // Create a checkbox for the item state.
        final CheckBox stateCheckBox = new CheckBox(this);
        Utils.setColors(stateCheckBox);
        setStateCheckBox(stateCheckBox, item);
        stateCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (stateCheckBox.isEnabled()) {
              stateCheckBoxChanged(stateCheckBox, isChecked, item);
            }
          }
        });
        row.addView(stateCheckBox, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0f));

        // Create a TextView for the item description.
        TextView description = new TextView(this);
        Utils.setColors(description);
        description.setText(item.getDescription());
        description.setOnLongClickListener(new OnLongClickListener() {
          @Override
          public boolean onLongClick(View view) {
            editItem(item);
            return true;
          }
        });
        row.addView(description, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        // Create another TextView for the category or aisle, depending on the display mode.
        TextView second = new TextView(this);
        Utils.setColors(second);
        String text;
        switch (displayMode) {
          default:
          case PLANNING:
            text = item.getCategory();
            break;
          case SHOPPING:
            if (storeFilter.equals(STORE_FILTER_ALL) ||
                storeFilter.equals(STORE_FILTER_MISSING)) {
              text = "";
            } else {
              text = item.getAisle(storeFilter);
            }
            break;
        }
        second.setText(text);
        second.setGravity(Gravity.RIGHT);
        second.setPadding(0, 0, 10, 0);
        row.addView(
            second, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0f));

        // Add the row to the itemsContainer.
        itemsContainer.addView(row);
      }

      if (displayedItems.size() == 0) {
        TextView message = new TextView(this);
        Utils.setColors(message);
        message.setSingleLine(false);
        message.setTextSize(30);
        message.setGravity(Gravity.CENTER);
        String text;
        switch (displayMode) {
          default:
          case PLANNING:
            text = getString(R.string.NoItems);
            break;
          case SHOPPING:
            if (storeFilter.equals(STORE_FILTER_ALL)) {
              text = getString(R.string.NoItemsNeeded);
            } else if (storeFilter.equals(STORE_FILTER_MISSING)) {
              text = getString(R.string.NoItemsNeeded);
            } else {
              text = getString(R.string.NoItemsNeededAtStore, storeFilter);
            }
            break;
        }
        message.setText(text);
        itemsContainer.addView(message, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 0f));
      }

      itemsContainer.requestLayout();

      Utils.updateSpinner(
          storeFilterSpinner,
          storeFilterAdapter,
          (displayMode == DisplayMode.PLANNING)
              ? allStores.getStoresForStoreFilter()
              : neededStores.getStoresForStoreFilter(),
          storeFilter);
      setSpinnersDropDownVerticalOffset();

      if (DEBUG) {
        long elapsedTimeDebug = System.currentTimeMillis() - startTimeDebug;
        Log.d(LOG_TAG, "storeFilter is \"" + storeFilter + "\"");
        Log.d(
            LOG_TAG,
            "displaying "
                + displayedItems.size()
                + " (of "
                + allItems.size()
                + ") took "
                + elapsedTimeDebug
                + " ms");
      }
    }
  }

  private boolean itemShouldBeShown(Item item, String searchText) {
    if (storeFilter.equals(STORE_FILTER_MISSING)) {
      // Only show items that are missing a store.
      if (!item.isMissingStore()) {
        return false;
      }
    }

    // Eliminate items because of search text.
    if (searchText != null && !item.getDescription().toLowerCase(Locale.getDefault()).contains(searchText)) {
      return false;
    }

    return true;
  }

  private void startSearch() {
    if (searchArea.getVisibility() != View.VISIBLE) {
      searchArea.setVisibility(View.VISIBLE);
      Utils.showSoftKeyboard(searchBox);
    }
  }

  private void stopSearch() {
    searchBox.setText("");
    searchArea.setVisibility(View.GONE);
    Utils.hideSoftKeyboard(searchBox);
  }

  private void setStateCheckBox(CheckBox stateCheckBox, Item item) {
    switch (displayMode) {
      case PLANNING:
        switch (item.getState()) {
          case DONT_NEED:
            stateCheckBox.setEnabled(true);
            stateCheckBox.setChecked(false);
            break;
          case NEED:
            stateCheckBox.setEnabled(true);
            stateCheckBox.setChecked(true);
            break;
          case IN_SHOPPING_CART:
            // If the item is already in the shopping cart, disable the checkbox.
            stateCheckBox.setEnabled(false);
            stateCheckBox.setChecked(true);
            break;
        }
        break;
      case SHOPPING:
        switch (item.getState()) {
          case DONT_NEED:
            // Item should not even be displayed in SHOPPING mode.
            stateCheckBox.setEnabled(false);
            stateCheckBox.setChecked(false);
            break;
          case NEED:
            stateCheckBox.setEnabled(true);
            stateCheckBox.setChecked(false);
            break;
          case IN_SHOPPING_CART:
            stateCheckBox.setEnabled(true);
            stateCheckBox.setChecked(true);
            break;
        }
        break;
    }
  }

  private void stateCheckBoxChanged(CheckBox stateCheckBox, boolean isChecked, Item item) {
    switch (displayMode) {
      case PLANNING:
        item.setState(isChecked ? ItemState.NEED : ItemState.DONT_NEED);
        break;
      case SHOPPING:
        item.setState(isChecked ? ItemState.IN_SHOPPING_CART : ItemState.NEED);
        break;
    }
    storage.saveItem(item);
  }

  private void editItem(Item item) {
    Intent activityIntent = new Intent();
    activityIntent.setClass(this, EditItem.class);
    activityIntent.putExtra(EditItem.ALL_CATEGORIES, allCategories.getCategoriesArray());
    activityIntent.putExtra(EditItem.ALL_STORES, allStores.getStoresArray());
    activityIntent.putExtra(EditItem.ALL_AISLES, allAisles.getAislesArray());

    if (item != null) {
      item.exportToIntent(activityIntent);
    } else {
      if (searchArea.getVisibility() == View.VISIBLE) {
        String descriptionString = searchBox.getText().toString().trim();
        activityIntent.putExtra(EditItem.ITEM_DESCRIPTION, descriptionString);
      }
    }

    try {
      itemBeingEdited = item;
      startActivityForResult(activityIntent, EDIT_ITEM_REQUEST_CODE);
    } catch (ActivityNotFoundException e) {
      Log.e(LOG_TAG, "startActivityForResult threw ActivityNotFoundException", e);
      itemBeingEdited = null;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == EDIT_ITEM_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        if (data != null) {
          Item item;
          if (itemBeingEdited != null) {
            // Edit an existing item.
            item = itemBeingEdited;
            itemBeingEdited = null;
          } else {
            // Add an item.
            item = new Item(storage.getUnusedItemId());
            synchronized (allItemsLock) {
              allItems.add(item);
            }
          }
          item.importFromIntent(data);
          allCategories.add(item);
          allStores.add(item);
          allAisles.add(item);
          storage.saveItem(item);
        } else {
          // Delete the item.
          if (itemBeingEdited != null) {
            synchronized (allItemsLock) {
              allItems.remove(itemBeingEdited);
            }
            storage.deleteItem(itemBeingEdited);
            itemBeingEdited = null;
          }
        }
        updateDisplay();
      }
    }
  }

  private void importFromFile() {
    synchronized (allItemsLock) {
      allItems.clear();
    }
    allCategories.clear();
    allAisles.clear();
    allStores.clear();
    displayMode = DisplayMode.PLANNING;
    storeFilter = STORE_FILTER_ALL;
    storage.clear();

    new Thread(new Runnable() {
      @Override
      public void run() {
        long startTimeDebug = System.currentTimeMillis();
        int count = 0;
        try {
          BufferedReader reader = new BufferedReader(new FileReader(new File(Environment.getExternalStorageDirectory().getPath(), IMPORT_FILE_NAME)));
          try {
            while (true) {
              String line = reader.readLine();
              if (line == null) {
                break;
              }
              line = line.trim();
              if (line.length() > 0) {
                Item item = new Item(storage.getUnusedItemId());
                item.setState(ItemState.DONT_NEED);
                if (item.importFromString(line)) {
                  synchronized (allItemsLock) {
                    allItems.add(item);
                  }
                  allCategories.add(item);
                  allAisles.add(item);
                  allStores.add(item);
                  storage.saveItem(item);
                  count++;
                } else {
                  Log.w(LOG_TAG, "Unable to import item from \"" + line + "\"");
                  storage.deleteItem(item);
                }
              }
            }
          } finally {
            reader.close();
          }
        } catch (IOException e) {
          Log.e(LOG_TAG, "importFromFile caught", e);
        }
        if (DEBUG) {
          long elapsedTimeDebug = System.currentTimeMillis() - startTimeDebug;
          Log.d(LOG_TAG, "Importing " + count + " items took " + elapsedTimeDebug + " ms");
          Log.d(LOG_TAG, "Categories:");
          for (String category : allCategories.getCategoriesArray()) {
            Log.d(LOG_TAG, "    " + category);
          }
          Log.d(LOG_TAG, "Aisles:");
          for (String aisle : allAisles.getAislesArray()) {
            Log.d(LOG_TAG, "    " + aisle);
          }
          Log.d(LOG_TAG, "Stores:");
          for (String store : allStores.getStoresArray()) {
            Log.d(LOG_TAG, "    " + store);
          }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            updateDisplay();
          }
        });
      }
    }).start();
  }

  private void exportToFile() {
    synchronized (allItemsLock) {
      try {
        int count = 0;
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(Environment.getExternalStorageDirectory().getPath(), EXPORT_FILE_NAME)));
        try {
          for (Item item : allItems) {
            writer.write(item.exportToString());
            writer.write("\n");
            count++;
          }
        } finally {
          writer.close();
          if (DEBUG) {
            Log.d(LOG_TAG, "Exported " + count + " items");
          }
        }
      } catch (IOException e) {
        Log.e(LOG_TAG, "exportToFile caught", e);
      }
    }
  }
}
