package bb.bbandroidmonitor.ui.PhoneUIMode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.IDevice.DeviceUnixSocketNamespace;
import com.android.ddmlib.SyncException;

import bb.bbandroidmonitor.ui.LOG;
import bb.bbandroidmonitor.ui.Util;

//负责处理鼠标事件
public class TouchpadModel implements  TouchpadObserver {
	private IDevice mIDevice;
	private int PORT = 2013;
    private final String ABI_COMMAND = "ro.product.cpu.abi";
    private final String SDK_COMMAND = "ro.build.version.sdk";
    private String mCurAbsolutePath;
    private String MINITOUCH = "minitouch";
    private String REMOTE_PATH = "/data/local/tmp";
    private String MINITOUCH_START_COMMAND = "/data/local/tmp/minitouch";
    private int mScale = 0;
    private Socket mSocket;
    private DataOutputStream mDataOutputStream;
    private Thread mServerThread;
    private int mServerPID = 0;
    private boolean mWorkStatus = false;
//	public TouchpadModel(IDevice phone) {
//		mIDevice = phone;
//	}
	
	public TouchpadModel(IDevice phone,int scale,int port) {
		mIDevice = phone;
		mScale = scale;
		PORT = port;
		mCurAbsolutePath = Util.getAbsolutePath();
	}
	
	public void startTouchServer() {
		mServerThread = new Thread(new CmdThread());
		mServerThread.start();
		mWorkStatus = true;
	}
	
	public boolean isWorking() {
		return mWorkStatus;
	}
	
	public void stopTouchServer() {
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
            	String realCommand = Util.ADB+" -s "+mIDevice.getSerialNumber()+" shell kill -9 "+mServerPID;
            	LOG.info("stopScreenListener Command : "+realCommand);
            	Util.runCommand(realCommand);
            }
        }).start();
        
        if (mServerThread != null) {
        	mServerThread.interrupt();
        	mServerThread = null;
        }
        mWorkStatus = false;
	}
	
	public void initNetwork() {
		LOG.debug("TouchpadModel initNetwork start");
        InputStream stream = null;
        DataInputStream input = null;
        try {
        	mSocket = new Socket("localhost", PORT);
            stream = mSocket.getInputStream();
            input = new DataInputStream(stream);
            byte[] buffer;
            int len = 0;
            while (len == 0) {
            	len = input.available();
            }
            buffer = new byte[len];
            input.read(buffer);
            String serinfo = new String(buffer);
            LOG.debug("buffer: "+serinfo);
            
            String pid = serinfo.split("\n")[2].split(" ")[1];
            LOG.debug("pid: "+pid);
            
            mServerPID = Integer.parseInt(pid);
            //input.close();
            //stream.close();
            
            mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
		LOG.debug("initNetwork end");
	}
	
    class MinitouchServerThread implements Runnable {
        @Override
        public void run() {
            LOG.debug("MinitouchServerThread start");
            // TODO Auto-generated method stub
            InputStream stream = null;
            DataInputStream input = null;

            try {
            	mSocket = new Socket("localhost", PORT);
                stream = mSocket.getInputStream();
                input = new DataInputStream(stream);
                byte[] buffer;
                int len = 0;
                while (len == 0) {
                	len = input.available();
                }
                buffer = new byte[len];
                input.read(buffer);
                LOG.debug("buffer: "+buffer);
                
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mSocket != null && mSocket.isConnected()) {
                    try {
                    	mSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            LOG.debug("MinitouchServerThread end");
        }
    }
	
	public class  CmdThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//startConnect();
	        String abi = mIDevice.getProperty(ABI_COMMAND);
	        String sdk = mIDevice.getProperty(SDK_COMMAND);

	        try {
//	            // 将minitouch的可执行文件和.so文件一起push到设备中
	        	String mybinpath = mCurAbsolutePath+File.separator+"nativeTools"+File.separator+MINITOUCH+File.separator+"bin"+File.separator+abi+File.separator+MINITOUCH;
	        	LOG.info("my bin path: "+mybinpath);
	        	mIDevice.pushFile(mybinpath,MINITOUCH_START_COMMAND);
	        	executeShellCommand("chmod 777 "+MINITOUCH_START_COMMAND);
	        	
	            // 端口转发
	            mIDevice.createForward(PORT,MINITOUCH,DeviceUnixSocketNamespace.ABSTRACT);
	            
	            //
	            LOG.info("startCommand : "+MINITOUCH_START_COMMAND);
	            // 启动minicap服务
	            new Thread(new Runnable() {
	                @Override
	                public void run() {
	                    //LOG.info("minicap服务器启动");
						//executeShellCommand(startCommand);
	                	String realCommand = Util.ADB+" -s "+mIDevice.getSerialNumber()+" shell "+MINITOUCH_START_COMMAND;
	                	LOG.info("start Command : "+realCommand);
	                	Util.runCommand(realCommand);
	                	LOG.info("end Command : "+MINITOUCH_START_COMMAND);
	                }
	            }).start();
	            
	            Thread.sleep(2000);
	            initNetwork();
	            
	        }/* catch (SyncException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }  */catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (AdbCommandRejectedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (TimeoutException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (SyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void update(TouchpadEvent event) {
		// TODO Auto-generated method stub
		LOG.debug("TouchpadModel update : "+event.toEnlargeScaleString(mScale));
		if(mDataOutputStream != null) {
			try {
				//mDataOutputStream.write(event.toEnlargeScaleString(mScale).getBytes());
				LOG.debug("send : "+event.toEnlargeScaleString(mScale));
				mDataOutputStream.writeBytes(event.toEnlargeScaleString(mScale));
				mDataOutputStream.writeBytes("c\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
    private String executeShellCommand(String command) {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        try {
        	LOG.debug("executeShellCommand start : "+command);
        	mIDevice.executeShellCommand(command, output);
        	//mIDevice.executeShellCommand(arg0, arg1, arg2, arg3);
        	LOG.debug("executeShellCommand end : "+command);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String outputStr = output.getOutput();
        LOG.debug("executeShellCommand end : "+outputStr);
        return null;
    }
}
