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

/**
 * APropertyType
 * This abstract class is base for all used types of data.
 *
 * @Creation date (10/24/2003 1:36 PM)
 */
public abstract class APropertyType {
    /**
     * Optional property of type. It not used for simple types.
     */
    private IEditor mEditor;
    /**
     * Optional property of type. It not used for that types
     * which are got from special editors.
     */
    private IValidator mValidator;
    /**
     * Obligatory property of type
     */
    private String msName;

    /* Constructor */

    /**
     * Creates new type of data.
     */
    protected APropertyType() {
    }

    /**
     * Returns editor.
     *
     * @return mEditor early set editor for this type of data.
     */
    public IEditor getEditor() {
        return mEditor;
    }

    /**
     * This method is called to create editor and prohibition validator
     * for this type of data.
     *
     * @param aEditor editor for this type
     */
    public final void setEditor(IEditor aEditor) {
        mValidator = null;
        mEditor = aEditor;
    }

    /**
     * Returns name
     *
     * @return msName early set name for this type of data.
     */
    public String getName() {
        return msName;
    }

    /**
     * Returns renderer.
     *
     * @param aoValue Object, which type defines what renderer to return
     *
     * @return AValueEditorPanel special panel for displaying
     *         a given type of data in the list.
     */
    public abstract AValueEditorPanel getRenderer(Object aoValue);

    /**
     * Returns validator.
     *
     * @return mValidator early set validator for this type of data.
     */
    public IValidator getValidator() {
        return mValidator;
    }

    /* -- Public methods -- */

    /**
     * This method is called to create validator and prohibition
     * editor for this type of data.
     *
     * @param aValidator : what setting.
     */
    public final void setValidator(IValidator aValidator) {
        mEditor = null;
        mValidator = aValidator;
    }

    /**
     * Assign name a type of data.
     *
     * @param asName name a type of data
     */
    public void setName(String asName) {
        msName = asName;
    }

    /**
     * Returs string representation of value for storing.
     *
     * @param aValue Object, which should be present.
     *
     * @return String presentation which will be used for storage
     *
     * @throws
     */
    public abstract String toString(Object aValue);

    /**
     * Returs string representation of value for showing.
     *
     * @param aValue Object, which should be present.
     *
     * @return String presentation which will be used to draw on screen.
     */
    public abstract String toStringValue(Object aValue);

    /**
     * This method is used for transformation of value in string format
     * in its native value.
     *
     * @param asValue: string record of value.
     *
     * @return Object representing type of data.
     *
     * @throws ValidationException
     */
    public Object toValue(Object asValue) throws ValidationException {
        if (mValidator != null) {
            return mValidator.validate((String) asValue);
        }
        if (mEditor != null) {
            return asValue;
        }
        return null;
    }
}