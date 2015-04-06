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
package com.datumbox.applications.nlp;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.featureselection.CategoricalFeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.bases.wrappers.BaseWrapper;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.framework.utilities.dataset.DatasetBuilder;
import com.datumbox.framework.utilities.text.extractors.TextExtractor;
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TextClassifier extends BaseWrapper<TextClassifier.ModelParameters, TextClassifier.TrainingParameters>  {
    
    public static class ModelParameters extends BaseWrapper.ModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    public static class TrainingParameters extends BaseWrapper.TrainingParameters<DataTransformer, FeatureSelection, BaseMLmodel> {

        //primitives/wrappers
        private Integer kFolds = 5;
        
        //Classes

        private Class<? extends TextExtractor> textExtractorClass;

        //Parameter Objects

        private TextExtractor.Parameters textExtractorTrainingParameters;

        //Field Getters/Setters

        public Integer getkFolds() {
            return kFolds;
        }

        public void setkFolds(Integer kFolds) {
            this.kFolds = kFolds;
        }

        public Class<? extends TextExtractor> getTextExtractorClass() {
            return textExtractorClass;
        }

        public void setTextExtractorClass(Class<? extends TextExtractor> textExtractorClass) {
            this.textExtractorClass = textExtractorClass;
        }

        public TextExtractor.Parameters getTextExtractorTrainingParameters() {
            return textExtractorTrainingParameters;
        }

        public void setTextExtractorTrainingParameters(TextExtractor.Parameters textExtractorTrainingParameters) {
            this.textExtractorTrainingParameters = textExtractorTrainingParameters;
        }

    }

    
    
    
    public TextClassifier(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, TextClassifier.ModelParameters.class, TextClassifier.TrainingParameters.class);
    }
    
    @Deprecated
    @Override
    public void fit(Dataset trainingData, TrainingParameters trainingParameters) {
        throw new UnsupportedOperationException("This version of fit() is not supported."); 
    }
    
    public void fit(Map<Object, URI> dataset, TrainingParameters trainingParameters) { 
        initializeTrainingConfiguration(trainingParameters);
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        
        //build trainingDataset
        Dataset trainingDataset = DatasetBuilder.parseFromTextFiles(dataset, textExtractor, knowledgeBase.getDbConf());
        
        _fit(trainingDataset);
        
        //store database
        knowledgeBase.save();
    }
    
    @Override
    protected void _fit(Dataset trainingDataset) {
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            dataTransformer = DataTransformer.<DataTransformer>newInstance(dtClass, dbName, dbConf);
            dataTransformer.fit_transform(trainingDataset, trainingParameters.getDataTransformerTrainingParameters());
        }
        
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            featureSelection = FeatureSelection.<FeatureSelection>newInstance(fsClass, dbName, dbConf);
            FeatureSelection.TrainingParameters featureSelectionParameters = trainingParameters.getFeatureSelectionTrainingParameters();
            if(CategoricalFeatureSelection.TrainingParameters.class.isAssignableFrom(featureSelectionParameters.getClass())) {
                ((CategoricalFeatureSelection.TrainingParameters)featureSelectionParameters).setIgnoringNumericalFeatures(false); //this should be turned off in feature selection
            }
            //find the most popular features
            featureSelection.fit(trainingDataset, trainingParameters.getFeatureSelectionTrainingParameters());   

            //remove unnecessary features
            featureSelection.transform(trainingDataset);
        }
        
        //initialize mlmodel
        mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName, dbConf); 
        
        
        int k = trainingParameters.getkFolds();
                
        //call k-fold cross validation and get the average accuracy
        BaseMLmodel.ValidationMetrics averageValidationMetrics = (BaseMLmodel.ValidationMetrics) mlmodel.kFoldCrossValidation(trainingDataset, trainingParameters.getMLmodelTrainingParameters(), k);

        //train the mlmodel on the whole dataset
        mlmodel.fit(trainingDataset, trainingParameters.getMLmodelTrainingParameters());

        //set its ValidationMetrics to the average VP from k-fold cross validation
        mlmodel.setValidationMetrics(averageValidationMetrics);
        
        
        if(transformData) {
            dataTransformer.denormalize(trainingDataset); //optional denormalization
        }
    }
    
    public List<Object> predict(URI datasetURI) {
        List<String> text = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(datasetURI)), "UTF8"))) {
            //read strings one by one
            for(String line; (line = br.readLine()) != null; ) {
                text.add(line);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        } 
        
        return predict(text);
    }
    
    public List<Object> predict(List<String> text) {
        
        Dataset predictedDataset = getPredictions(text);
        
        //extract responses
        List<Object> predictedClasses = new LinkedList<>();
        for(Integer rId : predictedDataset) {
            Record r = predictedDataset.get(rId);
            predictedClasses.add(r.getYPredicted());
        }
        predictedDataset = null;
        
        return predictedClasses;
    }
    
    public List<AssociativeArray> predictProbabilities(URI datasetURI) {
        List<String> text = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(new File(datasetURI)))) {
            //read strings one by one
            for(String line; (line = br.readLine()) != null; ) {
                text.add(line);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        } 
        
        return predictProbabilities(text);
    }
    
    public List<AssociativeArray> predictProbabilities(List<String> text) {
        
        Dataset predictedDataset = getPredictions(text);
        
        //extract responses
        List<AssociativeArray> predictedClassProbabilities = new LinkedList<>();
        for(Integer rId : predictedDataset) {
            Record r = predictedDataset.get(rId);
            predictedClassProbabilities.add(r.getYPredictedProbabilities());
        }
        predictedDataset = null;
        
        return predictedClassProbabilities;
    }
    
    public BaseMLmodel.ValidationMetrics validate(Map<Object, URI> dataset) {  
        
        //ensure db loaded
        knowledgeBase.load();
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        

        //build the testDataset
        Dataset testDataset = DatasetBuilder.parseFromTextFiles(dataset, textExtractor, dbConf);
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = DataTransformer.<DataTransformer>newInstance(dtClass, dbName, dbConf);
            }        
            
            dataTransformer.transform(testDataset);
        }

        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelection==null) {
                featureSelection = FeatureSelection.<FeatureSelection>newInstance(fsClass, dbName, dbConf);
            }

            //remove unnecessary features
            featureSelection.transform(testDataset);
        }
        
        
        //initialize mlmodel
        if(mlmodel==null) {
            mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName, dbConf); 
        }
        
        //call predict of the mlmodel for the new dataset
        BaseMLmodel.ValidationMetrics vm = mlmodel.validate(testDataset);
        
        if(transformData) {
            dataTransformer.denormalize(testDataset); //optional denormization
        }
        
        return vm;
    }
    
    public BaseMLmodel.ValidationMetrics getValidationMetrics() {
        if(mlmodel==null) {
            validate(new HashMap<>()); //this forces the loading of the algorithm
        }
        BaseMLmodel.ValidationMetrics vm =  mlmodel.getValidationMetrics();
        
        return vm;
    }
    
    
    private Dataset getPredictions(List<String> text) {
        
        //ensure db loaded
        knowledgeBase.load();
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        
        //build the newDataset
        Dataset newData = new Dataset(dbConf);
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        
        //loop through every line of the text array
        for(String line : text) {
            //extract features of the string and add every keyword combination in X map
            Record r = new Record(new AssociativeArray(textExtractor.extract(StringCleaner.clear(line))), null);
            
            //add each example in the newData
            newData.add(r); 
        }
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = DataTransformer.<DataTransformer>newInstance(dtClass, dbName, dbConf);
            }        
            dataTransformer.transform(newData);
        }
        
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelection==null) {
                featureSelection = FeatureSelection.<FeatureSelection>newInstance(fsClass, dbName, dbConf);
            }

            //remove unnecessary features
            featureSelection.transform(newData);
        }
        
        
        //initialize mlmodel
        if(mlmodel==null) {
            Class mlClass = trainingParameters.getMLmodelClass();
            mlmodel = BaseMLmodel.<BaseMLmodel>newInstance(mlClass, dbName, dbConf); 
        }
        
        //call predict of the mlmodel for the new dataset
        mlmodel.predict(newData);
        
        if(transformData) {
            dataTransformer.denormalize(newData); //optional denormization
        }
        
        return newData;
    }
    

    
    
}
