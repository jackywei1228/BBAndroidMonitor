package bb.bbandroidmonitor.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

import bb.bbandroidmonitor.ui.Util;
import bb.bbandroidmonitor.ui.PhoneUIMode.KeyManager;


class BBArrayListComboBoxModel extends AbstractListModel implements ComboBoxModel {
	private Object selectedItem;

	private ArrayList anArrayList;

	public BBArrayListComboBoxModel(ArrayList arrayList) {
		anArrayList = arrayList;
	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(Object newValue) {
		selectedItem = newValue;
	}

	public int getSize() {
		return anArrayList.size();
	}

	public Object getElementAt(int i) {
		return anArrayList.get(i);
	}
}

public class MainUi {
	public static final int SETUP_MAX_LINES = 100;
	private static ArrayList<String> mSetupList = new ArrayList<String>();
	private static ArrayList<RowDataMonitor> mRowDataMoniterList = new ArrayList<RowDataMonitor>();
	
	private static Map<JComboBox, RowDataMonitor> mJComboBoxMap = new HashMap<JComboBox, RowDataMonitor>();
	
	public static void readSetupFile() {
		File file = new File("setup.ini");
		BufferedReader reader = null;
        try {
            System.out.println("读取配置文件:.......");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
            	LOG.debug("line " + line + " : " + tempString);
                line++;
                if(mSetupList.size() < SETUP_MAX_LINES) {
                	mSetupList.add(tempString);
                }
            }
            reader.close();
            Util.ADB = mSetupList.get(0).replaceAll("\\s*", "");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }
	
	
	
