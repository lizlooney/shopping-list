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

import java.util.Comparator;

/**
 * Class containing Item Comparators.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public final class ItemComparators {
  public static final Comparator<Item> SORT_FOR_PLANNING = new Comparator<Item>() {
    @Override
    public int compare(Item o1, Item o2) {
      int result = o1.getCategory().compareToIgnoreCase(o2.getCategory());
      if (result != 0) {
        return result;
      }
      result = o1.getDescription().compareToIgnoreCase(o2.getDescription());
      if (result != 0) {
        return result;
      }
      return o1.getId() - o2.getId();
    }
  };

  public static Comparator<Item> sortForShopping(final String storeFilter) {
    return new Comparator<Item>() {
      @Override
      public int compare(Item o1, Item o2) {
        ItemState state1 = o1.getState();
        ItemState state2 = o2.getState();
        // Needed items come first.
        if (state1 != state2) {
          if (state1 == ItemState.NEED) {
            return -1;
          }
          if (state2 == ItemState.NEED) {
            return 1;
          }
        }

        if (storeFilter != null) {
          int result = Aisles.compareAisles(o1.getAisle(storeFilter), o2.getAisle(storeFilter));
          if (result != 0) {
            return result;
          }
        }

        int result = o1.getDescription().compareToIgnoreCase(o2.getDescription());
        if (result != 0) {
          return result;
        }

        return o1.getId() - o2.getId();
      }
    };
  }
}
