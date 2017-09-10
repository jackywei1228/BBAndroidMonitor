package bb.bbandroidmonitor.ui;

import com.android.ddmlib.IDevice;

public class RowDataMonitor {
	private IDevice mIDevice;
	private int mScreenResolution = SCALE_1;
	public static final int SCALE_1 = 2;
	public static final int SCALE_2 = 3;
	public static final int SCALE_3 = 4;

	public RowDataMonitor(IDevice device) {
		mIDevice = device;
	}

	public IDevice getDevice() {
		return mIDevice;
	}

	public RowDataMonitor(IDevice device, int screenresolution) {
		mIDevice = device;
		mScreenResolution = screenresolution;
	}

	public void setScreenResolution(int screenresolution) {
		LOG.info(this + ": setScreenResolution : " + screenresolution);
		mScreenResolution = screenresolution;
	}

	public boolean launchPhoneUI() {
		LOG.info("mIDevice = " + mIDevice + ",mScreenResolution = " + mScreenResolution);
		PhoneUIController test = PUICFactory.getPhoneUIController(mIDevice, mScreenResolution);
		test.initAllModels();
		test.startAllModes();
		PhoneUI phoneUI = new PhoneUI();
		phoneUI.setPhoneUIController(test);
		phoneUI.init(test.getImageDataModel());
		phoneUI.getTouchpadObservable().registerObserver(test.getTouchpadModel());
		phoneUI.setKeyManager(test.getKeyboardModel());

		return true;
	}
}
