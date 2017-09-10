package bb.bbandroidmonitor.ui.PhoneUIMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

import bb.bbandroidmonitor.ui.LOG;
import bb.bbandroidmonitor.ui.Util;

public abstract class MonitorModel {
	private IDevice mIDevice;
	
	public MonitorModel(IDevice ids) {
		mIDevice = ids;
	}
	public IDevice getDevice() {
		return mIDevice;
	}
	
	public String executeAdbShellCommand(String command) {
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
	
	public ArrayList<String> runCommandAsDaemon(String command){
		LOG.debug(command);
		String line = "null";
		ArrayList<String> linelist = new ArrayList<String>();
		try {
			Process proc = Runtime.getRuntime().exec(Util.ADB+" -s "+mIDevice.getSerialNumber()+" shell "+command);
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
    
	public abstract int startWork();
	public abstract int stopWork();
	public abstract boolean isWorking();
}
