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

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class handling loading from and saving to storage.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class Storage {
  private static final String TAG_MAX_ITEM_ID = "MaxItemId";
  private static final String TAG_ITEM_PREFIX = "Item_";
  private static final String TAG_DISPLAY_MODE = "DisplayMode";
  private static final String TAG_STORE_FILTER = "StoreFilter";

  private final Gson gson;
  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor sharedPrefsEditor;

  private final Object lock = new Object();
  private int maxItemId;
  private final List<Integer> itemIdHoles = new ArrayList<>();

  public Storage(Gson gson) {
    this.gson = gson;
  }

  public void init(Context context) {
    sharedPreferences = context.getSharedPreferences("ShoppingList", Context.MODE_PRIVATE);
  }

  public void clear() {
    synchronized (lock) {
      maxItemId = 0;
      itemIdHoles.clear();
    }
  }

  /**
   * Loads the given list with the items from storage.
   */
  public void loadItems(List<Item> items) {
    synchronized (lock) {
      maxItemId = getInt(TAG_MAX_ITEM_ID);
      for (int id = 0; id <= maxItemId; id++) {
        Item item = loadItem(id);
        if (item != null) {
          items.add(item);
        } else {
          itemIdHoles.add(id);
        }
      }
    }
  }

  private Item loadItem(int id) {
    String json = getString(TAG_ITEM_PREFIX + id);
    if (json != null) {
      Item item = gson.fromJson(json, Item.class);
      item.setId(id);
      return item;
    }

    return null;
  }

  /**
   * Deletes the item with the given id from storage.
   */
  public void deleteItem(Item item) {
    synchronized (lock) {
      edit();
      int id = item.getId();
      removeValue(TAG_ITEM_PREFIX + id);
      if (id == maxItemId) {
        maxItemId = id - 1;
        putInt(TAG_MAX_ITEM_ID, maxItemId);
      } else {
        itemIdHoles.add(id);
      }
      commit();
    }
  }

  public int getUnusedItemId() {
    synchronized (lock) {
      int size = itemIdHoles.size();
      if (size > 0) {
        return itemIdHoles.remove(size - 1);
      }
      maxItemId++;
      edit();
      putInt(TAG_MAX_ITEM_ID, maxItemId);
      commit();
      return maxItemId;
    }
  }

  /**
   * Saves the given item to storage.
   */
  public void saveItem(Item item) {
    synchronized (lock) {
      edit();
      putString(TAG_ITEM_PREFIX + item.getId(), gson.toJson(item));
      commit();
    }
  }

  /**
   * Loads the display mode from storage.
   */
  public DisplayMode loadDisplayMode() {
    String s = getString(TAG_DISPLAY_MODE);
    if (s != null) {
      return DisplayMode.valueOf(s);
    }
    return DisplayMode.PLANNING;
  }

  /**
   * Saves the display mode to storage.
   */
  public void saveDisplayMode(DisplayMode displayMode) {
    edit();
    putString(TAG_DISPLAY_MODE, displayMode.toString());
    commit();
  }

  /**
   * Loads the store filter from storage.
   */
  public String loadStoreFilter() {
    return getString(TAG_STORE_FILTER);
  }

  /**
   * Saves the store filter to storage.
   */
  public void saveStoreFilter(String storeFilter) {
    edit();
    putString(TAG_STORE_FILTER, storeFilter);
    commit();
  }

  private int getInt(String tag) {
    return sharedPreferences.getInt(tag, 0);
  }

  private String getString(String tag) {
    return sharedPreferences.getString(tag, null);
  }

  private void putInt(String tag, int value) {
    sharedPrefsEditor.putInt(tag, value);
  }

  private void putString(String tag, String value) {
    sharedPrefsEditor.putString(tag, value);
  }

  private void removeValue(String tag) {
    sharedPrefsEditor.remove(tag);
  }

  private void edit() {
    sharedPrefsEditor = sharedPreferences.edit();
  }

  private void commit() {
    sharedPrefsEditor.commit();
    sharedPrefsEditor = null;
  }
}
