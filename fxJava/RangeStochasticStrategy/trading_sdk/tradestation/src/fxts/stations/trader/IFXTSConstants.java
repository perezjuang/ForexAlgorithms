/*

 * Copyright 2006 FXCM LLC

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *      http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package fxts.stations.trader;

/**
 * Interface that is used for saving of all global constants of application.
 */
public interface IFXTSConstants {
    /**
     * Key string is applied to safe name of menu item (or name of frame) at the menu item (or menu).
     */
    public static final String MENUITEM_NAME = "menuitem_name";
    /**
     * Key string is applied to safe referrence to Locale at the menu item.
     */
    public static final String LOCALE_KEY = "locale";
    /**
     * Current version of application.
     */
    public static final String CURRENT_VERSION = "J3";
    /**
     * Rate`s dispersion. This value specifies in how times input value of rate
     * may differents from current value.
     */
    public static final int RATE_DISPERSION = 5;
    /**
     * Maximum value of the amount.
     */
    public static final long MAXIMUM_AMOUNT = 50000000;
}
