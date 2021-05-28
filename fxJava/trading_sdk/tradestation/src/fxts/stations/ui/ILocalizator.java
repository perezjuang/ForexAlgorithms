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
package fxts.stations.ui;

import fxts.stations.util.ResourceManager;

/**
 * Interface that is used to support dynamic localization of UI elements.
 */
public interface ILocalizator {
    /**
     * Returns object that is localized by this localizator instance.
     */
    public Object getLocalizedObject();

    /**
     * Is called when current locale is changed.
     *
     * @param aMan new resource manager
     */
    public void onChangeLocale(ResourceManager aMan);
}