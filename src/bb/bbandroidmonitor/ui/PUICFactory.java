package bb.bbandroidmonitor.ui;

import java.util.HashMap;
import java.util.Map;

import com.android.ddmlib.IDevice;

public class PUICFactory {
	private PUICFactory() {}
	private static Map<IDevice, PhoneUIController> mPhoneUIControllerMap = new HashMap<IDevice, PhoneUIController>();
	private static int DEFAULT_PORT_START = 6000;
	
//	public static PhoneUIController getPhoneUIController(IDevice device) {
//		if(mPhoneUIControllerMap.containsKey(device)) {
//			return mPhoneUIControllerMap.get(device);
//		} else {
//			PhoneUIController tmp = new PhoneUIController(device);
//			mPhoneUIControllerMap.put(device, tmp);
//			return tmp;
//		}
//	}
	
	public static PhoneUIController getPhoneUIController(IDevice device,int scale) {
		if(mPhoneUIControllerMap.containsKey(device)) {
			return mPhoneUIControllerMap.get(device);
		} else {
			PhoneUIController uicontroller = new PhoneUIController(device,scale,DEFAULT_PORT_START);
			mPhoneUIControllerMap.put(device, uicontroller);
			DEFAULT_PORT_START += 100;
			return uicontroller;
		}
	}
	
	public static void RegisterController(IDevice device,PhoneUIController uicontroller) {
		mPhoneUIControllerMap.put(device, uicontroller);
	}
	
	public static void UnRegisterController(IDevice device) {
		if(mPhoneUIControllerMap.containsKey(device)) {
			mPhoneUIControllerMap.remove(device);
		}
	}
}
