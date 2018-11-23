/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.core.selector;
/**
 * Implementation of a convert objects and comparing. Here we compare two objects and evaluate to a double value.
 */
public class ConvertAndCompare {

    public double cnovert (Object x , Object y) {
        String s = String.valueOf(x);
        String s1 = String.valueOf(y);
        double value = s.compareTo(s1);
        return value;
    }

    public double convertToDouble(Object leftValue) {
        String s = String.valueOf(leftValue);
        double x = Double.parseDouble(s);
        return  x;
    }

    public boolean convertToBoolean(Object value) {
        String s = String.valueOf(value);
        Boolean b = Boolean.parseBoolean(s);
        return  b;
    }
}

