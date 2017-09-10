package bb.bbandroidmonitor.ui.PhoneUIMode;

public interface KeyManager {
	
	public static class KeyEvent{
	    public static final int KEYCODE_HOME = 3;
	    public static final int KEYCODE_BACK = 4;
	    public static final int KEYCODE_VOLUME_UP = 24;
	    public static final int KEYCODE_VOLUME_DOWN = 25;
	    public static final int KEYCODE_POWER = 26;
	    public static final int KEYCODE_MENU = 82;
	    public static final int KEYCODE_APP_SWITCH = 187;
	}
	
	public boolean sendKey(int keyval);
}
