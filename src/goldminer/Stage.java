package goldminer;

import javax.swing.*;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.text.ParseException;
import java.net.*;

public class Stage extends JPanel {
	
	double width = 800;
    double height = 600;

    double timeleft;
    List<Mineral> mineralList = new ArrayList<Mineral>();
    double rand;
    enum StageState {MENU, PLAYING, PAUSE, GAME_OVER}
    boolean ServerOrNot;
    StageState stageState;
    
    int score;
    int TotalScore;
    
    ArrayList <Integer>alistClientScores;
    ArrayList <Integer>alistServerScores;
    ArrayList <Integer>alistTotalScores;
        
    Hook hook,hookOther;
    
    Timer timer;
    Thread bgmPlay;

    void load() throws IOException {
        mineralList.clear();
        timeleft=60;
//      Customize following codes to initialize different game stages:
        mineralList.add(new Rock(409,303));
        mineralList.add(new Rock(153,234));
        mineralList.add(new Rock(647,256));
        mineralList.add(new Rock(303,392));
        mineralList.add(new Rock(431,496));
        
        mineralList.add(new Gold(132,467,30,500));
        mineralList.add(new Gold(481,544,30,500));
        mineralList.add(new Gold(377,313,30,500));
        
        mineralList.add(new Gold(211,235,20,200));
        mineralList.add(new Gold(224,363,20,200));
        mineralList.add(new Gold(391,461,20,200));
        mineralList.add(new Gold(637,462,20,200));
        mineralList.add(new Gold(291,434,20,200));
        
        mineralList.add(new Gold(681,277,10,100));
        mineralList.add(new Gold(75,373,10,100));
        mineralList.add(new Gold(213,493,10,100));
        mineralList.add(new Gold(404,513,10,100));
        mineralList.add(new Gold(618,497,10,100));
        mineralList.add(new Gold(655,507,10,100));
        mineralList.add(new Gold(694,502,10,100));
        mineralList.add(new Gold(237,254,10,100));
        
        mineralList.add(new Mouse(339,264,1,5));
        mineralList.add(new Mouse(567,414,-1,15));
        mineralList.add(new Mouse(261,505,1,10));
        
        mineralList.add(new Bag(324,300,(int)(rand*200)));
        mineralList.add(new Bag(518,314,(int)(rand*600)));
        mineralList.add(new Bag(44,566,(int)(rand*1000)));
    }

    public Stage(boolean blStatus, double random) throws IOException {
        ServerOrNot=blStatus;
        rand=random;
    	if (ServerOrNot)
    	{
    		this.stageState=StageState.PLAYING;
    		hook = new Hook(width-200, 180,false);
    		hookOther=new Hook(width+200,180,true);
    	}
    	else
    	{
    		this.stageState=StageState.MENU;
    		hook = new Hook(width+200, 180,false);
    		hookOther=new Hook(width-200,180,true);
    	}
        this.requestFocus();
    }

    void pause() {
        if (stageState == StageState.PLAYING)
            stageState = StageState.PAUSE;
        else if (stageState == StageState.PAUSE) {
            stageState = StageState.PLAYING;
        }
    }

	void refresh() throws IOException, InterruptedException, ParseException {
        if (stageState != StageState.PLAYING) return;
        if ((mineralList.isEmpty() && ! hook.hasMineral())&& ! hookOther.hasMineral()||timeleft <= 0) {
        	stageState=StageState.GAME_OVER;
        	bgmPlay.stop();
        	
        	if(ServerOrNot)
        	{
        		File fileRecords=new File("res/ServerRecords.txt");
            	BufferedWriter bwRecord=new BufferedWriter(new FileWriter(fileRecords,true));
        		bwRecord.write(String.valueOf(TotalScore-score)+","+String.valueOf(score)+","+String.valueOf(TotalScore)+"/");
            	bwRecord.close();
        	}
        	else
        	{
        		File fileRecords=new File("res/ClientRecords.txt");
            	BufferedWriter bwRecord=new BufferedWriter(new FileWriter(fileRecords,true));
        		bwRecord.write(String.valueOf(TotalScore-score)+","+String.valueOf(score)+","+String.valueOf(TotalScore)+"/");
            	bwRecord.close();
        	}
        	alistClientScores=new ArrayList<Integer>();
        	alistServerScores=new ArrayList<Integer>();
        	alistTotalScores=new ArrayList<Integer>();
        	FileInputStream fisRecords=new FileInputStream("res/ServerRecords.txt");
        	BufferedReader brRecords=new BufferedReader(new InputStreamReader(fisRecords));
        	String[] arrayRecords=brRecords.readLine().split("/");
        	for (int i=0;i<arrayRecords.length;i++)
        		{
        			String[] arrayScores=arrayRecords[i].split(",");
        			alistClientScores.add(Integer.valueOf(arrayScores[0]));
        			alistServerScores.add(Integer.valueOf(arrayScores[1]));
        			alistTotalScores.add(Integer.valueOf(arrayScores[2]));
        		}
        	brRecords.close();
        	fisRecords.close();
        }
        timeleft-=0.1;
        hook.refresh(this);
        hookOther.refresh(this);
        for (Mineral i : mineralList) {
        	if (i instanceof Mouse) {
        		((Mouse)i).runMouse();
        	}
        }
        
        repaint();
    }

