package com.jaradgray.wally;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.UINT_PTR;
import com.sun.jna.win32.StdCallLibrary;

/**
 * The WallpaperManager class uses the JNA and JNA Platform libraries
 * to provide wallpaper-setting functionality on Windows.
 * @author Jarad
 *
 */
public class WallpaperManager {
	
		// "Declare a Java interface to hold the native library methods
		//	by extending the Library interface"
		public interface Spi extends StdCallLibrary {
			static final long SPI_SETDESKTOPWALLPAPER = 20;
			static final long SPIF_UPDATEINIFILE = 0x01;
			static final long SPIF_SENDWININICHANGE = 0x02;
			
			// "Within this interface, define an instance of the native
			//	library using the Native.load(Class) method, providing the
			//	native library interface you defined previously"
			final Spi INSTANCE = (Spi) Native.load("user32", Spi.class);
			
			// "Declare methods that mirror the functions in the target library
			//	by defining Java methods with the same name and argument types
			//	as the native function"
			boolean SystemParametersInfoA(
					UINT_PTR uiAction,
					UINT_PTR uiParam,
					String pvParam,
					UINT_PTR fWiniIni);
		}

		
		// Public methods
		
		public void setWallpaper(String path) {
			// "You can now invoke methods on the library instance just like
			//	any other Java class"
			Spi.INSTANCE.SystemParametersInfoA(
					new UINT_PTR(Spi.SPI_SETDESKTOPWALLPAPER), 
					new UINT_PTR(0),
					path,
					new UINT_PTR(Spi.SPIF_UPDATEINIFILE | Spi.SPIF_SENDWININICHANGE));
			
			System.out.println("Changed wallpaper to " + path);
		}
}
