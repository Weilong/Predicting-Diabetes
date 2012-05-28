import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

public class ClassifierApplication
{
	private String fileName;
	private String validation;
	private String algorithm;
	private BufferedReader dataSet;
	private ArrayList<String> attributes;
	private int numInstance;
	
	public ClassifierApplication(String filename) throws IOException
	{
		this.fileName = filename;
		//this.algorithm = algorithm;
		//this.validation = validation;
		attributes = new ArrayList<String>();
		numInstance = 0;
		readDataSet();
		preprocessDataSet();
	}
	
	private void readDataSet() throws FileNotFoundException
	{
		dataSet = new BufferedReader(new FileReader(fileName));
	}
	
	private void preprocessDataSet() throws IOException
	{
		String line = dataSet.readLine();
		String tmpHeaders[] = line.split(",");
		for (String  item:tmpHeaders)
			attributes.add(item);
		line =dataSet.readLine();
		
		while(line!=null)
		{
			numInstance++;
			line = dataSet.readLine();
		}
		dataSet.close();
	}
	
	public void run() throws IOException
	{
		int folds =10;
		double accuracySum =0;
		int instanceNumber;
		int strat[] = new int[folds];
		int stratPos[] = new int[folds];
		String newRecord;
		NumberFormat nf = NumberFormat.getPercentInstance();
		NaiveBayes nb = new NaiveBayes(attributes);
		
		for (int i=0;i<folds;i++)
			strat[i]=numInstance/folds;
		for (int i=0;i<numInstance%folds;i++)
			strat[strat.length-1-i]++;
		for (int i=0;i<folds;i++)
		{
			stratPos[i]=1;
			for (int j=0;j<i;j++)
			{
				stratPos[i]+=strat[j];
			}
		}
			
		for (int i=0;i<folds;i++)
		{
			//training
			readDataSet();
			dataSet.readLine();	//skip header line
			newRecord = dataSet.readLine();
			numInstance++;
			instanceNumber = 1;
			
			while(newRecord!=null)
			{
				if (instanceNumber == stratPos[i])
				{
					for (int j=0;j<strat[i];j++)
					{
						newRecord = dataSet.readLine();
						instanceNumber++;
					}
				}
				if (newRecord!=null)
				{
					nb.addTrainingData(newRecord);
					newRecord = dataSet.readLine();
					instanceNumber++;
				}
			}
			dataSet.close();
			nb.training();
			System.out.println(nb);
			
			//testing
			readDataSet();
			dataSet.readLine();	//skip header line
			newRecord = dataSet.readLine();
			numInstance++;
			int correctInstances = 0;
			instanceNumber=1;
			
			while(newRecord!=null)
			{
				if (instanceNumber == stratPos[i])
				{
					for (int j=0;j<strat[i];j++)
					{
						if(nb.testing(newRecord))
							correctInstances++;
						newRecord = dataSet.readLine();
						instanceNumber++;
					}
					break;
				}
				newRecord = dataSet.readLine();
				instanceNumber++;
			}
			dataSet.close();
			
			nf.setMinimumFractionDigits(4);
			System.out.println((i+1) + ((i+1)==1?" fold":" folds")+" accuracy: "+ nf.format((double)correctInstances/(double)strat[i]));
			System.out.println();
			accuracySum+=(double)correctInstances/(double)strat[i];
			nb.reset();
		}
		System.out.println("Average accuracy over the "+folds+" runs: "+nf.format(accuracySum/folds));
	}
	
	public static void main(String[] args) throws IOException
	{
		ClassifierApplication classifier = new ClassifierApplication(args[0]);
		classifier.run();
	}
}