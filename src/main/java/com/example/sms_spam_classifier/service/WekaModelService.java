package com.example.sms_spam_classifier.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.*;
import weka.filters.unsupervised.attribute.StringToWordVector;

@Service
public class WekaModelService {

	@Value("${model.file:./model/weka.model}")
	private String modelFilePath;

	private FilteredClassifier classifier;

	@PostConstruct
	  public void init() {
	    try {
	      File f = new File(modelFilePath);
	      if (f.exists()) {
	        classifier = (FilteredClassifier) SerializationHelper.read(modelFilePath);
	        System.out.println("Loaded model from " + modelFilePath);
	      } else {
	        trainAndSaveModel();
	        System.out.println("Trained and saved model to " + modelFilePath);
	      }
	    } catch (Exception e) {
	      throw new RuntimeException(e);
	    }
	}
	
	public synchronized void trainAndSaveModel() throws Exception {
	    Instances train = buildTrainingInstances();
	    StringToWordVector filter = new StringToWordVector();
	    filter.setAttributeIndices("first"); // apply to text attribute
	    filter.setTFTransform(true);
	    filter.setIDFTransform(true);
	    filter.setLowerCaseTokens(true);
	    filter.setOutputWordCounts(true);
	    filter.setWordsToKeep(1000);

	    FilteredClassifier fc = new FilteredClassifier();
	    fc.setFilter(filter);
	    fc.setClassifier(new NaiveBayes());
	    fc.buildClassifier(train);
	    File f = new File(modelFilePath);
	    f.getParentFile().mkdirs();
	    SerializationHelper.write(modelFilePath, fc);

	    this.classifier = fc;
	  }
	
	private Instances buildTrainingInstances() {
	    ArrayList<Attribute> attrs = new ArrayList<>();
	    attrs.add(new Attribute("text", (ArrayList<String>) null));
	    ArrayList<String> classVals = new ArrayList<>(Arrays.asList("ham", "spam"));
	    attrs.add(new Attribute("class", classVals));

	    Instances data = new Instances("sms", attrs, 0);
	    data.setClassIndex(data.numAttributes() - 1);

	    // Very small sample training set (you should replace with larger dataset)
	    addInstance(data, "Are we meeting today?", "ham");
	    addInstance(data, "Don't forget the documents", "ham");
	    addInstance(data, "Win cash prize now", "spam");
	    addInstance(data, "Congratulations! You have won a lottery", "spam");
	    addInstance(data, "Lunch at 1pm?", "ham");
	    addInstance(data, "Free entry in weekly competition, claim prize", "spam");
	    addInstance(data, "Can you call me back?", "ham");
	    return data;
	}
	
	private void addInstance(Instances data, String text, String label) {
	    DenseInstance inst = new DenseInstance(2);
	    inst.setValue(data.attribute(0), data.attribute(0).addStringValue(text));
	    inst.setValue(data.attribute(1), label);
	    data.add(inst);
	  }
	
	public String classify(String text) throws Exception {
	    ArrayList<Attribute> attrs = new ArrayList<>();
	    attrs.add(new Attribute("text", (ArrayList<String>) null));
	    ArrayList<String> classVals = new ArrayList<>(Arrays.asList("ham", "spam"));
	    attrs.add(new Attribute("class", classVals));

	    Instances data = new Instances("test", attrs, 0);
	    data.setClassIndex(data.numAttributes() - 1);

	    DenseInstance inst = new DenseInstance(data.numAttributes());
	    inst.setValue(data.attribute(0), data.attribute(0).addStringValue(text));
	    
	    // Attach instance to dataset
	    inst.setDataset(data);
	    
	   
	    data.add(inst);

	    double idx = classifier.classifyInstance(data.instance(0));
	    return data.classAttribute().value((int) idx);
	  }
	

}
