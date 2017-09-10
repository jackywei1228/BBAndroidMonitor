package bb.bbandroidmonitor.ui.PhoneUIMode;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IDevice.DeviceUnixSocketNamespace;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;

import bb.bbandroidmonitor.ui.LOG;
import bb.bbandroidmonitor.ui.Util;

//负责处理图形显示
public class ScreenDataModel {
	private IDevice mIDevice = null;
	private int mPhoneWidth;
	private int mPhoneHeight;
	private int mScale = 0;
	private BufferedImage mBufferedImage;
	private String mCurAbsolutePath;
	private int PORT = 3838;
	private boolean isRunning = false;
	private Object mMessenger = new Object();
	private Queue<byte[]> dataQueue = new ConcurrentLinkedQueue<byte[]>();
	private Queue<BufferedImage> mBufferedImageQueue = new ConcurrentLinkedQueue<BufferedImage>();
	List<PhoneScreenMonitor> mPhoneScreenMonitorList = new ArrayList<PhoneScreenMonitor>();
	private int INT_BYTES = 4;
	private int HEAD_LEN = 24;
	private int mServerPID = 0;
	private Thread mServerThread = null;
	private boolean mWorkstatus = false;
	
	private String MINICAP_BIN = "minicap";
	private String REMOTE_PATH = "/data/local/tmp";
	private final String MINICAP_START_COMMAND = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %dx%d@%dx%d/0";
    private String ABI_COMMAND = "ro.product.cpu.abi";
    private String SDK_COMMAND = "ro.build.version.sdk";
    private String MINICAP_CHMOD_COMMAND = "chmod 777 %s/%s";
    private String MINICAP_SO = "minicap.so";
	
//	public ImageDataModel(IDevice phone) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException{
//		mIDevice = phone;
//		init();
//	}
	
	public ScreenDataModel(IDevice phone,int scale,int port) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException{
		mIDevice = phone;
		mScale = scale;
		PORT = port;
		init();
	}
	
	public boolean isWorking() {
		return mWorkstatus;
	}
	
	public void init() throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException {
		CountDownLatch commandCompleteLatch = new CountDownLatch(1);
		CollectingOutputReceiver lIShellOutputReceiver = new CollectingOutputReceiver(commandCompleteLatch);
		mIDevice.executeShellCommand("wm size", lIShellOutputReceiver);
		commandCompleteLatch.await(5L, TimeUnit.SECONDS);

		String line = lIShellOutputReceiver.getOutput();
		String pattern = "(\\d*)x(\\d+)";
		 

		Pattern r = Pattern.compile(pattern);
		

		Matcher m = r.matcher(line);
		if (m.find()) {
//			System.out.println("Found value: " + m.group(0));
			try {
				mPhoneWidth = Integer.parseInt(m.group(1));
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			}
			System.out.println("Found mPhoneWidth value: " + mPhoneWidth);
			
			try {
				mPhoneHeight = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			}
			
			System.out.println("Found mPhoneHeight value: " + mPhoneHeight);
			
		} else {
			System.out.println("NO MATCH");
		}
		
		
		File directory = new File("");//设定为当前文件夹 
		try{ 
		    System.out.println(directory.getCanonicalPath());//获取标准的路径 
		    System.out.println(directory.getAbsolutePath());//获取绝对路径
		    mCurAbsolutePath = directory.getAbsolutePath();
		}catch(Exception e){}
		//mBufferedImageQueue.
	}
	
	public boolean start() {
		new CmdThread().start();
		startScreenListener();
		mWorkstatus = true;
		return true;
	}
	public int getPhoneWidth(){
		return mPhoneWidth / mScale;
	}
	
	public int getPhoneHeight() {
		return mPhoneHeight / mScale;
	}
	
	public int getRealPhoneWidth(){
		return mPhoneWidth;
	}
	
	public int getRealPhoneHeight() {
		return mPhoneHeight;
	}
	
	public boolean registerMonitor(PhoneScreenMonitor psm) {
		mPhoneScreenMonitorList.add(psm);
		return true;
	}
	
	
	public boolean ScreenUpdate(BufferedImage bufferedImage) {
	    Iterator<PhoneScreenMonitor> it1 = mPhoneScreenMonitorList.iterator();
	    while(it1.hasNext()){
	      ((PhoneScreenMonitor)it1.next()).update(bufferedImage);
	      //System.out.println("ScreenUpdate");
	    }
		return true;
	}
	
	
	
