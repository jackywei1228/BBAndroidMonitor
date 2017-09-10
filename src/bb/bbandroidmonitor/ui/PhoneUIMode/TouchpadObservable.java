package bb.bbandroidmonitor.ui.PhoneUIMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TouchpadObservable {
	List<TouchpadObserver> mTouchpadObserverList = new ArrayList<TouchpadObserver>();
	private TouchpadEvent mTouchpadEvent;
	
	public TouchpadObservable setTouchpadEvent(TouchpadEvent event) {
		mTouchpadEvent = event;
		return this;
	}
	
	public void registerObserver(TouchpadObserver observer) {
		if(observer != null) {
			mTouchpadObserverList.add(observer);
		}
	}
	
	public void unregisterObserver(TouchpadObserver observer) {
		if(observer != null) {
			mTouchpadObserverList.remove(observer);
		}
	}
	
	public void notifyAllObserver() {
	    Iterator<TouchpadObserver> it1 = mTouchpadObserverList.iterator();
	    while(it1.hasNext()){
	      ((TouchpadObserver)it1.next()).update(mTouchpadEvent);
	      //System.out.println("ScreenUpdate");
	    }
	}
}
