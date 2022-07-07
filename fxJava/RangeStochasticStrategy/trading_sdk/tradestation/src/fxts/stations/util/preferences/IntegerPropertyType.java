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

import fxts.stations.util.UserPreferences;

public class IntegerPropertyType extends APropertyType {
    private static class Validator implements IValidator {
        public Object validate(String asValue) throws ValidationException {
            try {
                return Integer.valueOf(asValue);
            } catch (Exception e) {
                throw new ValidationException("IDS_NUMBER_FORMAT_INVALID");
            }
        }
    }

    public IntegerPropertyType() {
        setValidator(new Validator());
    }

    public AValueEditorPanel getRenderer(Object aValue) {
        return new SimpleEditorPanel(aValue, this);
    }

    public String toString(Object aValue) {
        return UserPreferences.getStringValue((Integer) aValue);
    }

    public String toStringValue(Object aValue) {
        return aValue.toString();
    }
}