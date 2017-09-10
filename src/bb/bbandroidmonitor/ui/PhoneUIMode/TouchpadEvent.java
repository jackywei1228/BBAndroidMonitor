package bb.bbandroidmonitor.ui.PhoneUIMode;

import bb.bbandroidmonitor.ui.LOG;

public class TouchpadEvent {
	public static final int DOWN = 0;
	public static final int UP = 1;
	public static final int MOVE = 2;
	private int mScreenIndex = 0;
	private int mAction = 0;
	private int mCoordinate_x = 0;
	private int mCoordinate_y = 0;
	
	
	public TouchpadEvent(int action,int index,int x,int y) {
		mAction = action;
		mScreenIndex = index;
		mCoordinate_x = x;
		mCoordinate_y = y;
	}
	
	public int getScreenIndex() {
		return mScreenIndex;
	}
	
	public int setScreenIndex(int index) {
		mScreenIndex = index;
		return 0;
	}
	
	public int getX() {
		return mCoordinate_x;
	}
	
	public int getY() {
		return mCoordinate_y;
	}
	
	public int setX(int x) {
		mCoordinate_x = x;
		return 0;
	}
	
	public int setY(int y) {
		mCoordinate_y = y;
		return 0;
	}
	
	public int setAction(int action) {
		mAction = action;
		return 0;
	}
	
	public String actionToStr() {
		switch(mAction) {
		case DOWN:
			return "d";
		case UP:
			return "u";
		case MOVE:
			return "m";
		default:
			LOG.error("actionToStr error");
			return null;
		}
	}
	
	public String toString() {
		return String.format("%s %d %d %d %d",actionToStr(),mScreenIndex,mCoordinate_x,mCoordinate_y,50);
	}
	
	
	public String toEnlargeScaleString(int scale) {
		if(mAction == UP) {
			return String.format("%s %d\n",actionToStr(),mScreenIndex);
		}
		return String.format("%s %d %d %d %d\n",actionToStr(),mScreenIndex,mCoordinate_x * scale, mCoordinate_y * scale ,50);
	}
}
