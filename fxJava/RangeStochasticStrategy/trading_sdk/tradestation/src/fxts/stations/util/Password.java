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
 * Class is usable for cretaing password Preference property type.<br>
 * <br>
 * .<br>
 * <br>
 *
 * @Creation date (1/16/2004 7:41 PM)
 */
public class Password {
    private String msPassword;

    public Password(String asPassword) {
        msPassword = asPassword;
    }

    public String toString() {
        return msPassword;
    }
}