package goldminer;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.*;
import java.io.*;

public class GoldMiner extends JFrame{
    Stage stage;
    static final double TIME_STEP = 0.1;
    static final double PERIOD = 20.0;
    Thread bgmMenu;
    
    static Socket sCStart, sSStart,sCPause,sSPause,sCLaunch,sSLaunch;
    static ServerSocket ssStart,ssPause,ssLaunch;
    boolean ServerOrNot;
    
	//Customize strHost for online game
	String strHost="localhost";
    
    public GoldMiner(String strStatus,boolean blStatus, double rand) throws IOException{
    	setTitle(strStatus);
        setSize(800,650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ServerOrNot=blStatus;
        stage = new Stage(ServerOrNot,rand);
		stage.setFocusable(true);
		stage.requestFocusInWindow();
        String soundName="res/sounds/menu-bgm.wav";
        bgmMenu=new Thread(new SoundPlayer(soundName));
        if(!ServerOrNot) bgmMenu.start();
       
        stage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
                super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        stage.hook.launch();
                        if(ServerOrNot)
                        {
                        	DataOutputStream dosServerLaunch;
							try {
								dosServerLaunch = new DataOutputStream(sSLaunch.getOutputStream());
								dosServerLaunch.write(1);
	                        	dosServerLaunch.flush();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
                        }
                        else
                        {
                        	DataOutputStream dosClientLaunch;
							try {
								dosClientLaunch = new DataOutputStream(sCLaunch.getOutputStream());
	                        	dosClientLaunch.write(1);
	                        	dosClientLaunch.flush();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
                        }
                        break;
                    case KeyEvent.VK_P:
                        stage.pause();
                        if(ServerOrNot)
                        {
                        	DataOutputStream dosServerPause;
							try {
								dosServerPause = new DataOutputStream(sSPause.getOutputStream());
								dosServerPause.write(1);
	                        	dosServerPause.flush();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
                        }
                        else
                        {
                        	DataOutputStream dosClientPause;
							try {
								dosClientPause = new DataOutputStream(sCPause.getOutputStream());
	                        	dosClientPause.write(1);
	                        	dosClientPause.flush();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
                        }
                        break;
                }
            }
        });

        stage.addMouseListener(new MouseAdapter() {
                                   @Override
                                   public void mouseClicked(MouseEvent e) {
                                       super.mouseClicked(e);
                                       int x=e.getX(), y=e.getY();
                                       if(stage.stageState==Stage.StageState.MENU){

                                           if(x>340&&x<490 && y>200 && y<250){
                                               try{
                                            	   bgmMenu.stop();
                                            	   sCStart=new Socket(strHost,19999);
                                            	   sCPause=new Socket(strHost,19998);
                                            	   sCLaunch=new Socket(strHost,19997);
                                            	   setClientThread();
                                            	   stage.load();
                                                   stage.start();
                                               }catch (IOException e1){
                                                   e1.printStackTrace();
                                               }
                                           }else if(x>340&&x<490&& y>280 && y<330){
                                               bgmMenu.stop();
                                        	   dispose();
                                           }
                                       }
                                   }
                               }
        );

        add(stage);
    }
    
    public static void main(String[] args) throws IOException{
    	double rand=Math.random();
    	ssStart=new ServerSocket(19999);
    	ssPause=new ServerSocket(19998);
    	ssLaunch=new ServerSocket(19997);
    	
    	EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
			        GoldMiner gmClient = new GoldMiner("Gold Miner: Client Mode",false,rand);
			        gmClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    	
    	while(true)
    	{
    		sSStart=ssStart.accept();
    		sSPause=ssPause.accept();
    		sSLaunch=ssLaunch.accept();
    		EventQueue.invokeLater(new Runnable()
    		{
    			public void run()
    			{
    				GoldMiner gmServer;
					try {
						gmServer = new GoldMiner("Gold Miner: Server Mode",true,rand);
						gmServer.setServerThread();
                 	    gmServer.stage.load();
                        gmServer.stage.start();
						gmServer.setVisible(true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    		      
    			}
    		});
    	}
    }
    
    public void setClientThread() throws IOException
    {
    	Thread trdServerPause=new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					BufferedReader brServerPause=new BufferedReader(new InputStreamReader(sCPause.getInputStream()));
					do
					{
						int signalServerPause=brServerPause.read();
						stage.pause();
					}while(true);
				}
				catch(IOException e2)
				{
					e2.printStackTrace();
				}
			}
		});
    	trdServerPause.start();
    	Thread trdServerLaunch=new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					BufferedReader brServerLaunch=new BufferedReader(new InputStreamReader(sCLaunch.getInputStream()));
					do
					{
						int signalServerLaunch=brServerLaunch.read();
						stage.hookOther.launch();
					}while(true);
				}
				catch(IOException e2)
				{
					e2.printStackTrace();
				}
			}
		});
    	trdServerLaunch.start();
    }
    
    public void setServerThread() throws IOException
    {
    	Thread trdClientPause=new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					BufferedReader brClientPause=new BufferedReader(new InputStreamReader(sSPause.getInputStream()));
					do
					{
						int signalClientPause=brClientPause.read();
						stage.pause();
					}while(true);
				}
				catch(IOException e2)
				{
					e2.printStackTrace();
				}
			}
		});
    	trdClientPause.start();
    	Thread trdClientLaunch=new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					BufferedReader brClientLaunch=new BufferedReader(new InputStreamReader(sSLaunch.getInputStream()));
					do
					{
						int signalClientLaunch=brClientLaunch.read();
						stage.hookOther.launch();
					}while(true);
				}
				catch(IOException e2)
				{
					e2.printStackTrace();
				}
			}
		});
    	trdClientLaunch.start();
    }
}
