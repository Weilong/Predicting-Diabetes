import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

public class NB_KFold
{
	private String fileName;
	private BufferedReader dataSet;
	private ArrayList<String> attributes;
	private int numInstance;
	
	public NB_KFold(String filename) throws IOException
	{
		this.fileName = filename;
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
	
	public void run(int folds) throws IOException
	{
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
	{System.out.println(args[0]);
		NB_KFold classifier = new NB_KFold(args[0]);
		classifier.run(Integer.parseInt(args[1]));
	}
}