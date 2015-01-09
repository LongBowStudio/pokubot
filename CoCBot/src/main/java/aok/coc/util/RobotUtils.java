package aok.coc.util;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import aok.coc.util.coords.Area;
import aok.coc.util.coords.Clickable;

import com.sun.jna.platform.win32.WinDef.HWND;

public class RobotUtils {

	private static final Logger	logger				= Logger.getLogger(RobotUtils.class.getName());

	public static final String	WORKING_DIR			= System.getProperty("user.dir");
	public static final int		SCREEN_WIDTH		= Toolkit.getDefaultToolkit().getScreenSize().width;
	public static final int		SCREEN_HEIGHT		= Toolkit.getDefaultToolkit().getScreenSize().height;
	public static final String	SYSTEM_OS			= System.getProperty("os.name");
	public static final String	USER_NAME			= System.getProperty("user.name");
	public static final String	USER_HOME_DIR		= System.getProperty("user.home");

	private static Robot		r;
	private static Integer		offsetX				= null;
	private static Integer		offsetY				= null;

	public static Random		random				= new Random();

	static {
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	// user32
	public static final int		WM_COMMAND			= 0x111;
	public static final int		WM_LBUTTONDOWN		= 0x201;
	public static final int		WM_LBUTTONUP		= 0x202;
	public static final int		WM_LBUTTONDBLCLK	= 0x203;
	public static final int		WM_RBUTTONDOWN		= 0x204;
	public static final int		WM_RBUTTONUP		= 0x205;
	public static final int		WM_RBUTTONDBLCLK	= 0x206;
	public static final int		WM_KEYDOWN			= 0x100;
	public static final int		WM_KEYUP			= 0x101;
	public static final int		WM_MOUSEWHEEL		= 0x20A;

	private static HWND			handler				= null;

	public static void setup(Rectangle rect) {
		offsetX = rect.x;
		offsetY = rect.y;
	}

	public static void setupWin32(HWND handler, Rectangle rect) {
		RobotUtils.handler = handler;
		offsetX = rect.x;
		offsetY = rect.y;
	}

	public static void zoomUp(int notch) throws InterruptedException {
		logger.info("Zooming out...");
		for (int i = 0; i < notch; i++) {
			User32.INSTANCE.SendMessage(handler, WM_KEYDOWN, 0x28, 0X1500001);
			Thread.sleep(350);
		}
		User32.INSTANCE.SendMessage(handler, WM_KEYDOWN, 0X11, 0X11d0001);
	}

	public static void zoomUp() throws InterruptedException {
		zoomUp(20);
	}

	public static void mouseMove(int x, int y) {
		r.mouseMove(x + offsetX, y + offsetY);
	}

	public static void leftClick(Clickable clickable, int sleepInMs) throws InterruptedException {
		leftClickWin32(clickable.getX(), clickable.getY());
		Thread.sleep(sleepInMs + random.nextInt(sleepInMs));
		//		mouseClick("left", clickable.getX() + offsetX, clickable.getY() + offsetY);
	}

	public static void leftClick(int x, int y) {
		leftClickWin32(x, y);
		//		mouseClick("left", x + offsetX, y + offsetY);
	}

	private static void leftClickWin32(int x, int y) {
		int lParam = makeParam(x, y);
		User32.INSTANCE.SendMessage(handler, WM_LBUTTONDOWN, 0x00000001, lParam);
		User32.INSTANCE.SendMessage(handler, WM_LBUTTONUP, 0x00000000, lParam);
	}

	private static int makeParam(int low, int high) {
		// to work for negative numbers
		return (high << 16) | ((low << 16) >>> 16);
	}

	public static void sleepTillClickableIsActive(Clickable clickable) throws InterruptedException {
		while (true) {
			if (isClickableActive(clickable)) {
				return;
			}
			Thread.sleep(random.nextInt(250) + 750);
		}
	}

	private static void msgBox(String Text, String Title) {
		JOptionPane.showMessageDialog(null, Text, Title, JOptionPane.PLAIN_MESSAGE); //Show message box
	}

	public static void msgBox(String Text) {
		msgBox(Text, "");
	}

	public static boolean confirmationBox(String msg, String title) {
		int result = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}

	public static BufferedImage screenShot(Area area) {
		return screenShot(area.getX1(), area.getY1(), area.getX2(), area.getY2());
	}

	public static BufferedImage screenShot(int x1, int y1, int x2, int y2) {
		return r.createScreenCapture(new Rectangle(x1 + offsetX, y1 + offsetY, x2 - x1, y2 - y1));
	}

	public static File saveScreenShot(String fileName, Area area) throws IOException {
		return saveScreenShot(fileName, area.getX1(), area.getY1(), area.getX2(), area.getY2());
	}

	public static File saveScreenShot(String fileName, int x1, int y1, int x2, int y2) throws IOException {
		if (!(fileName.toLowerCase().endsWith(".png"))) {
			fileName = fileName + ".png";
		}
		BufferedImage img = screenShot(x1, y1, x2, y2);
		File file = new File(fileName);
		ImageIO.write(img, "png", file);
		return file;
	}

	public static Color pixelGetColor(int x, int y) {
		Color pixel = r.getPixelColor(x + offsetX, y + offsetY);
		return pixel;
	}

	public static boolean isClickableActive(Clickable clickable) {
		if (clickable.getColor() == null) {
			throw new IllegalArgumentException(clickable.name());
		}

		return pixelGetColor(clickable.getX(), clickable.getY()).equals(clickable.getColor());
	}

}