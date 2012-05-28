import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
/**
 * COMP3308 Introduction to AI Assignment2
 * NB_KFold.java
 * Purpose: the main class of the program. receive the command from user
 * and do the Naive Bayes algorithm on the given dataset with k-fold val-
 * idation. The program will print out the accuracry for each fold and an
 * average accuracy will be printed out at the end. The number of folds
 * must be greater than 1, or a warning will be given.
 *
 * @author Weilong Ding/Ruijun GU
 * @version 1.0 27/05/2012
 */
public class NB_KFold
{
	private String fileName;
	private BufferedReader dataSet;	
	private ArrayList<String> attributes;	//store the names of the attributes of the dataset
	private int numInstance;	//the number of total instances
	private int folds;	//the number of folds Naive Nayes runs
	
	/**
	 * This is the constructor of NB_KFold class which will read
	 * a file and the number of folds to initialize and preprocess
	 * the dataset
	 * @param filename the url of the input dataset
	 * @param folds the number of folds user what to do
	 * @throws IOException
	 */
	public NB_KFold(String filename,int folds) throws IOException
	{
		this.fileName = filename;
		this.folds = folds;
		attributes = new ArrayList<String>();
		numInstance = 0;
		readDataSet();
		preprocessDataSet();
	}
	
	/**
	 * Method readDataSet initialize the BufferedReader of the
	 * input dataset
	 * @throws FileNotFoundException
	 */
	private void readDataSet() throws FileNotFoundException
	{
		dataSet = new BufferedReader(new FileReader(fileName));
	}
	
	/**
	 * Method preprocessDataSet read and store the names of all
	 * the attributes and calculate the number of instances in 
	 * the dataset
	 * @throws IOException
	 */
	private void preprocessDataSet() throws IOException
	{
		/*
		 * read the first line of dataset, which is the 
		 * header line and store the names of the attri-
		 * butes in attributes list.
		 */
		String line = dataSet.readLine();
		String tmpHeaders[] = line.split(",");
		for (String  item:tmpHeaders)
			attributes.add(item);
		line =dataSet.readLine();
		//calculate the number of instances in the dataset
		while(line!=null)
		{
			numInstance++;
			line = dataSet.readLine();
		}
		dataSet.close();
	}
	/**
	 * Method run executes the most function of NB_KFold class.
	 * It stratifies the dataset according to the the number of
	 * folds from user's input and do Naive Bayes algorithm. The
	 * accuracy of each fold will be printed out in percentage and
	 * an average accuracy will be printed out at the end.
	 * @throws IOException
	 */
	public void run() throws IOException
	{
		//if the number of folds from input is not greater than 1
		//show warning and exit program
		if (folds<2)
		{
			System.out.println("Warning! Number of folds must be greater than 1!");
			return;
		}
		double accuracySum =0;	//the sum of accuracies of all folds
		int instanceNumber;
		int strat[] = new int[folds];	//stores how many instances in each stratification
		int stratPos[] = new int[folds];	//stores the position where stratifiction begins
		String newInstance;
		NumberFormat nf = NumberFormat.getPercentInstance();
		NaiveBayes nb = new NaiveBayes(attributes);	//create an instance of NaiveBayes class
		/*
		 * since stratification needs to be done as evenly as possible
		 * first we divide numInstace by folds and store the result in 
		 * strat list. so far each item of strat is the same.Secondly we get
		 * the reminder and allocate it evenly to the existed stratification.
		 * eg in this program the dataset has 768 instances. firstly we divide 
		 * 768 by 10 and we get 76 and allocate the reminder 8 to the existed 
		 * stratification so that we get 76,76,77,77,77,77,77,77,77,77.
		 */
		for (int i=0;i<folds;i++)
			strat[i]=numInstance/folds;
		for (int i=0;i<numInstance%folds;i++)
			strat[strat.length-1-i]++;
		/*
		 * calculate the postion where the stratification starts
		 * if we know the position, we can skip instances from that position
		 * for certain instances since the skipped instances are reserved for
		 * testing.
		 */
		for (int i=0;i<folds;i++)
		{
			stratPos[i]=1;
			for (int j=0;j<i;j++)
			{
				stratPos[i]+=strat[j];
			}
		}
		//run Naive Bayes algorithm with k-fold validation
		for (int i=0;i<folds;i++)
		{
			//training starts
			readDataSet();
			dataSet.readLine();	//skip header line
			newInstance = dataSet.readLine();
			numInstance++;
			instanceNumber = 1;	//track which instance has been reached
			//read each line until file reaches end
			while(newInstance!=null)
			{
				/*
				 * if the current instance is the one where stratification
				 * should start, skip certain number of instances (which is 
				 * for testing, the number of instances to skip is stored in 
				 * strat list) and continue to read the data
				 */
				if (instanceNumber == stratPos[i])
				{
					for (int j=0;j<strat[i];j++)
					{
						newInstance = dataSet.readLine();
						instanceNumber++;
					}
				}
				if (newInstance!=null)
				{
					nb.addTrainingData(newInstance);
					newInstance = dataSet.readLine();
					instanceNumber++;
				}
			}
			dataSet.close();
			nb.training();
			
			//testing starts
			readDataSet();
			dataSet.readLine();	//skip header line
			newInstance = dataSet.readLine();
			numInstance++;
			int correctInstances = 0;	//track the number of correct classified instances
			instanceNumber=1;
			
			while(newInstance!=null)
			{
				/*
				 * if the current instance is where the stratification
				 * starts, test the instance until the testing portion 
				 * of the dataset finishes, otherwise skip the instance. 
				 */
				
				if (instanceNumber == stratPos[i])
				{
					for (int j=0;j<strat[i];j++)
					{
						if(nb.testing(newInstance))
							correctInstances++;	//if the instance is correctly classified,increment correctInstances
						newInstance = dataSet.readLine();
						instanceNumber++;
					}
					break;	//after finish the testing data portion, stop reading  dataset since no need
				}
				newInstance = dataSet.readLine();
				instanceNumber++;
			}
			dataSet.close();
			//calculate the accuracy for each fold and print it out
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
		NB_KFold classifier = new NB_KFold(args[0],Integer.parseInt(args[1]));
		classifier.run();
	}
}