package bb.bbandroidmonitor.ui.PhoneUIMode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;

import bb.bbandroidmonitor.ui.LOG;
import bb.bbandroidmonitor.ui.Util;

public class KeyboardModel extends MonitorModel implements KeyManager{
	private static final String INPUT_JAR = "/data/local/tmp/inputserver.jar";
	private static final String INPUT_SHELL = "/data/local/tmp/inputserver";
	private int PORT = 6001;
	private Socket mSocket;
	private DataOutputStream mDataOutputStream;
	private int mServerPID;
	private boolean mInvalid = false;
	private boolean mWorkStatus = false;
	
	public KeyboardModel(IDevice ids,int port) {
		super(ids);
		PORT = port;
	}

	@Override
	public int startWork() {
		// TODO Auto-generated method stub
		LOG.info("KeyboardModel : startWork()");
		pushDependsFile();
		initRemoteServer();
		createForward();
		initNetwork();
		mWorkStatus = true;
		return 0;
	}

	@Override
	public int stopWork() {
		// TODO Auto-generated method stub
		try {
			if(mDataOutputStream != null) {
				mDataOutputStream.close();
				mDataOutputStream = null;
			}
			
			if(mSocket != null) {
				mSocket.close();
				mSocket = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        new Thread(new Runnable() {
            @Override
            public void run() {
            	String realCommand = Util.ADB+" -s "+getDevice().getSerialNumber()+" shell kill -9 "+mServerPID;
            	LOG.info("stopWork Command start: "+realCommand);
            	Util.runCommand(realCommand);
            	LOG.info("stopWork Command end: "+realCommand);
            }
        }).start();
        
        mWorkStatus = true;
        
		return 0;
	}
	
	public int pushDependsFile() {
		String abi = getDevice().getProperty("ro.product.cpu.abi");
    	String myjarpath = Util.getAbsolutePath()+File.separator+"nativeTools"+File.separator+"input"+File.separator+abi+File.separator+"inputserver.jar";
    	LOG.info("my bin path: "+myjarpath);
    	String myshscriptpath = Util.getAbsolutePath()+File.separator+"nativeTools"+File.separator+"input"+File.separator+"inputserver";
    	try {
			getDevice().pushFile(myjarpath,INPUT_JAR);
			getDevice().pushFile(myshscriptpath,INPUT_SHELL);
		} catch (SyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdbCommandRejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	executeAdbShellCommand("chmod 777 "+INPUT_JAR);
    	executeAdbShellCommand("chmod 777 "+INPUT_SHELL);
		return 0;
	}
	
	public int createForward() {
//		getDevice().createForward(PORT,MINITOUCH,DeviceUnixSocketNamespace.ABSTRACT);
		try {
			Runtime.getRuntime().exec(Util.ADB+" -s "+getDevice().getSerialNumber()+" forward tcp:"+PORT+" tcp:6000 ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mInvalid = true;
		}
		return 0;
	}
	
	public int initRemoteServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("initRemoteServer");
            	String realCommand = Util.ADB+" -s "+getDevice().getSerialNumber()+" shell "+INPUT_SHELL;
            	LOG.info("initRemoteServer start Command : "+realCommand);
            	Util.runCommand(realCommand);
            	LOG.info("initRemoteServer end Command : "+realCommand);
            	mInvalid = true;
            }
        }).start();
        
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	
	public void initNetwork() {
		LOG.debug("Keyboard initNetwork start");
        InputStream stream = null;
        DataInputStream input = null;
        try {
        	mSocket = new Socket("localhost", PORT);
            stream = mSocket.getInputStream();
            input = new DataInputStream(stream);
            byte[] buffer;
            int len = 0;
            int times = 0;
            while (len == 0 && times < 10) {
            	len = input.available();
            	times++;
            	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            if(times < 9) {
                buffer = new byte[len];
                input.read(buffer);
                String serinfo = new String(buffer);
                LOG.debug("buffer: "+serinfo);
                String pid = serinfo.replaceAll("\\s*", "");
                LOG.debug("Keyboard Server pid: "+pid);
                mServerPID = Integer.parseInt(pid);
            }
            mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
            mInvalid = false;
        } catch (IOException e) {
            e.printStackTrace();
            mInvalid = false;
        }
		LOG.debug("initNetwork end");
	}
	
	@Override
    public boolean sendKey(int keyval) {
		LOG.debug("KeyboardModel sendKey : "+"keyevent "+keyval+",mInvalid = " + mInvalid);
		if(mInvalid) {
			return false;
		}
		if(mDataOutputStream != null) {
			try {
				//mDataOutputStream.write(event.toEnlargeScaleString(mScale).getBytes());
				mDataOutputStream.writeBytes("keyevent "+keyval+"\n");
				//mDataOutputStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	return true;
    }

	@Override
	public boolean isWorking() {
		// TODO Auto-generated method stub
		return mWorkStatus;
	}
}
