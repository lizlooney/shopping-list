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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class maintaining the set of all stores.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public class Stores {
  private final Object lock = new Object();
  private final Set<String> stores = new TreeSet<>();
  private boolean missingStore;

  public void loadStores(List<Item> items) {
    synchronized (lock) {
      // Loads the stores TreeSet with the stores that are used in the items.
      clear();
      missingStore = false;
      for (Item item: items) {
        add(item);
      }
    }
  }

  public void clear() {
    synchronized (lock) {
      stores.clear();
      missingStore = false;
    }
  }

  public void add(Item item) {
    synchronized (lock) {
      if (item.isMissingStore()) {
        missingStore = true;
      }
      for (String store : item.getStores()) {
        stores.add(store);
      }
    }
  }

  public String[] getStoresArray() {
    synchronized (lock) {
      return stores.toArray(new String[0]);
    }
  }

  public Collection<String> getStoresForStoreFilter() {
    synchronized (lock) {
      List<String> storesForStoreFilter = new ArrayList<>();
      storesForStoreFilter.add(ShoppingList.STORE_FILTER_ALL);
      if (missingStore) {
        storesForStoreFilter.add(ShoppingList.STORE_FILTER_MISSING);
      }
      storesForStoreFilter.addAll(stores);
      return storesForStoreFilter;
    }
  }
}
