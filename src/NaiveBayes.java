import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class NaiveBayes
{
	private ArrayList<String> attributes;
	private HashMap<String,ArrayList<Double>> meanMap;
	private HashMap<String,ArrayList<Double>> SDMap;
	private HashMap<String,Double> classPriors;
	private HashMap<String,Integer> numClass;
	
	public NaiveBayes(ArrayList<String> attributes) throws FileNotFoundException
	{
		this.attributes = attributes;
		meanMap = new HashMap<String,ArrayList<Double>>();
		SDMap = new HashMap<String,ArrayList<Double>>();
		classPriors = new HashMap<String,Double>();
		numClass = new HashMap<String,Integer>();
	}
	
	public void addTrainingData(String record)
	{
		String className;
		String values[];
		
		values = record.split(",");
		className = values[values.length-1];
		
		if(!meanMap.containsKey(className)&&!SDMap.containsKey(className))
		{
			meanMap.put(className, new ArrayList<Double>());
			SDMap.put(className, new ArrayList<Double>());
			classPriors.put(className, (double)0);
			numClass.put(className, 0);
			
			for (int i=0;i<values.length-1;i++)
			{
				meanMap.get(className).add((double)0);
				SDMap.get(className).add((double)0);
			}
		}
		
		for (int i=0;i<values.length-1;i++)
		{
			
			meanMap.get(className).set(i, meanMap.get(className).get(i)+Double.parseDouble(values[i]));
			SDMap.get(className).set(i, SDMap.get(className).get(i)+Math.pow(Double.parseDouble(values[i]), 2));
		}
		numClass.put(className,numClass.get(className)+1);
	}
	
	public void training() throws IOException
	{
		String className;
		int numInstance = 0;
		for (Iterator<String> it = numClass.keySet().iterator();it.hasNext();)
			numInstance+=numClass.get(it.next());
		
		for (Iterator<String> it = numClass.keySet().iterator();it.hasNext();)
		{
			className = it.next();
			classPriors.put(className, numClass.get(className)/(double)numInstance);
		}
		
		for (Iterator<String> it = meanMap.keySet().iterator();it.hasNext();)
		{
			className = it.next();
			ArrayList<Double> list = meanMap.get(className);
			for (int i=0;i<list.size();i++)
			{
				list.set(i, list.get(i)/numClass.get(className));			
			}
		}
				
		for (Iterator<String> it = SDMap.keySet().iterator();it.hasNext();)
		{
			className = it.next();
			ArrayList<Double> list = SDMap.get(className);
			for (int i=0;i<list.size();i++)
			{
				double tmpSD = Math.sqrt(list.get(i)/numClass.get(className)-Math.pow(meanMap.get(className).get(i),2));
				list.set(i, tmpSD);
			}
		}
	}
	
	public boolean testing(String record)
	{
		String correctClass = null;
		String testClass = null;
		String values[];
		values = record.split(",");
		correctClass = values[values.length-1];
		double likelihood,maxProb=0;
		Iterator<String> meanIt = meanMap.keySet().iterator();
		Iterator<String> sdIt = SDMap.keySet().iterator();

		while(meanIt.hasNext()&&sdIt.hasNext())
		{
			String className = meanIt.next();
			likelihood=1;
			ArrayList<Double> meanList = meanMap.get(className);
			ArrayList<Double> sdList = SDMap.get(className);	
			
			for (int i=0;i<meanList.size();i++)
				likelihood*= pdf(Double.parseDouble(values[i]),meanList.get(i),sdList.get(i));
			likelihood*=classPriors.get(className);//System.out.println();
			
			if (likelihood>maxProb)
			{
				maxProb = likelihood;
				testClass = className;
			}
		}
		return testClass.equals(correctClass);
	}
	
	public void reset()
	{
		meanMap.clear();
		SDMap.clear();
		numClass.clear();
		classPriors.clear();
	}
	
	private double pdf(double x,double mean,double sd)
	{
		return 1/(sd*Math.sqrt(2*Math.PI))*Math.exp(-Math.pow(x-mean, 2)/(2*Math.pow(sd, 2)));
	}
	
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
		for (Iterator<String> it = SDMap.keySet().iterator();it.hasNext();)
		{
			String className = it.next();
			ArrayList<Double> list = SDMap.get(className);
			for (double value:list)
				System.out.printf("%17.4f",value);
			System.out.printf("%17s",className);
			System.out.println();
		}
		return "";
	}
}