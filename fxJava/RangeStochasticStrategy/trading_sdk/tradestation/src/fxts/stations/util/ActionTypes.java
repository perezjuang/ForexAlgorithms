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
package fxts.stations.util;

/**
 * Enumeration of actions types for geting them through defferent interfaces.<br>
 * <br>
 * .<br>
 * <br>
 *
 * @Creation date (11/4/2003 7:54 PM)
 */
public class ActionTypes {
    public static final ActionTypes CLOSE_POSITION = new ActionTypes();
    public static final ActionTypes CREATE_ENTRY_ORDER = new ActionTypes();
    public static final ActionTypes CREATE_MARKET_ORDER = new ActionTypes();
    public static final ActionTypes REQUEST_FOR_QUOTE = new ActionTypes();
    public static final ActionTypes LOGIN = new ActionTypes();
    public static final ActionTypes REMOVE_ENTRY_ORDER = new ActionTypes();
    public static final ActionTypes REPORT = new ActionTypes();
    public static final ActionTypes SET_STOP_LIMIT = new ActionTypes();
    public static final ActionTypes SET_STOP_LIMIT_ORDER = new ActionTypes();
    public static final ActionTypes UPDATE_ENTRY_ORDER = new ActionTypes();

    private ActionTypes() {
    }
}