	public class  UpdateThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
//			while(isRunning) {
//				if(mBufferedImage != null) {
//					ScreenUpdate(mBufferedImage);
//					mBufferedImage = null;
//				} else {
//					try {
//						Thread.sleep(17);
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//				}
//			}
			while(isRunning) {
				if(mBufferedImage != null) {
					ScreenUpdate(mBufferedImage);
					mBufferedImage = null;
				}
				try {
					synchronized(mMessenger) {
						mMessenger.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			try {
//				synchronized(mMessenger) {
//					mMessenger.wait();
//				}
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
    public void startScreenListener() {
        isRunning = true;
        new Thread(new UpdateThread()).start();
        new Thread(new ImageConverter()).start();
    }

    public void stopScreenListener() {
        isRunning = false;
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
        }
        mWorkstatus = false;
    }
    
	public class  CmdThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
	        String abi = mIDevice.getProperty(ABI_COMMAND);
	        String sdk = mIDevice.getProperty(SDK_COMMAND);
	        sdk = "android-"+sdk;
	        try {

	        	String mybinpath = mCurAbsolutePath+File.separator+"nativeTools"+File.separator+MINICAP_BIN+File.separator+"bin"+File.separator+abi+File.separator+MINICAP_BIN;
	        	LOG.info("my ImageDataModel bin path: "+mybinpath);
	        	mIDevice.pushFile(mybinpath,REMOTE_PATH+ "/" + MINICAP_BIN);
        	
	        	String mySoPath = mCurAbsolutePath+File.separator+"nativeTools"+File.separator+MINICAP_BIN+File.separator+"libs"+File.separator+sdk+File.separator+abi+File.separator+MINICAP_SO;
	        	LOG.info("my ImageDataModel mySoPath path: "+mySoPath);
	        	mIDevice.pushFile(mySoPath, REMOTE_PATH + File.separator + MINICAP_SO);
	        	
	            executeShellCommand(String.format(MINICAP_CHMOD_COMMAND,REMOTE_PATH, MINICAP_BIN));
	            
	            // 端口转发
	            mIDevice.createForward(PORT, "minicap",DeviceUnixSocketNamespace.ABSTRACT);
	            
	            //
	            final String startCommand = String.format(MINICAP_START_COMMAND,
	            		mPhoneWidth, mPhoneHeight,mPhoneWidth/mScale,mPhoneHeight/mScale);
	            LOG.info("startCommand : "+startCommand);
	            // 启动minicap服务
	            mServerThread = new Thread(new Runnable() {
	                @Override
	                public void run() {
	                    //LOG.info("minicap服务器启动");
						//executeShellCommand(startCommand);
	                	String realCommand = Util.ADB+" -s "+mIDevice.getSerialNumber()+" shell "+startCommand;
	                	LOG.info("realCommand : "+realCommand);
	                	Util.runCommand(realCommand);
	                }
	            });
	            mServerThread.start();
	            Thread.sleep(2000);
	            new Thread(new ImageBinaryFrameCollector()).start();
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
	        } catch (InterruptedException e) {
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
    
    class ImageBinaryFrameCollector implements Runnable {
        private Socket socket;
        private final int HEAD_INIT = 0;
        private final int FRAME_INIT = 1;
        private final int FRAME_PRODUCE = 2;
        @Override
        public void run() {
            LOG.debug("图片二进制数据收集器已经开启");
            // TODO Auto-generated method stub
            InputStream stream = null;
            DataInputStream input = null;

            int expectLength = 0;
            int state = HEAD_INIT;
            int frameBodyLength = 0;
            try {
                socket = new Socket("localhost", PORT);
                //设置缓存,避免win7下面收不到数据,WIN7,默认缓存是8192
                socket.setReceiveBufferSize(1024*1024*6);
                stream = socket.getInputStream();
                input = new DataInputStream(stream);
                while (isRunning) {
                    byte[] buffer;
                    int len = 0;
                    while (len == 0) {
                        len = input.available();
                    }
                    
                    switch(state) {
                    case HEAD_INIT:{
                    	expectLength = HEAD_LEN;
                    	buffer = new byte[expectLength];
                    	input.read(buffer);
                    	headParser(buffer);
                    	state = FRAME_INIT;
//                    	len = 0;//reset
                    }break;
                    
                    case FRAME_INIT:{
                    	expectLength = INT_BYTES;
                    	if(expectLength > len) {
//                    		len = 0;//reset
                    		break;
                    	}
                    	buffer = new byte[expectLength];
                    	input.read(buffer);
    	                frameBodyLength = bytes2int(ntohs(buffer));
    	                LOG.debug("FRAME_INIT : "+frameBodyLength);
    	                state = FRAME_PRODUCE;
//    	                len = 0;//reset
                    }break;
                    
                    case FRAME_PRODUCE:{
                    	expectLength = frameBodyLength;
                    	if(expectLength > len) {
//                    		len = 0;//reset
                    		break;
                    	}
//                    	len = 0;//reset
                    	buffer = new byte[expectLength];
                    	input.read(buffer);
                        dataQueue.add(buffer);
                        frameBodyLength = 0;
//                        LOG.debug("FRAME_PRODUCE : "+expectLength);
                        state = FRAME_INIT;
                    }break;
                    }
                    len = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null && socket.isConnected()) {
                    try {
                        socket.close();
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
            LOG.debug("图片二进制数据收集器已关闭");
        }
    }
    
    public byte[] ntohs(byte[] bytes){
    	byte[] fourbts = new byte[4];
		fourbts[0] = bytes[3];
		fourbts[1] = bytes[2];
		fourbts[2] = bytes[1];
		fourbts[3] = bytes[0];
    	return fourbts;
    }
    
    int headParser(byte[] bytes) {
    	ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
    	DataInputStream source =new DataInputStream(bais);
    	byte[] tempbyte = new byte[4];
    	try {
			int version = source.readUnsignedByte() & 0xFF;
			int length = source.readUnsignedByte() & 0xFF;
			source.read(tempbyte);
			int pid = bytes2int(ntohs(tempbyte));
			mServerPID = pid;
			source.read(tempbyte);
			int realWidth = bytes2int(ntohs(tempbyte));
			source.read(tempbyte);
			int realHeight = bytes2int(ntohs(tempbyte));
			source.read(tempbyte);
			int virtualWidth = bytes2int(ntohs(tempbyte));
			source.read(tempbyte);
			int virtualHeight = bytes2int(ntohs(tempbyte));
			
			int orientation = source.readUnsignedByte() & 0xFF;
			int quirks = source.readUnsignedByte() & 0xFF;
			
			LOG.debug("version: "+version);
			LOG.debug("length: "+length);
			LOG.debug("pid: "+pid);
			LOG.debug("realWidth: "+realWidth);
			LOG.debug("realHeight: "+realHeight);
			LOG.debug("virtualWidth: "+virtualWidth);
			LOG.debug("virtualHeight: "+virtualHeight);
			LOG.debug("orientation: "+orientation);
			LOG.debug("quirks: "+quirks);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return 0;
    }
    
    class ImageConverter implements Runnable {
        @Override
        public void run() {
            LOG.debug("图片生成器已经开启");
            long start = System.currentTimeMillis();
            while (isRunning) {
                byte[] binaryData = dataQueue.poll();
                if (binaryData == null) {
                    continue;
                }
				//mBufferedImageQueue.add(createImageFromByte(binaryData));
				mBufferedImage = createImageFromByte(binaryData);
				synchronized(mMessenger) {
					mMessenger.notifyAll();
				}
				long cur = System.currentTimeMillis();
//				LOG.debug("图片生成时间: "+(cur - start));
				start = cur;
            }
            LOG.debug("图片生成器关闭");
        }
    }
    
    private synchronized BufferedImage createImageFromByte(byte[] binaryData){
        InputStream in = new ByteArrayInputStream(binaryData);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }
    
	public static int byte2int(byte bt){
		int result = 0;
		result = 0x000000ff & bt;
		return result;
	}
	
	public static int bytes2int(byte[] bts) {
		int a, b, c, d;
		int result = 0;
		if(bts.length == 4){
			a = (bts[0] & 0xff) << 24; 
			b = (bts[1] & 0xff) << 16;
			c = (bts[2] & 0xff) << 8;
			d = bts[3] & 0xff;
			result = a | b | c | d;
		}
		return result;
	}
	
	public String getSerialNumber() {
		return mIDevice.getSerialNumber();
	}
}
