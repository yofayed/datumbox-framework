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
package com.datumbox.framework.machinelearning.common.validation;

import com.datumbox.framework.machinelearning.classification.OrdinalRegression;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class OrdinalRegressionValidation extends ClassifierValidation<OrdinalRegression.ModelParameters, OrdinalRegression.TrainingParameters, OrdinalRegression.ValidationMetrics> {
    
    
    public OrdinalRegressionValidation() {
        super();
    }
        
    @Override
    public OrdinalRegression.ValidationMetrics calculateAverageValidationMetrics(List<OrdinalRegression.ValidationMetrics> validationMetricsList) {
        OrdinalRegression.ValidationMetrics avgValidationMetrics = super.calculateAverageValidationMetrics(validationMetricsList);
        if(avgValidationMetrics==null) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples
        for(OrdinalRegression.ValidationMetrics vmSample : validationMetricsList) {
            avgValidationMetrics.setCountRSquare(avgValidationMetrics.getCountRSquare() + vmSample.getCountRSquare()/k);
            avgValidationMetrics.setSSE(avgValidationMetrics.getSSE() + vmSample.getSSE()/k);
        }
        
        return avgValidationMetrics;
    }
}