    void start() {
        stageState = StageState.PLAYING;
        String soundName="res/sounds/play-bgm.wav";
        bgmPlay=new Thread(new SoundPlayer(soundName));
        bgmPlay.start();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
					try {
						refresh();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            }
        }, 0, 100);
    }
    Image scoreboard=Toolkit.getDefaultToolkit().createImage("res/images/scoreboard.png");
    Image gamebgPic=Toolkit.getDefaultToolkit().createImage("res/images/map_bg_0.png");
    Image buttonBg=Toolkit.getDefaultToolkit().createImage("res/images/text-background.png");
    Image timeLineCenter=Toolkit.getDefaultToolkit().createImage("res/images/timecenter.png");
    Image retryBtn=Toolkit.getDefaultToolkit().createImage("res/images/replay.png");



    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, (int)width, (int)height);
		switch (stageState) {
            case PLAYING:
            case PAUSE:
            	g.drawImage(gamebgPic,0,0,(int)width,(int)height,this);
            	g.drawImage(scoreboard,30,30,250,80,this);
            	g.setFont(new Font("Tahoma", Font.BOLD, 22));
            	g.setColor(Color.white);
            	g.drawString("Press ¡ý to dig, press P to pause or resume",300,20);
            	g.drawString("Your Score: "+score,50,60);
            	g.drawString("Total Score: "+TotalScore,50,90);
            	g.drawString("Time: "+((int)timeleft),670,50);
            	g.drawString("Server", 280, 150);
            	g.drawString("Client", 480, 150);
            	g.setColor(Color.black);
            	try {
                	hook.paint(g);
                	hookOther.paint(g);
                } catch (IOException error) {}
                for (Mineral m : mineralList) {
                    m.paint(g);
                }           
                g.setColor(Color.red);
                break;
            case MENU:
                g.drawImage(gamebgPic,0,0,(int)width, (int)height, this);
                g.drawImage(buttonBg,330,210,150, 50,this);
                g.drawImage(buttonBg,330,290,150, 50,this);
                
                g.setFont(new Font("Arial", Font.BOLD, 28));
                g.setColor(Color.white);
                g.drawString("Start",350,242);
                g.drawString("Quit",350,322);
                //g.drawString("")

                break;
            case GAME_OVER:
            	int[] ClientRank= rankRecords(alistClientScores);
            	int[] ServerRank=rankRecords(alistServerScores);
            	int[] TotalRank=rankRecords(alistTotalScores);
            	g.drawImage(gamebgPic,0,0,(int)width,(int)height,this);
            	g.drawImage(scoreboard,200,20,400,100,this);
            	g.setFont(new Font("Tahoma", Font.BOLD, 42));
            	g.setColor(Color.white);
            	g.drawString("GAME OVER", 250, 80);
            	g.setFont(new Font("Tahoma", Font.BOLD, 28));
            	g.drawString("Your Partner's Score: "+(TotalScore-score),250,230);
            	g.drawString("Your Score: "+score,250,260);
            	g.drawString("Total Score: "+TotalScore,250,290);
            	g.setFont(new Font("Tahoma", Font.PLAIN, 20));
            	g.drawString("Client Ranking:",130,330);
            	g.drawString("Server Ranking:",330,330);
            	g.drawString("Total Ranking:",530,330);
            	g.drawString(String.valueOf(ClientRank[0]),170,360);
            	g.drawString(String.valueOf(ClientRank[1]),170,390);
            	g.drawString(String.valueOf(ClientRank[2]),170,420);
            	g.drawString(String.valueOf(ClientRank[3]),170,450);
            	g.drawString(String.valueOf(ClientRank[4]),170,480);
            	g.drawString(String.valueOf(ServerRank[0]),370,360);
            	g.drawString(String.valueOf(ServerRank[1]),370,390);
            	g.drawString(String.valueOf(ServerRank[2]),370,420);
            	g.drawString(String.valueOf(ServerRank[3]),370,450);
            	g.drawString(String.valueOf(ServerRank[4]),370,480);
            	g.drawString(String.valueOf(TotalRank[0]),570,360);
            	g.drawString(String.valueOf(TotalRank[1]),570,390);
            	g.drawString(String.valueOf(TotalRank[2]),570,420);
            	g.drawString(String.valueOf(TotalRank[3]),570,450);
            	g.drawString(String.valueOf(TotalRank[4]),570,480);
                break;
        }
    }
    
    public int[] rankRecords(ArrayList<Integer> alist)
    {	
    	int[] RankedScores=new int[5];
    	int[] RankedIndex=new int[5];
    	for (int i=0;i<5;i++)
    	{
    		RankedScores[i]=0;
    	}
    	for (int position=0;position<5;position++)
    	{
        	for (int i=0;i<alist.size();i++)
        	{
        		if (alist.get(i)>RankedScores[position]) 
        		{
        			RankedScores[position]=alist.get(i);
        			RankedIndex[position]=i;
        		}
        		else if(alist.get(i)==RankedScores[position])
        		{
        			if(RankedIndex[position]<i)
        			{
        				RankedScores[position]=alist.get(i);
            			RankedIndex[position]=i;
        			}
        		}
        	}
    		alist.remove(RankedIndex[position]);
    	}
    	return RankedScores;
    }
}