package bb.bbandroidmonitor.ui.PhoneUIMode;

import java.awt.image.BufferedImage;


public interface PhoneScreenMonitor {
	public boolean update(BufferedImage image);
	public boolean screenChanged(ScreenDataModel IDM);
}
