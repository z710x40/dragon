package dragonraspberry.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Class that can store a full motion
 * @author Erwin
 *
 */
public class Motion {
		
	Logger log=Logger.getLogger(this.getClass().getSimpleName());
	
	private final int __size=Globals.numberOfServos;
	private String seqFileName;
	private String waveFileName;
	private List<int[]> servoValueList=new ArrayList<>();
	private int numberOfSteps=0;
	private boolean endOfSequenceReached=false;
	
	public Motion(String name,String waveName)
	{
		log.info("Make Motion ");
		this.seqFileName=name;
		this.waveFileName=waveName;
		log.debug("Sequence file is "+seqFileName);
		log.debug("wave file is "+waveName);
		log.debug("actionlist has action:"+servoValueList.size());
	}
	
	
	public void makeEmtpyMotion(int steps)
	{
		log.info("Create empty motion of "+steps+"steps");
		int [] emtpy={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		servoValueList.clear();
		for(int tel=0;tel<steps;tel++)
		{
			servoValueList.add(emtpy);
		}
		numberOfSteps=steps;
		endOfSequenceReached=false;
	}
	
	
	public int[] getListFromStep(int step)
	{
		if(step<numberOfSteps)return servoValueList.get(step);
		log.info("End of sequence reached");
		endOfSequenceReached=true;
		return servoValueList.get(numberOfSteps-1);
	}
	
	public String getSeqFileName() {
		return seqFileName;
	}
	
	public String getWaveFileName()
	{
		return waveFileName;
	}
	
	public void parseSequenceFile(List<String> seqFile) {
		servoValueList.clear();
		for (String line : seqFile) {
			//log.debug("Parse " + line);
			int[] valueList = new int[__size];
			for (int tel = 0; tel < __size; tel++) {
				valueList[tel] = Integer.parseInt(line.substring((tel * 4), 4 + (tel * 4)));
			}
			servoValueList.add(valueList);
		}
		numberOfSteps = servoValueList.size();
		log.debug("Parsed action file of " + numberOfSteps + " steps");
	}
	
	public void dumpMotion()
	{
		log.info("Sequence name is "+seqFileName);
		log.info("WaveFile name is "+waveFileName);
		servoValueList.forEach(motion -> log.info(Arrays.toString(motion)));
	}
	
	public boolean isEndOfSequenceReached()
	{
		return endOfSequenceReached;
	}
	
	public void resetMotion()
	{
		endOfSequenceReached=false;
	}
	
}
