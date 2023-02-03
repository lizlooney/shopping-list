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

import android.content.Intent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class representing an item on the shopping list.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class Item {
  private final int id;
  private String description;
  private String category;
  private ItemState state;
  private boolean autoDelete;
  private final Map<String, String> storeAisles;

  /**
   * Constructor used when reading an item from storage.
   */
  public Item(int id, String description, String category, Map<String, String> storeAisles, ItemState state) {
    this.id = id;
    this.description = description;
    this.category = category;
    this.storeAisles = new TreeMap<>(storeAisles);
    this.state = state;
  }

  /**
   * Constructor used when creating a new item.
   */
  public Item(int id) {
    this.id = id;
    description = "";
    category = "";
    storeAisles = new TreeMap<>();
    state = ItemState.NEED;
  }

  public int getId() {
    return id;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return category;
  }

  public void setState(ItemState state) {
    this.state = state;
  }

  public ItemState getState() {
    return state;
  }

  public void setAutoDelete(boolean autoDelete) {
    this.autoDelete = autoDelete;
  }

  public boolean getAutoDelete() {
    return autoDelete;
  }

  public void clearStoreAisles() {
    storeAisles.clear();
  }

  public void addStoreAisle(String store, String aisle) {
    storeAisles.put(store, aisle);
  }

  public boolean isMissingStore() {
    return storeAisles.isEmpty();
  }

  public boolean containsStore(String store) {
    return storeAisles.containsKey(store);
  }

  public Iterable<String> getStores() {
    return storeAisles.keySet();
  }

  public String getAisle(String store) {
    if (storeAisles.containsKey(store)) {
      return storeAisles.get(store);
    }
    // If this item isn't available from the store, return ~, which will sort after everything else.
    return "~";
  }

  public String getFirstAisle() {
    for (String aisle : storeAisles.values()) {
      return aisle;
    }
    return "";
  }

  public Iterable<String> getAisles() {
    return new HashSet<>(storeAisles.values());
  }

  public String exportToString() {
    StringBuilder sb = new StringBuilder();
    sb.append(description)
        .append("\t").append(category)
        .append("\t").append(state)
        .append("\t").append(autoDelete);
    for (Map.Entry<String, String> entry : storeAisles.entrySet()) {
      sb.append("\t").append(entry.getKey()).append("\t").append(entry.getValue());
    }
    return sb.toString();
  }

  public boolean importFromString(String s) {
    String[] parts = s.split("\t");
    int i = 0;
    String d = parts[i++].trim();
    if (d.length() == 0) {
      return false;
    }
    setDescription(d);

    setCategory(parts[i++].trim());
    setState(ItemState.valueOf(parts[i++]));
    setAutoDelete(Boolean.valueOf(parts[i++]));

    clearStoreAisles();
    while (i + 1 < parts.length) {
      String store = parts[i++].trim();
      String aisle = parts[i++].trim();
      if (store.length() > 0 && aisle.length() > 0) {
        addStoreAisle(store, aisle);
      }
    }

    return true;
  }

  void exportToIntent(Intent intent) {
    String descriptionString = description;
    intent.putExtra(EditItem.ITEM_DESCRIPTION, descriptionString);
    String categoryString = category;
    intent.putExtra(EditItem.ITEM_CATEGORY, categoryString);
    boolean autoDeleteBoolean = autoDelete;
    intent.putExtra(EditItem.ITEM_AUTO_DELETE, autoDeleteBoolean);

    List<String> stores = new ArrayList<>();
    List<String> aisles = new ArrayList<>();
    for (Map.Entry<String, String> entry : storeAisles.entrySet()) {
      stores.add(entry.getKey());
      aisles.add(entry.getValue());
    }
    String[] storesArray = stores.toArray(new String[0]);
    intent.putExtra(EditItem.ITEM_STORES, storesArray);
    String[] aislesArray = aisles.toArray(new String[0]);
    intent.putExtra(EditItem.ITEM_AISLES, aislesArray);
  }

  void importFromIntent(Intent intent) {
    setDescription(intent.getStringExtra(EditItem.ITEM_DESCRIPTION));
    setCategory(intent.getStringExtra(EditItem.ITEM_CATEGORY));
    setAutoDelete(intent.getBooleanExtra(EditItem.ITEM_AUTO_DELETE, false));

    String[] stores = intent.getStringArrayExtra(EditItem.ITEM_STORES);
    String[] aisles = intent.getStringArrayExtra(EditItem.ITEM_AISLES);
    clearStoreAisles();
    if (stores.length == aisles.length) {
      for (int i = 0; i < stores.length; i++) {
        String store = stores[i];
        String aisle = aisles[i];
        addStoreAisle(store, aisle);
      }
    }
  }
}
