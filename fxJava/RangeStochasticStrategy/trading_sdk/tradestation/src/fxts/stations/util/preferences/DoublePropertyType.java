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

/**
 * Property type for double values.
 *
 * @Creation date (10/28/2003 1:36 PM)
 */
public class DoublePropertyType extends APropertyType {
    /**
     * Implementation of IValidator for boolean values.
     */
    private static class Validator implements IValidator {
        /**
         * Check validity of specified value.
         *
         * @param asValue specified value
         *
         * @return object of class Boolean or throws exception
         *
         * @throws ValidationException throws when value not valid.
         */
        public Object validate(String asValue) throws ValidationException {
            try {
                return Double.valueOf(asValue);
            } catch (Exception e) {
                throw new ValidationException("IDS_NUMBER_FORMAT_INVALID");
            }
        }
    }

    /**
     * Constructor.
     */
    public DoublePropertyType() {
        setValidator(new Validator());
    }

    /**
     * Returns renderer for specified value.
     *
     * @param aValue target value
     */
    public AValueEditorPanel getRenderer(Object aValue) {
        return new SimpleEditorPanel(aValue, this);
    }

    /**
     * Returns string representation of value for storing.
     *
     * @param aValue specified value
     */
    public String toString(Object aValue) {
        return UserPreferences.getStringValue((Double) aValue);
    }

    /**
     * Returns string representation of value for showing.
     *
     * @param aValue specified value
     */
    public String toStringValue(Object aValue) {
        return aValue.toString();
    }
}