	public static void printFozu() {
		LOG.info("////////////////////////////////////////////////////////////////////");
		LOG.info("//                          _ooOoo_                               //");
		LOG.info("//                         o8888888o                              //");
		LOG.info("//                         88\" . \"88                              //");
		LOG.info("//                         (| ^_^ |)                              //");
		LOG.info("//                         O\\  =  /O                              //");
		LOG.info("//                      ____/`---'\\____                           //");
		LOG.info("//                    .'  \\|     |//  `.                         //");
		LOG.info("//                   /  \\\\|||  :  |||//  \\                        //");
		LOG.info("//                  /  _||||| -:- |||||-  \\                       //");
		LOG.info("//                  |   | \\\\\\  -  /// |   |                       //");
		LOG.info("//                  | \\_|  ''\\---/''  |   |                       //");
		LOG.info("//                  \\  .-\\__  `-`  ___/-. /                       //");
		LOG.info("//                ___`. .'  /--.--\\  `. . ___                     //");
		LOG.info("//              .\"\" '<  `.___\\_<|>_/___.'  >'\"\".                  //");
		LOG.info("//            | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |                 //");
		LOG.info("//            \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /                 //");
		LOG.info("//      ========`-.____`-.___\\_____/___.-`____.-'========         //");
		LOG.info("//                           `=---='                              //");
		LOG.info("//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^        //");
		LOG.info("//         佛祖保佑       永无BUG     永不修改                        //");
		LOG.info("////////////////////////////////////////////////////////////////////");
		LOG.info("作者: 彪彪 		邮箱: 258106975@qq.com");
		LOG.info("////////////////////////////////////////////////////////////////////");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException, InterruptedException {
    	readSetupFile();
    	LOG.debug("adb : "+Util.ADB);
    	IDevice device;
        AndroidDebugBridge.init(false);
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(Util.ADB, false);
        printFozu();
        waitForDevice(bridge);
        IDevice devices[] = bridge.getDevices();
        LOG.debug("如果下面出错,请检查 adb devices 命令是否正常.......");
        initRowDataMoniterList(devices);
        initRowsDataShow();
    }

    private static void initRowDataMoniterList(IDevice devices[]) {
    	mRowDataMoniterList.clear();
    	for(int i = 0; i < devices.length;i++) {
    		mRowDataMoniterList.add(new RowDataMonitor(devices[i]));
    	}
    }
    
    private static int[] getWH(IDevice device){
    	int[] store = {0,0};
		CountDownLatch commandCompleteLatch = new CountDownLatch(1);
		CollectingOutputReceiver lIShellOutputReceiver = new CollectingOutputReceiver(commandCompleteLatch);
		
		try {
			device.executeShellCommand("wm size", lIShellOutputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			commandCompleteLatch.await(5L, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String line = lIShellOutputReceiver.getOutput();
		String pattern = "(\\d*)x(\\d+)";

		Pattern r = Pattern.compile(pattern);

		Matcher m = r.matcher(line);
		if (m.find()) {
			try {
				store[0] = Integer.parseInt(m.group(1));
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			}
			System.out.println("Found mPhoneWidth value: " + store[0]);
			
			try {
				store[1] = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			}
			
			System.out.println("Found mPhoneHeight value: " + store[1]);
			
		} else {
			System.out.println("NO MATCH");
		}    	
    	return store;
    }
    
    private static void initRowsDataShow() {
		JFrame frame = new JFrame("彪彪");
		frame.setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel controlPanl = new JPanel();
		
		controlPanl.setLayout(new GridLayout(mRowDataMoniterList.size(), 3));
		
		for (int i = 0; i < mRowDataMoniterList.size(); i++) {
			IDevice device = mRowDataMoniterList.get(i).getDevice();
			int[] wh = getWH(device);
			RowDataMonitor rdm = mRowDataMoniterList.get(i);
			ArrayList<Object> arrayList = new ArrayList<Object>();
			arrayList.add(String.format("%dx%d",wh[0]/RowDataMonitor.SCALE_1,wh[1]/RowDataMonitor.SCALE_1));
			arrayList.add(String.format("%dx%d",wh[0]/RowDataMonitor.SCALE_2,wh[1]/RowDataMonitor.SCALE_2));
			arrayList.add(String.format("%dx%d",wh[0]/RowDataMonitor.SCALE_3,wh[1]/RowDataMonitor.SCALE_3));
			BBArrayListComboBoxModel model = new BBArrayListComboBoxModel(arrayList);
			JComboBox comboBox = new JComboBox(model);
			comboBox.setSize(new Dimension(200, 30));
			comboBox.setSelectedIndex(0);
			mJComboBoxMap.put(comboBox, mRowDataMoniterList.get(i));
			
			comboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					if (e.getSource() == comboBox) {
						int index = comboBox.getSelectedIndex();
						System.out.println("index : " + index);
						rdm.setScreenResolution(index+RowDataMonitor.SCALE_1);
					}
				}
			});


			JLabel jtf3 = new JLabel(i + ":"+device.getName());
			jtf3.setSize(new Dimension(200, 30));

			JButton b = new JButton("启动");
			b.setSize(new Dimension(200, 30));
			
	        b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					rdm.launchPhoneUI();
					comboBox.setEnabled(false);
				}
	        });
	        controlPanl.add(jtf3);
	        controlPanl.add(comboBox);
	        controlPanl.add(b);
		}
		
		controlPanl.setSize(new Dimension(600, 30 * mRowDataMoniterList.size()));
		frame.add(controlPanl);
		
		JPanel jpBotinfo = new JPanel();
		jpBotinfo.setLayout(new GridLayout(2,1));
		JLabel jlPort = new JLabel("作者: 彪彪", JLabel.LEFT);
		jpBotinfo.add(jlPort);
		JLabel jlSerial = new JLabel("Email: biao.wei@tcl.com", JLabel.LEFT);
		jpBotinfo.add(jlSerial);
		
		frame.add(jpBotinfo);
		
		frame.setSize(new Dimension(900, 30 * mRowDataMoniterList.size()+100));
		frame.setVisible(true);
    }
    
    private static void waitForDevice(AndroidDebugBridge bridge) {
        int count = 0;
        while (!bridge.hasInitialDeviceList()) {
            try {
                Thread.sleep(100);
                count++;
            } catch (InterruptedException ignored) {
            }
            if (count > 300) {
                System.err.print("Time out");
                break;
            }
        }
    }
}  