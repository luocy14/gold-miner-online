package goldminer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;


public class Hook {
    private double sourceX;
    private double sourceY;
    private double theta=0.0;
    private double d=0.0;
    final double r = 30.0;
    private double weight=800.0;
    boolean OtherOrNot;
    private Mineral mineral;
    
    HookState state;
    enum HookState{WAIT, FORWARD, BACKWARD}
    
    int hookWaitDirection = 1;

    public Hook(double width, double height, boolean bl){
        sourceX = width/2;
        sourceY = height; 
        OtherOrNot=bl;
        state = HookState.WAIT;
    }

    double getX(){
        return sourceX + d * Math.cos(theta);
    }

    double getY(){
        return sourceY + d * Math.sin(theta);
    }

    double getWeight(){
        return mineral == null ? weight : weight 
        		+ mineral.density * mineral.r * mineral.r;
    }

    double getPullVelocity(){
    	return 40000.0 / getWeight();
    }    
    double getPushVelocity(){
    	return 50.0;
    }
    
    boolean hasMineral() {
    	return mineral != null;
    }

    boolean hookMineral(Mineral m){
        if(distance(getX(),getY(),m.x,m.y) < (r/2 + m.r)){
            mineral = m;
            state = HookState.BACKWARD;
            String soundName="res/sounds/pull-org.wav"; 
            Thread playSound = new Thread(new SoundPlayer(soundName)); 
			playSound.start();
            return true;
        } else {
        	return false;
        }
    }
    
    void refresh(Stage stage){
        switch (state){
            case WAIT:
            	theta += hookWaitDirection * Math.PI / GoldMiner.PERIOD;
            	
            	if (theta >= Math.PI * 19 / 20) {
            		hookWaitDirection = -1;
            	}
            	else if (theta <= Math.PI / 20) {
            		hookWaitDirection = 1;
            	}
                break;
                
            case FORWARD:
            	d += getPushVelocity();
            	
            	if (getX() < 50 || getX() > 750 || getY() > 550) {
            		state = HookState.BACKWARD;
            		break;
            	}
            	
                for(int i=0; i<stage.mineralList.size(); i++){
                    Mineral testMineral = stage.mineralList.get(i);
                	if(hookMineral(testMineral)){
                    	testMineral.hooked(stage,i);
                    	break;
                    }
                }
                break;
                
            case BACKWARD:
            	d -= getPullVelocity();
            	
            	if (mineral != null){
            		mineral.refresh(getX() + r * Math.cos(theta), 
            				getY() + r * Math.sin(theta));
            	}
            	if (d <= 0){
            		if (mineral != null) {
            			stage.TotalScore += mineral.value;
            			if (!OtherOrNot) stage.score+=mineral.value;
            			String soundName; 
            			if (mineral.value < 150) {
            				soundName = "res/sounds/low-value.wav";
            			} else if (mineral.value >= 300) {
            				soundName = "res/sounds/high-value.wav";
            			} else {
            				soundName = "res/sounds/normal-value.wav";
            			}
            			Thread playSound = new Thread(new SoundPlayer(soundName)); 
        				playSound.start();
            			mineral = null;
            		}
            		d = 0;
            		state = HookState.WAIT;
            	}
            	break;
        }
    }

    void paint(Graphics g) throws IOException{
    	switch (state) {
    	case BACKWARD:
    		if (mineral != null){
    			mineral.paint(g);
    		}    		
    	default:
    		Graphics2D g2= (Graphics2D)g;
    		g2.setStroke(new BasicStroke(2.0f));
        	g2.drawLine((int)sourceX, (int)(sourceY), (int)getX(), (int)getY());
    		BufferedImage hookImage = ImageIO.read(new File("res/images/hook2.png"));
        	BufferedImage rotatedImage = rotateImage(hookImage, theta);
        	g.drawImage(rotatedImage,
        			(int)(getX() - r), (int)(getY() - r), 2*(int)r, 2*(int)r, null);
    	}    	
    }

    void launch(){
        if(state==HookState.WAIT)
        {
        	state = HookState.FORWARD;
            String soundName="res/sounds/dig.wav";
            Thread playSound=new Thread(new SoundPlayer(soundName));
            playSound.start();
        }
    }

    private static double distance(double x1, double y1, double x2, double y2){
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
    
    public static BufferedImage rotateImage(final BufferedImage bufferedimage,
            final double theta) {
        int w = bufferedimage.getWidth();
        int h = bufferedimage.getHeight();
        int type = bufferedimage.getColorModel().getTransparency();
        BufferedImage img;
        Graphics2D graphics2d;
        (graphics2d = (img = new BufferedImage(w, h, type))
                .createGraphics()).setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.rotate(theta, w / 2, h / 2);
        graphics2d.drawImage(bufferedimage, 0, 0, null);
        graphics2d.dispose();
        return img;
    }    
}

class SoundPlayer implements Runnable {
	public boolean canplay=true;
	private String soundName;
	SoundPlayer(String soundName) {
		this.soundName = soundName;
	}
	
	public void run() {
		final File file = new File(soundName);

        try {
            final AudioInputStream in = 
            		AudioSystem.getAudioInputStream(file);
            
            final int ch = in.getFormat().getChannels();  
            final float rate = in.getFormat().getSampleRate();  
            final AudioFormat outFormat = new AudioFormat(
            		AudioFormat.Encoding.PCM_SIGNED, rate,
            		16, ch, ch * 2, rate, false);
            
            final DataLine.Info info = 
            		new DataLine.Info(SourceDataLine.class, outFormat);
            final SourceDataLine line = 
            		(SourceDataLine) AudioSystem.getLine(info);

            if (line != null) {
                line.open(outFormat);
                line.start();
                canplay=true;
                final byte[] buffer = new byte[65536];  
                for (int n = 0; n != -1&&canplay;
                		n = AudioSystem
                				.getAudioInputStream(outFormat, in)
                				.read(buffer, 0, buffer.length)) {  
                    line.write(buffer, 0, n);
                }
                line.drain();
                line.stop();
            }
            line.close();
            in.close();
            
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
	}
}
