package dragonraspberry.services;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.log4j.Logger;

public class WaveService{
	
	Logger log=Logger.getLogger(WaveService.class);
	
	private AudioInputStream audioInputStream;
	private int frameLength;
	private int frameSize;
	private int sampleFormat;
	private int channels;
	private AudioFormat format;
	private byte[] eightBitByteArray;
	private int[][] samples;
	private static WaveService INSTANCE;
	private Clip clip;
	private File waveFile;
	private String filename		= "";
	
	private WaveService()
	{
		log.info("Make the WaveService ");
	}
	
	public static WaveService getInstance()
	{
		if(INSTANCE==null)
		{
			INSTANCE=new WaveService();
		}
		return INSTANCE;
	}
	
	public boolean loadWaveFile(String audioFile)
	{
		try {
			log.info("Load wave file "+audioFile);
			filename=audioFile;
			waveFile=new File(audioFile);
			audioInputStream=AudioSystem.getAudioInputStream(waveFile);		// Open de audiofile naar een inputstream
			format=audioInputStream.getFormat();								// Haal het formaat op
			
			frameLength		= (int)audioInputStream.getFrameLength();
			frameSize		= format.getFrameSize();
			sampleFormat	= format.getSampleSizeInBits();
			channels		= format.getChannels();
			samples			= new int[channels][frameLength];					// Dit wordt de definitieve lijst met samples
			
			int sampleIndex = 0;												// teller om langs de sample te gaan
			eightBitByteArray = new byte[(int) (frameLength * frameSize)];		// Maak een 8bits buffer om de ruwe wave data in op te slaan
			int result 		  = audioInputStream.read(eightBitByteArray);		// Vul de 8bits buffer met ruwe data
			
			log.info("Framelength is "+frameLength);
			log.info("Framesize is "+frameSize);
			log.info("Samplesize is "+sampleFormat);
			log.info("Number of channels is "+channels);
			log.info("Number of samples is "+samples.length);
			log.info("eightBitByteArray size is "+eightBitByteArray.length);
			log.info("There are "+result+" bytes read");
			
			if (sampleFormat == 8) {												// verwerk een 8 bits sample
				for (int t = 0; t < eightBitByteArray.length; t++) {				// loop alle samples langs
					for (int channel = 0; channel < channels; channel++) {			// Binnen de channels
						int sample= (int) eightBitByteArray[t]+128;					// Laad een sample, een signed bit dus even een +128 correctie naar int
						samples[channel][sampleIndex] = sample;						// En laadt deze in de lijst	
					}
					sampleIndex++;
				}
			}
			
			if (sampleFormat == 16) {												// verwerk een 16 bits sample
				for (int t = 0; t < eightBitByteArray.length; t++) {				// loop door de ruwe data
					for (int channel = 0; channel < channels; channel++) {			// ga alle channels langs
						int low = (int) eightBitByteArray[t];						// lage byte
						t++;
						int high = (int) eightBitByteArray[t];						// hoge byte

						int sample = getSixteenBitSample(high, low) + 16000;		// Maak er 16 bits van
						samples[channel][sampleIndex] = sample;						// En opslaan in de array
					}
					sampleIndex++;
				}
			}
			log.info("Sample loaded size "+sampleIndex);
			
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void stopWave()
	{
		if(clip==null)return;
		clip.stop();
	}
	
	public void playWave()
	{	
		try {
			log.info("Play wave file");
			// Prepare the wave file
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			clip=(Clip)AudioSystem.getLine(info);
			
			clip.addLineListener(new LineListener(){
				@Override
				public void update(LineEvent event) {
					log.info("Event "+event.getFramePosition()+" Type "+event.getType());
					if(event.getType()==LineEvent.Type.START)
						{
							log.info("Start playing "+filename);
						}
					if(event.getType()==LineEvent.Type.STOP)
						{
						 log.info("Wave is stoppped:"+filename);
						 clip.close();
						}
				}});
			
			audioInputStream=AudioSystem.getAudioInputStream(waveFile);
			clip.open(audioInputStream);
			clip.setMicrosecondPosition(0);
			clip.start();
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	
	protected int getSixteenBitSample(int high, int low) {
		return (high << 8) + (low & 0x00ff);
	}

}
