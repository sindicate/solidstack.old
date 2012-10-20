/*
 * Copyright 2003-2007 the original author or authors.
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
package solidstack.script.java;

public class TypeConversionException extends ClassCastException {

    public TypeConversionException(Object objectToCast, Class classToCastTo) {
        super(makeMessage(objectToCast,classToCastTo));
    }

    public TypeConversionException(String string) {
        super(string);
    }

    private static String makeMessage(Object objectToCast, Class classToCastTo) {
       String classToCastFrom;
       if (objectToCast!=null) {
           classToCastFrom = objectToCast.getClass().getName();
       } else {
           objectToCast = "null";
           classToCastFrom = "null";
       }
       return "Cannot cast object '" + objectToCast + "' " +
              "with class '" + classToCastFrom + "' " +
              "to class '" + classToCastTo.getName() + "'";
    }

}
