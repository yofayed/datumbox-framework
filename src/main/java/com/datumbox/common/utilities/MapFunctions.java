/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.common.utilities;

import com.datumbox.common.dataobjects.AssociativeArray;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author bbriniotis
 */
public class MapFunctions {
    
     /**
     * Selects the key-value entry with the largest value.
     * 
     * @param keyValueMap
     * @return 
     */
    public static Map.Entry<Object, Object> selectMaxKeyValue(AssociativeArray keyValueMap) {
        Double maxValue=Double.NEGATIVE_INFINITY;
        Object maxValueKey = null;
        
        for(Map.Entry<Object, Object> entry : keyValueMap.entrySet()) {
            Double value = TypeConversions.toDouble(entry.getValue());
            if(value!=null && value>maxValue) {
                maxValue=value;
                maxValueKey=entry.getKey();
            }
        }
        
        return new AbstractMap.SimpleEntry<>(maxValueKey, keyValueMap.get(maxValueKey));
    }
    
    public static Map.Entry<Object, Double> selectMaxKeyValue(Map<Object, Double> keyValueMap) {
        Double maxValue=Double.NEGATIVE_INFINITY;
        Object maxValueKey = null;
        
        for(Map.Entry<Object, Double> entry : keyValueMap.entrySet()) {
            Double value = entry.getValue();
            if(value!=null && value>maxValue) {
                maxValue=value;
                maxValueKey=entry.getKey();
            }
        }
        
        return new AbstractMap.SimpleEntry<>(maxValueKey, keyValueMap.get(maxValueKey));
    }
    
     /**
     * Selects the key-value entry with the smallest value.
     * 
     * @param keyValueMap
     * @return 
     */
    public static Map.Entry<Object, Object> selectMinKeyValue(AssociativeArray keyValueMap) {
        Double minValue=Double.POSITIVE_INFINITY;
        Object minValueKey = null;
        
        for(Map.Entry<Object, Object> entry : keyValueMap.entrySet()) {
            Double value = TypeConversions.toDouble(entry.getValue());
            if(value!=null && value<minValue) {
                minValue=value;
                minValueKey=entry.getKey();
            }
        }
        
        return new AbstractMap.SimpleEntry<>(minValueKey, keyValueMap.get(minValueKey));
    }
    
    public static Map.Entry<Object, Double> selectMinKeyValue(Map<Object, Double> keyValueMap) {
        Double minValue=Double.POSITIVE_INFINITY;
        Object minValueKey = null;
        
        for(Map.Entry<Object, Double> entry : keyValueMap.entrySet()) {
            Double value = entry.getValue();
            if(value!=null && value<minValue) {
                minValue=value;
                minValueKey=entry.getKey();
            }
        }
        
        return new AbstractMap.SimpleEntry<>(minValueKey, keyValueMap.get(minValueKey));
    }
    
    /**
     * Sorts by Key a Map in ascending order. 
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByKeyAscending(Map<K, V> map) {
        return sortNumberMapByKeyAscending(map.entrySet());
    }
    
    /**
     * Sorts by Key a Map in ascending order. 
     * 
     * @param <K>
     * @param <V>
     * @param entrySet
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByKeyAscending(Set<Map.Entry<K, V>> entrySet) {
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(entrySet);
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> a, Map.Entry<K, V> b){
              Double va = TypeConversions.toDouble(a.getKey());
              Double vb = TypeConversions.toDouble(b.getKey());
              return va.compareTo(vb);
          }
        });
        
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
          sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
    
    
    /**
     * Sorts by Key a Map in descending order. 
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByKeyDescending(Map<K, V> map) {
        return sortNumberMapByKeyDescending(map.entrySet());
    }
    
    /**
     * Sorts by Key a Map in descending order. 
     * 
     * @param <K>
     * @param <V>
     * @param entrySet
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByKeyDescending(Set<Map.Entry<K, V>> entrySet) {
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(entrySet);
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> a, Map.Entry<K, V> b){
              Double va = TypeConversions.toDouble(a.getKey());
              Double vb = TypeConversions.toDouble(b.getKey());
              return -va.compareTo(vb);
          }
        });
        
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
          sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
    /**
     * Sorts by Value a Map in ascending order. 
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByValueAscending(Map<K, V> map) {
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> a, Map.Entry<K, V> b){
              Double va = TypeConversions.toDouble(a.getValue());
              Double vb = TypeConversions.toDouble(b.getValue());
              return va.compareTo(vb);
          }
        });
        
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
          sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
    /**
     * Sorts by Value a Map in descending order. 
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByValueDescending(Map<K, V> map) {
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> a, Map.Entry<K, V> b){
              Double va = TypeConversions.toDouble(a.getValue());
              Double vb = TypeConversions.toDouble(b.getValue());
              return -va.compareTo(vb);
          }
        });
        
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
          sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
    /**
     * Sorts by Value a Associative Array in ascending order. 
     * 
     * @param associativeArray
     * @return 
     */
    public static AssociativeArray sortAssociativeArrayByValueAscending(AssociativeArray associativeArray) {
        ArrayList<Map.Entry<Object, Object>> entries = new ArrayList<>(associativeArray.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Object,Object>>() {
          @Override
          public int compare(Map.Entry<Object, Object> a, Map.Entry<Object, Object> b){
              Double va = TypeConversions.toDouble(a.getValue());
              Double vb = TypeConversions.toDouble(b.getValue());
              return va.compareTo(vb);
          }
        });
        
        AssociativeArray sortedAssociativeArray = new AssociativeArray();
        for (Map.Entry<Object, Object> entry : entries) {
          sortedAssociativeArray.put(entry.getKey(), entry.getValue());
        }
        
        return sortedAssociativeArray;
    }
    
    /**
     * Sorts by Value a Associative Array in descending order. 
     * 
     * @param associativeArray
     * @return 
     */
    public static AssociativeArray sortAssociativeArrayByValueDescending(AssociativeArray associativeArray) {
        ArrayList<Map.Entry<Object, Object>> entries = new ArrayList<>(associativeArray.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Object,Object>>() {
          @Override
          public int compare(Map.Entry<Object, Object> a, Map.Entry<Object, Object> b){
              Double va = TypeConversions.toDouble(a.getValue());
              Double vb = TypeConversions.toDouble(b.getValue());
              return -va.compareTo(vb);
          }
        });
        
        AssociativeArray sortedAssociativeArray = new AssociativeArray();
        for (Map.Entry<Object, Object> entry : entries) {
          sortedAssociativeArray.put(entry.getKey(), entry.getValue());
        }
        
        return sortedAssociativeArray;
    }
}
