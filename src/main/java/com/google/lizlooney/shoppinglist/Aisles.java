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
 * Class maintaining the set of all aisles.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class Aisles {
  private final Object lock = new Object();
  private final TreeSet<String> aisles = new TreeSet<>();

  public void loadAisles(List<Item> items) {
    synchronized (lock) {
      // Loads the aisles TreeSet with the aisles that are used in the items.
      clear();
      for (Item item: items) {
        add(item);
      }
    }
  }

  public void clear() {
    synchronized (lock) {
      aisles.clear();
    }
  }

  public void add(Item newItem) {
    synchronized (lock) {
      for (String aisle : newItem.getAisles()) {
        aisles.add(aisle);
      }
    }
  }

  public String[] getAislesArray() {
    synchronized (lock) {
      return aisles.toArray(new String[0]);
    }
  }

  public static int compareAisles(String a1, String a2) {
    // Numbered aisles come first, in numerical order.
    Integer n1 = null;
    try {
      n1 = Integer.parseInt(a1);
    } catch (NumberFormatException e) {
    }
    Integer n2 = null;
    try {
      n2 = Integer.parseInt(a2);
    } catch (NumberFormatException e) {
    }
    if (n1 != null) {
      if (n2 != null) {
        return n1 - n2;
      } else {
        return -1;
      }
    } else {
      if (n2 != null) {
        return 1;
      } else {
        return a1.compareToIgnoreCase(a2);
      }
    }
  }
}
