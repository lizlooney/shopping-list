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
  private transient int id;
  private String description = "";
  private String category = "";
  private ItemState state = ItemState.NEED;
  private long lastPurchased;
  private boolean autoDelete;
  private final Map<String, String> storeAisles = new TreeMap<>();

  public void setId(int id) {
    this.id = id;
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

  public void setLastPurchased(long lastPurchased) {
    this.lastPurchased = lastPurchased;
  }

  public long getLastPurchased() {
    return lastPurchased;
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

  void exportToIntent(Intent intent) {
    intent.putExtra(EditItem.ITEM_DESCRIPTION, description);
    intent.putExtra(EditItem.ITEM_CATEGORY, category);
    intent.putExtra(EditItem.ITEM_LAST_PURCHASED, lastPurchased);
    intent.putExtra(EditItem.ITEM_AUTO_DELETE, autoDelete);

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
