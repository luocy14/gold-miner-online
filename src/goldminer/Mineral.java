package goldminer;

import java.awt.*;

import javax.swing.ImageIcon;


public abstract class Mineral {
    double x;
    double y;
    double r;
    int value;
    int density;
    Mineral(double x,double y,double r,int value,int density){
    	this.x=x;
    	this.y=y;
    	this.r=r;
    	this.value=value;
    	this.density=density;
    }
    abstract void paint(Graphics g);
    
    void refresh(double newX, double newY) {
    	x = newX;
    	y = newY;
    }

    void hooked(Stage stage, int i){
    	stage.mineralList.remove(i);
    }
}

class Rock extends Mineral{
	Rock(double x, double y) {
		super(x, y, 20, 20, 20);
	}
	void paint(Graphics g) {
		Image icon = new ImageIcon("res/images/mine_rock_b.png").getImage();
		g.drawImage(icon, (int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r), null);
	}
}

class Gold extends Mineral{
	
	Gold(double x, double y, double r, int value) {
		super(x,y,r,value,10);
	}
	void paint(Graphics g) {
		Image icon = new ImageIcon("res/images/mine_gold_b_1.png").getImage();
		g.drawImage(icon, (int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r), null);
	}
}

class Mouse extends Mineral {
	int movingDirection;
	double movingSpeed;
	double movingRange;
	int paintCount;
	boolean isHooked;
	
	Mouse(double x, double y,int movingDirection, double movingSpeed) {
		super(x, y, 10, 5, 5);
		this.movingDirection = movingDirection;
		this.movingSpeed = movingSpeed;
		this.paintCount = 0;
		this.isHooked = false;
	}
	
	void runMouse() {
		x += movingDirection * movingSpeed;
		if (x <= 0 || x >= 800) {
			movingDirection  = -movingDirection;
		}
	}
	
	void hooked(Stage stage, int i){
    	stage.mineralList.remove(i);
    	isHooked = true;
    }
	
	void paint(Graphics g) {
		String suffix;
		if (movingDirection > 0) {
			suffix = new String("_right.png");
		} else {
			suffix = new String("_left.png");
		}
		String prefix = new String("res/images/mouse");
		
		Image icon = new ImageIcon(prefix + (paintCount+1) + suffix).getImage();
		
		if (!isHooked){
			paintCount += movingSpeed / 7 + 1;
			paintCount = paintCount % 4;
		}
		g.drawImage(icon, (int)(x-2*r), (int)(y-r), (int)(4*r), (int)(2*r), null);
	}
}

class Bag extends Mineral {
	Bag(double x,double y,int rand)
	{
		super(x,y,20,rand,5);
	}

	void paint(Graphics g) {
		Image icon=new ImageIcon("res/images/mine_bag.png").getImage();
		g.drawImage(icon,(int)(x-r),(int)(y-r),(int)(2*r),(int)(2*r),null);
	}
}
