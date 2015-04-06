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
package com.datumbox.tests.utilities;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.persistentstorage.inmemory.InMemoryConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.mapdb.MapDBConfiguration;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.configuration.TestConfiguration;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TestUtils {
    
    public static void assertDoubleDataTable2D(DataTable2D expResult, DataTable2D result) {
        for (Object key1 : result.keySet()) {
            for (Object key2 : result.get(key1).keySet()) {
                
                double v1 = TypeConversions.toDouble(expResult.get2d(key1, key2));
                double v2 = TypeConversions.toDouble(result.get2d(key1, key2));
                
                assertEquals(v1, v2, TestConfiguration.DOUBLE_ACCURACY_HIGH);
            }
        }
    }
    
    public static void assetDoubleAssociativeArray(AssociativeArray expResult, AssociativeArray result) {
        
        for (Object key : result.keySet()) {
            double v1 = expResult.getDouble(key);
            double v2 = result.getDouble(key);

            assertEquals(v1, v2, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }
    
    public static URI getRemoteFile(URL url) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("datumbox", ".tmp");
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(url.openStream()));
             DataOutputStream out = new DataOutputStream(new FileOutputStream(tmpFile))) {
            
            int n;
            byte[] buffer = new byte[4096];
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            
            return tmpFile.toURI();
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static final synchronized void log(Class klass, String msg) {
        LoggerFactory.getLogger(klass).info(msg);
    }

    public static DatabaseConfiguration getDBConfig() {
        if (TestConfiguration.PERMANENT_STORAGE.equals(InMemoryConfiguration.class)) {
            InMemoryConfiguration c = new InMemoryConfiguration();
            
            //Path of Output folder
            c.setOutputFolder("./");
            
            return c;
        } 
        else if (TestConfiguration.PERMANENT_STORAGE.equals(MapDBConfiguration.class)) {
            MapDBConfiguration c = new MapDBConfiguration();
            
            //Path of Output folder
            c.setOutputFolder("./");
            
            //Size of LRU cache. Zero turns off caching
            c.setCacheSize(10000);
            
            //Turns on/off the compression
            c.setCompression(true);
            
            //Turns on/off transactions
            c.setTransactions(false);
            
            return c;
        }
        return null;
    }
}
