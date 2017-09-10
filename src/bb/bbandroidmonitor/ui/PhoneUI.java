package bb.bbandroidmonitor.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import bb.bbandroidmonitor.ui.PhoneUIMode.ScreenDataModel;
import bb.bbandroidmonitor.ui.PhoneUIMode.KeyManager;
import bb.bbandroidmonitor.ui.PhoneUIMode.PhoneScreenMonitor;
import bb.bbandroidmonitor.ui.PhoneUIMode.TouchpadEvent;
import bb.bbandroidmonitor.ui.PhoneUIMode.TouchpadObservable;



public class PhoneUI extends JFrame implements PhoneScreenMonitor,PhoneUIController.IPhoneUIControllerMonitor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DrawCanvas mDrawCanvas = null;
	private JLabel mtextlabel;
	private ScreenDataModel mImageDataModel;
	private TouchpadObservable mTouchpadObservable = new TouchpadObservable();
	private KeyManager mKeyManager;
	private static final int BROAD_WIDTH = 100;
	private PhoneUIController mPhoneUIController;
	
    public PhoneUI(){
        super("彪彪");//建立新窗体
    }
    
    public void setPhoneUIController(PhoneUIController puic) {
    	mPhoneUIController = puic;
    	mPhoneUIController.registerMonitor(this);
    }
    
    public boolean init(ScreenDataModel idm) {
    	mImageDataModel = idm;
    	LOG.info("width: "+idm.getPhoneWidth()+",Height: "+idm.getPhoneHeight());
        this.setSize(idm.getPhoneWidth()+BROAD_WIDTH,idm.getPhoneHeight()+BROAD_WIDTH);//设置窗体的宽和高
        
        this.setLayout(new FlowLayout(FlowLayout.CENTER));//框架流布局且居中对齐
        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置点击关闭按钮时的默认操作
        
        PhoneUI tmp = this;
        
        this.addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent e){
                JOptionPane.showConfirmDialog(null, "真的要退出吗?");
                mPhoneUIController.unRegisterMonitor(tmp);
                mPhoneUIController.stopModels();
                //System.exit(0);
        	}
        });
        
        JPanel buttonBotinfo = new JPanel();
        
        buttonBotinfo.setLayout(new GridLayout(2,3,3,3));
        
        mtextlabel = new JLabel("@@@");
        
        JButton bBack =new JButton();
        bBack.setText("Back");
        
        bBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				mKeyManager.sendKey(KeyManager.KeyEvent.KEYCODE_BACK);
			}
        });
        
        JButton bHome =new JButton();
        bHome.setText("Home");
        
        bHome.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				mKeyManager.sendKey(KeyManager.KeyEvent.KEYCODE_HOME);
			}
        });
        
        JButton bMenu =new JButton();
        bMenu.setText("Menu");
        
        bMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				mKeyManager.sendKey(KeyManager.KeyEvent.KEYCODE_MENU);
			}
        });
        
        
        JButton bPower =new JButton();
        bPower.setText("Power");
        
        bPower.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				mKeyManager.sendKey(KeyManager.KeyEvent.KEYCODE_POWER);
			}
        });
        
        JButton bUp =new JButton();
        bUp.setText("+");
        
        bUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				mKeyManager.sendKey(KeyManager.KeyEvent.KEYCODE_VOLUME_UP);
			}
        });
        
        JButton bDown =new JButton();
        bDown.setText("-");
        
        bDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				mKeyManager.sendKey(KeyManager.KeyEvent.KEYCODE_VOLUME_DOWN);
			}
        });
        
        buttonBotinfo.add(bBack);
        buttonBotinfo.add(bHome);
        buttonBotinfo.add(bMenu);
        buttonBotinfo.add(bPower);
        buttonBotinfo.add(bUp);
        buttonBotinfo.add(bDown);
        
        this.add(buttonBotinfo); // 将标签放入窗体
        
        
    	JPanel mDrawPanel = new JPanel();
		
		mDrawCanvas = new DrawCanvas();
		mDrawCanvas.setPreferredSize(new Dimension(idm.getPhoneWidth(), idm.getPhoneHeight()));
		mDrawCanvas.addMouseListener(ma);
		mDrawCanvas.addMouseMotionListener(ma);
		
		mDrawPanel.setLayout(new BorderLayout());
		mDrawPanel.add(mDrawCanvas, BorderLayout.CENTER);
		
		JPanel jpBotinfo = new JPanel();
		jpBotinfo.setLayout(new GridLayout(2,1));
		JLabel jlPort = new JLabel("作者: biao.wei@tcl.com", JLabel.LEFT);
		jpBotinfo.add(jlPort);
		JLabel jlSerial = new JLabel("设备号: "+mImageDataModel.getSerialNumber(), JLabel.LEFT);
		jpBotinfo.add(jlSerial);
		
		mDrawPanel.add(jpBotinfo, BorderLayout.SOUTH);
		mDrawPanel.setBackground(new Color(255,255,255));
		
		this.add(mDrawPanel);
		idm.registerMonitor(this);
		this.setVisible(true);
		return true;
    	
    }
    
    public void setKeyManager(KeyManager kmg) {
    	mKeyManager = kmg;
    }
    
    MouseAdapter ma = new MouseAdapter(){  //匿名内部类，鼠标事件
        public void mouseClicked(MouseEvent e){   //鼠标完成点击事件
                int x = e.getX();  //得到鼠标x坐标
                int y = e.getY();  //得到鼠标y坐标
                String banner = "鼠标 Clicked: " + x + "," + y;
        }
        
        public void mouseDragged(MouseEvent e){
            int x = e.getX();  //得到鼠标x坐标
            int y = e.getY();  //得到鼠标y坐标
            String banner = "鼠标 Dragged: " + x + "," + y;
            mtextlabel.setText(banner);
            mTouchpadObservable.setTouchpadEvent(new TouchpadEvent(TouchpadEvent.MOVE,0,e.getX(),e.getY())).notifyAllObserver();
        }
        
        public void mouseMoved(MouseEvent e){
            int x = e.getX();  //得到鼠标x坐标
            int y = e.getY();  //得到鼠标y坐标
            String banner = "鼠标 Moved: " + x + "," + y;
        }
        
        public void mouseExited(MouseEvent e){
        }
        
        public void mouseEntered(MouseEvent e) {
        }
        
        public void mouseReleased(MouseEvent e) {
            int x = e.getX();  //得到鼠标x坐标
            int y = e.getY();  //得到鼠标y坐标
            String banner = "鼠标 Released: " + x + "," + y;
            mtextlabel.setText(banner);
            mTouchpadObservable.setTouchpadEvent(new TouchpadEvent(TouchpadEvent.UP,0,e.getX(),e.getY())).notifyAllObserver();
        }
        
        public void mouseWheelMoved(MouseWheelEvent e){
        	
        }
        
        public void mousePressed(MouseEvent e) {
        	mTouchpadObservable.setTouchpadEvent(new TouchpadEvent(TouchpadEvent.DOWN,0,e.getX(),e.getY())).notifyAllObserver();
        }
    };
    
    public boolean update(BufferedImage image) {
    	LOG.info("DrawCanvas notify update");
    	mDrawCanvas.setBufferdImage(image);
		mDrawCanvas.repaint();
    	return true;
    }
    
    public TouchpadObservable getTouchpadObservable() {
    	return mTouchpadObservable;
    }
    public boolean screenChanged(ScreenDataModel idm) {
    	return true;
    }
    
	class DrawCanvas extends Canvas{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private Image mOffScreenImage;
		private BufferedImage mBufferedImage;
		public void setBufferdImage(BufferedImage image) {
			mBufferedImage = image;
		}
		
		@Override
		public void update(Graphics g) {
			// TODO Auto-generated method stub
			LOG.info("update");
			if(mOffScreenImage == null)
				mOffScreenImage = createImage(mImageDataModel.getPhoneWidth(), mImageDataModel.getPhoneHeight());
			g.drawImage(mBufferedImage, 0, 0, null);
		}

		@Override
		public void paint(Graphics g) {
			// TODO Auto-generated method stub
			LOG.info("paint");
			g.drawImage(mBufferedImage, 0, 0, null);
		}
	}

	@Override
	public boolean update() {
		// TODO Auto-generated method stub
		return false;
	}
}
