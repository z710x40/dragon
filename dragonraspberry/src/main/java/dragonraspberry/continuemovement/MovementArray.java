package dragonraspberry.continuemovement;


public class MovementArray {

	
	private int __count=0;
	private int[] __valueList;
	private int __max;
	private int __min;
	private int __rest;
	 
	
	public MovementArray(int steps,int min,int max,int servo)
	{
		__valueList=__makeMove(steps,min,max);
		__count=0;
		__max=max;
	}
	
	
	public int getNext()
	{
		try{
			return __valueList[__count++];
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return __max;
		}
	}
	
	
	
	private int[] __makeMove(int steps,int min,int max)
	{
		int valueList[]=new int[steps];							// Make an empty list
		double diff=max-min;								// calculate the difference
		double stepSize=Math.PI/steps;						// Calculate the stepssize for 1/4 PI
		
		for(int tel=0;tel<steps;tel++)							// Loop to all the steps
		{
			valueList[tel]=min-(int)((0.5*Math.cos(tel*stepSize)-0.5)*diff);	// Calculate the movement
		}
		return valueList;
	}
	
}