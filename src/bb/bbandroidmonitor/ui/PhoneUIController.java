package bb.bbandroidmonitor.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

import bb.bbandroidmonitor.ui.PhoneUIMode.KeyboardModel;
import bb.bbandroidmonitor.ui.PhoneUIMode.PhoneScreenMonitor;
import bb.bbandroidmonitor.ui.PhoneUIMode.ScreenDataModel;
import bb.bbandroidmonitor.ui.PhoneUIMode.TouchpadModel;

public class PhoneUIController {

	private IDevice mIDevice = null;
	private PhoneUI mPhoneUI;
	private ScreenDataModel mImageDataModel;
	private TouchpadModel mTouchpadModel;
	private KeyboardModel mKeyboardModel;
	private int mScale = 2;
	private int mDefaultPort = 0;
	private List<IPhoneUIControllerMonitor> mIPhoneUIControllerMonitorList = new ArrayList<IPhoneUIControllerMonitor>();

	public interface IPhoneUIControllerMonitor {
		public boolean update();
	}

	public void registerMonitor(IPhoneUIControllerMonitor monitor) {
		if (!mIPhoneUIControllerMonitorList.contains(monitor)) {
			mIPhoneUIControllerMonitorList.add(monitor);
		}
	}

	public void unRegisterMonitor(IPhoneUIControllerMonitor monitor) {
		if (mIPhoneUIControllerMonitorList.contains(monitor)) {
			mIPhoneUIControllerMonitorList.remove(monitor);
		}
	}

	private PhoneUIController(IDevice phone) {
		mIDevice = phone;
	}

	public PhoneUIController(IDevice phone, int scale, int defaultport) {
		mIDevice = phone;
		mScale = scale;
		mDefaultPort = defaultport;
	}

	public int initAllModels() {
		if (mImageDataModel == null) {
			try {
				mImageDataModel = new ScreenDataModel(mIDevice, mScale, mDefaultPort++);
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (mTouchpadModel == null) {
			mTouchpadModel = new TouchpadModel(mIDevice, mScale, mDefaultPort++);
		}
		
		if (mKeyboardModel == null) {
			mKeyboardModel = new KeyboardModel(mIDevice, mDefaultPort++);
		}
		return 0;
	}

	public int startAllModes() {
		if ((mTouchpadModel != null) && (!mTouchpadModel.isWorking())) {
			mTouchpadModel.startTouchServer();
		}

		if (mKeyboardModel != null && !mKeyboardModel.isWorking()) {
			mKeyboardModel.startWork();
		}

		if (mImageDataModel != null && !mImageDataModel.isWorking()) {
			mImageDataModel.start();
		}

		return 0;
	}

	public ScreenDataModel getImageDataModel() {
		if (mImageDataModel == null) {
			try {
				mImageDataModel = new ScreenDataModel(mIDevice, mScale, mDefaultPort++);
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mImageDataModel;
	}

	public TouchpadModel getTouchpadModel() {
		if (mTouchpadModel == null) {
			mTouchpadModel = new TouchpadModel(mIDevice, mScale, mDefaultPort++);
		}
		return mTouchpadModel;
	}

	public KeyboardModel getKeyboardModel() {
		if (mKeyboardModel == null) {
			mKeyboardModel = new KeyboardModel(mIDevice, mDefaultPort++);
		}
		return mKeyboardModel;
	}

	public void stopModels() {
		if (mIPhoneUIControllerMonitorList.size() == 0) {
			if(mTouchpadModel != null) mTouchpadModel.stopTouchServer();
			if(mImageDataModel != null) mImageDataModel.stopScreenListener();
			if(mKeyboardModel != null) mKeyboardModel.stopWork();
			mTouchpadModel = null;
			mImageDataModel = null;
			mKeyboardModel = null;
			cleanProcessInDevice();
		} else {
			LOG.info("another monitor exist!!! so we can't close!!!");
		}
	}
	private void cleanProcessInDevice() {
		CountDownLatch commandCompleteLatch = new CountDownLatch(1);
		CollectingOutputReceiver lIShellOutputReceiver = new CollectingOutputReceiver(commandCompleteLatch);
		try {
			mIDevice.executeShellCommand("ps", lIShellOutputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = lIShellOutputReceiver.getOutput();
		//LOG.info(line);
		String[] lines = line.split("\\n");
		for(int i = 0 ; i < lines.length;i++) {
			//LOG.info(lines[i]);
			String pattern = ".*app_process$";
			// 创建 Pattern 对象
			Pattern r = Pattern.compile(pattern);
			
			// 现在创建 matcher 对象
			Matcher m = r.matcher(lines[i]);
			if (m.find()) {
				String row =  m.group(0);
				LOG.info("Found: " + m.group(0));
				String pid = row.split("\\s+")[1];
				LOG.info("Pid: " + pid);
				Util.runCommand("adb -s "+mIDevice.getSerialNumber()+" shell kill -9 "+pid);
			}
		}
		Util.KillProcessInDevice(mIDevice,"minicap");
		Util.KillProcessInDevice(mIDevice,"minitouch");
	}
}
