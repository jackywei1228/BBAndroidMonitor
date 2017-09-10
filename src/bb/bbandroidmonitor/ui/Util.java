package bb.bbandroidmonitor.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

public class Util {
	//public static String ADB = "/home/jackywei/mysoftware/android-sdk-linux_x86/platform-tools/adb";
	public static String ADB = "/home/jackywei/Android/Sdk/platform-tools/adb";
	public static ArrayList<String> runCommand(String command){
		System.out.println(command);
		String line = "null";
		ArrayList<String> linelist = new ArrayList<String>();
		try {
			Process proc = Runtime.getRuntime().exec(command);
			int i = proc.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while((line=buf.readLine())!=null){
				linelist.add(line);
				System.out.println(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.debug("!!!!!!这可能不是一个异常!!!!!!!");
		}
		return linelist;
	}
	
	public static String getAbsolutePath() {
		File directory = new File("");//设定为当前文件夹
		String absolutePath = "";
		
		try{
		    System.out.println(directory.getCanonicalPath());//获取标准的路径 
		    System.out.println(directory.getAbsolutePath());//获取绝对路径
		    absolutePath = directory.getAbsolutePath();
		}catch(Exception e){}
		
		return absolutePath;
	}
	
	public static void KillProcessInDevice(IDevice device,String ProcessName) {
		CountDownLatch commandCompleteLatch = new CountDownLatch(1);
		CollectingOutputReceiver lIShellOutputReceiver = new CollectingOutputReceiver(commandCompleteLatch);
		try {
			device.executeShellCommand("ps", lIShellOutputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = lIShellOutputReceiver.getOutput();
		//LOG.info(line);
		String[] lines = line.split("\\n");
		for(int i = 0 ; i < lines.length;i++) {
			//LOG.info(lines[i]);
			String pattern = ".*"+ProcessName+"$";
			// 创建 Pattern 对象
			Pattern r = Pattern.compile(pattern);
			
			// 现在创建 matcher 对象
			Matcher m = r.matcher(lines[i]);
			if (m.find()) {
				String row =  m.group(0);
				LOG.info("Found: " + m.group(0));
				String pid = row.split("\\s+")[1];
				LOG.info("Pid: " + pid);
				Util.runCommand("adb -s "+device.getSerialNumber()+" shell kill -9 "+pid);
//				try {
//					mIDevice.executeShellCommand("kill -9 "+pid, null);
//				} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
//						| IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					Util.runCommand("adb -s "+mIDevice.getSerialNumber()+" shell kill -9 "+pid);
//				}
			}
		}
	}
}
