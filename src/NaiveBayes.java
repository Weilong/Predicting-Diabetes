import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
/**
 * COMP3308 Introduction to AI Assignment2
 * NaiveBayes.java
 * Purpose: This NaiveBayes class receives given dataset and calculate
 * the mean and standard deviation for each attributes of all the classes
 * respectively during the training and receiveds dataset for testing to do
 * test using probability density function during the testing.
 * @author Weilong Ding/Ruijun GU
 * @version 1.0 27/05/2012
 */
public class NaiveBayes
{
	private ArrayList<String> attributes;	//store names of the attributes
	private HashMap<String,ArrayList<Double>> meanMap;	//store means of attributes for each class
	private HashMap<String,ArrayList<Double>> sdMap;	//store std.dev of attributes for each class
	private HashMap<String,Double> classPriors;	//store Priors of each class
	private HashMap<String,Integer> numClass;	//store the number of each class
	
	/**
	 * This is the constructor of NaiveBayes class which will receive
	 * the list of attribute names as parameter and initialize all necessary
	 * data structure
	 * @param attritubes the list of attribute names
	 * @throws FileNotFoundException
	 */
	public NaiveBayes(ArrayList<String> attributes) throws FileNotFoundException
	{
		this.attributes = attributes;
		meanMap = new HashMap<String,ArrayList<Double>>();
		sdMap = new HashMap<String,ArrayList<Double>>();
		classPriors = new HashMap<String,Double>();
		numClass = new HashMap<String,Integer>();
	}
	
	/**
	 * Method addTrainingData receives an instance and process it
	 * to store the crude data in meanMap and SDMap for later use
	 * @param instance
	 */
	public void addTrainingData(String instance)
	{
		String className;
		String values[];
		
		values = instance.split(",");
		className = values[values.length-1];
		//if a class has  not been stored , store it 
		if(!meanMap.containsKey(className)&&!sdMap.containsKey(className))
		{
			meanMap.put(className, new ArrayList<Double>());
			sdMap.put(className, new ArrayList<Double>());
			classPriors.put(className, (double)0);
			numClass.put(className, 0);
			
			for (int i=0;i<values.length-1;i++)
			{
				meanMap.get(className).add((double)0);
				sdMap.get(className).add((double)0);
			}
		}
		//store the crude data of all the attributes of each class
		for (int i=0;i<values.length-1;i++)
		{
			meanMap.get(className).set(i, meanMap.get(className).get(i)+Double.parseDouble(values[i]));
			sdMap.get(className).set(i, sdMap.get(className).get(i)+Math.pow(Double.parseDouble(values[i]), 2));
		}
		numClass.put(className,numClass.get(className)+1);
	}
	
	/**
	 * Method training uses the crude data in meanMap and sdMap to calculate
	 * the mean and standard deviation for all attributes of each class.
	 * @throws IOException
	 */
	public void training() throws IOException
	{
		String className;
		int numInstance = 0;
		for (Iterator<String> it = numClass.keySet().iterator();it.hasNext();)
			numInstance+=numClass.get(it.next());
		//calculate the priors of each class
		for (Iterator<String> it = numClass.keySet().iterator();it.hasNext();)
		{
			className = it.next();
			classPriors.put(className, numClass.get(className)/(double)numInstance);
		}
		//calculate means of all attributes for each class
		for (Iterator<String> it = meanMap.keySet().iterator();it.hasNext();)
		{
			className = it.next();
			ArrayList<Double> list = meanMap.get(className);
			for (int i=0;i<list.size();i++)
			{
				list.set(i, list.get(i)/numClass.get(className));			
			}
		}
		//calculate std.dev of all attributes for each class		
		for (Iterator<String> it = sdMap.keySet().iterator();it.hasNext();)
		{
			className = it.next();
			ArrayList<Double> list = sdMap.get(className);
			for (int i=0;i<list.size();i++)
			{
				double tmpSD = Math.sqrt(list.get(i)/numClass.get(className)-Math.pow(meanMap.get(className).get(i),2));
				list.set(i, tmpSD);
			}
		}
	}
	
	/**
	 * Method testing receives an instance and classify it using
	 * probability density function where the mean and std.dev are
	 * from training. return the testing result
	 * @param instance the instance for classifying
	 * @return true if classification is correct otherwise false.
	 */
	public boolean testing(String instance)
	{
		String correctClass = null;
		String testClass = null;
		String values[];
		values = instance.split(",");
		correctClass = values[values.length-1];
		double likelihood,maxProb=0;
		Iterator<String> meanIt = meanMap.keySet().iterator();
		Iterator<String> sdIt = sdMap.keySet().iterator();
		/*
		 * calculate the probability of each class and compare
		 * choose the class with greater probabilty as the classified
		 * result
		 */
		while(meanIt.hasNext()&&sdIt.hasNext())
		{
			String className = meanIt.next();
			likelihood=1;
			ArrayList<Double> meanList = meanMap.get(className);
			ArrayList<Double> sdList = sdMap.get(className);	
			//calculate the likelihood of each attribute and multiply them together
			for (int i=0;i<meanList.size();i++)
				likelihood*= pdf(Double.parseDouble(values[i]),meanList.get(i),sdList.get(i));
			likelihood*=classPriors.get(className);	//multiply with class prior
			//get the maximum probability and the class with this probabilty
			if (likelihood>maxProb)
			{
				maxProb = likelihood;
				testClass = className;
			}
		}
		return testClass.equals(correctClass);
	}
	
	/**
	 * reset the NaiveBayes instance
	 */
	public void reset()
	{
		meanMap.clear();
		sdMap.clear();
		numClass.clear();
		classPriors.clear();
	}
	
	/**
	 * Normal Probability Density Function
	 * @param x
	 * @param mean mean(or expected value)
	 * @param sd standard deviation
	 * @return
	 */
	private double pdf(double x,double mean,double sd)
	{
		return 1/(sd*Math.sqrt(2*Math.PI))*Math.exp(-Math.pow(x-mean, 2)/(2*Math.pow(sd, 2)));
	}
	
	/**
	 * @override
	 * Override toString method.
	 * Print out the training result of Naive Bayes in certain format
	 */
	public String toString()
	{
		System.out.println("Mean table:");
		System.out.println();
		for (String attribute:attributes)
			System.out.printf("%17s",attribute);
		System.out.println();
		for (Iterator<String> it = meanMap.keySet().iterator();it.hasNext();)
		{
			String className = it.next();
			ArrayList<Double> list = meanMap.get(className);
			for (double value:list)
				System.out.printf("%17.4f",value);
			System.out.printf("%17s",className);
			System.out.println();
		}
		System.out.println();
		
		System.out.println("Standard Deviation table:");
		System.out.println();
		for (String attribute:attributes)
			System.out.printf("%17s",attribute);
		System.out.println();
		for (Iterator<String> it = sdMap.keySet().iterator();it.hasNext();)
		{
			String className = it.next();
			ArrayList<Double> list = sdMap.get(className);
			for (double value:list)
				System.out.printf("%17.4f",value);
			System.out.printf("%17s",className);
			System.out.println();
		}
		return "";
	}
}