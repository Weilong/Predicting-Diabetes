import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class NaiveBayes
{
	//private String headers[];
	private ArrayList<String> headers;
	private HashMap<String,ArrayList<Double>> meanMap;
	private HashMap<String,ArrayList<Double>> SDMap;
	private HashMap<String,Integer> numClass;
	
	public NaiveBayes(String filename)
	{
		headers = new ArrayList<String>();
		meanMap = new HashMap<String,ArrayList<Double>>();
		SDMap = new HashMap<String,ArrayList<Double>>();
		numClass = new HashMap<String,Integer>();
		
		try {
			processFile(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void processFile(String filename) throws FileNotFoundException
	{
		Scanner s = new Scanner(new File(filename));
		String line;
		String className;
		String tmp[];

		line = s.nextLine();
		String tmpHeaders[] = line.split(",");
		for (String  item:tmpHeaders)
			headers.add(item);
		//test
		/*for (int i=0;i<headers.size();i++)
			System.out.print(headers.get(i)+",");
		System.out.println();*/
		while (s.hasNextLine())
		{
			line = s.nextLine();
			tmp = line.split(",");
			//test
			/*for (int i=0;i<tmp.length;i++)
				System.out.print(tmp[i]+",");
			System.out.println();*/
			className = tmp[tmp.length-1];
			//test
			//System.out.println(className);
			if(!meanMap.containsKey(className)&&!SDMap.containsKey(className))
			{
				meanMap.put(className, new ArrayList<Double>());
				SDMap.put(className, new ArrayList<Double>());
				numClass.put(className, 0);
				
				for (int i=0;i<tmp.length-1;i++)
				{
					meanMap.get(className).add((double)0);
					SDMap.get(className).add((double)0);
				}
			}
			for (int i=0;i<tmp.length-1;i++)
			{
				meanMap.get(className).set(i, meanMap.get(className).get(i)+Double.parseDouble(tmp[i]));
				SDMap.get(className).set(i, SDMap.get(className).get(i)+Math.pow(Double.parseDouble(tmp[i]), 2));
			}
			numClass.put(className,numClass.get(className)+1);
		}
		
		for (Iterator<String> it = meanMap.keySet().iterator();it.hasNext();)
		{
			className = it.next();
			ArrayList<Double> list = meanMap.get(className);
			for (int i=0;i<list.size();i++)
				list.set(i, list.get(i)/numClass.get(className));
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
	//TODO getMean
	public double getMean(String className,String attribute)
	{
		return meanMap.get(className).get(headers.indexOf(attribute));	
	}
	//TODO getSD
	public double getSD(String className,String attribute)
	{
		return meanMap.get(className).get(headers.indexOf(attribute));
	}
	//TODO toString
	public String toString()
	{
		System.out.println("Mean table:");
		for (String item:headers)
			System.out.print(item+" ");
		System.out.println();
		for (Iterator<String> it = meanMap.keySet().iterator();it.hasNext();)
		{
			String className = it.next();
			ArrayList<Double> list = meanMap.get(className);
			for (double item:list)
				System.out.format("%.4f ",item);
			System.out.print(className);
			System.out.println();
		}
		System.out.println();
		
		System.out.println("Standard Deviation table:");
		for (String item:headers)
			System.out.print(item+" ");
		System.out.println();
		for (Iterator<String> it = SDMap.keySet().iterator();it.hasNext();)
		{
			String className = it.next();
			ArrayList<Double> list = SDMap.get(className);
			for (double item:list)
				System.out.format("%.4f ",item);
			System.out.print(className);
			System.out.println();
		}
		
		return "";
	}
	
	public static void main(String[] args)
	{
		NaiveBayes classifier = new NaiveBayes(args[0]);
		System.out.println(classifier);
	}
}