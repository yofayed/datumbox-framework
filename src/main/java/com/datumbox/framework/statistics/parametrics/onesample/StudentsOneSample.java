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
package com.datumbox.framework.statistics.parametrics.onesample;

import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author bbriniotis
 */
public class StudentsOneSample {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * One Sample Mean Test for Students.
     * Requirements: Normal with unknown variance
     * 
     * @param xbar
     * @param n
     * @param H0mean
     * @param std
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testMean(double xbar, int n, double H0mean, double std, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=0 || std<=0) {
            throw new IllegalArgumentException();
        }

        //standardize it
        double t=(xbar-H0mean)/(std/Math.sqrt(n));

        boolean rejectH0=checkCriticalValue(t, n, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Autocorrelation test
     * 
     * @param pk
     * @param n
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testAutocorrelation(double pk, int n, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=0) {
            throw new IllegalArgumentException();
        }

        //standardize it
        double t=Math.sqrt(n)*pk;

        boolean rejectH0=checkCriticalValue(t, n, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param n
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    protected static boolean checkCriticalValue(double score, int n, boolean is_twoTailed, double aLevel) {
        double probability=ContinuousDistributions.StudentsCdf(score,n-1);

        boolean rejectH0=false;

        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2.0;
        }
        if(probability<=a || probability>=(1-a)) {
            rejectH0=true;
        }

        return rejectH0;
    }
}
