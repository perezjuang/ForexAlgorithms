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
package fxts.stations.util.preferences;

import java.awt.*;

/**
 * Interface of editor for abstract data type.
 */
public interface IEditor {
    /**
     * Returns component for placing on editor dialog.
     */
    Component getComponent();

    /**
     * Returns current value.
     */
    Object getValue();

    /**
     * Sets new value to editor.
     *
     * @param aValue new value
     */
    void setValue(Object aValue);
}