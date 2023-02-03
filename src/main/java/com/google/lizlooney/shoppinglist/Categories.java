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

import java.util.List;
import java.util.TreeSet;

/**
 * Class maintaining the set of all categories.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class Categories {
  private final Object lock = new Object();
  private final TreeSet<String> categories = new TreeSet<>();

  public void loadCategories(List<Item> items) {
    synchronized (lock) {
      // Loads the categories TreeSet with the categories that are used in the items.
      clear();
      for (Item item: items) {
        add(item);
      }
    }
  }

  public void clear() {
    synchronized (lock) {
      categories.clear();
    }
  }

  public void add(Item item) {
    synchronized (lock) {
      categories.add(item.getCategory());
    }
  }

  public String[] getCategoriesArray() {
    synchronized (lock) {
      return categories.toArray(new String[0]);
    }
  }
}
