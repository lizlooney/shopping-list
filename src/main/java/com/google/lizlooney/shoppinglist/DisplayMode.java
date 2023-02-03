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

/**
 * Enum for the different display modes.
 *
 * @author lizlooney@gmail.com (Liz Looney)
 */
public enum DisplayMode {
  /**
   * When I am planning what I need, all items are displayed. Items are sorted by category and the
   * category column is displayed.
   */
  PLANNING,

  /**
   * When I am shopping, items with state DONT_NEED are omitted. Items are sorted by aisle and the
   * aisle column is displayed.
   */
  SHOPPING;

  public static DisplayMode fromString(Context context, String s) {
    if (s.equals(context.getString(R.string.DisplayModePlanning))) {
      return DisplayMode.PLANNING;
    }
    if (s.equals(context.getString(R.string.DisplayModeShopping))) {
      return DisplayMode.SHOPPING;
    }
    throw new IllegalArgumentException();
  }

  public static String toString(Context context, DisplayMode displayMode) {
    switch (displayMode) {
      default:
      case PLANNING:
        return context.getString(R.string.DisplayModePlanning);
      case SHOPPING:
        return context.getString(R.string.DisplayModeShopping);
    }
  }

  public static String[] getStrings(Context context) {
    return new String[] {
      DisplayMode.toString(context, DisplayMode.PLANNING),
      DisplayMode.toString(context, DisplayMode.SHOPPING)
    };
  }
}
