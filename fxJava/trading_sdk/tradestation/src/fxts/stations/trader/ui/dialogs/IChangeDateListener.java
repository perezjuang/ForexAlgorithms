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
package fxts.stations.trader.ui.dialogs;

import java.util.Date;

/**
 * Implementations of this interface listen changes of date contril they are registered in.
 * <br>
 *
 * @Creation date (10/15/2003 8:56 PM)
 */
public interface IChangeDateListener {
    /**
     * That method is called when date was changed.
     *
     * @param aOldDate previous date
     * @param aNewDate the new one
     */
    void onDateChange(Date aOldDate, Date aNewDate);
}
