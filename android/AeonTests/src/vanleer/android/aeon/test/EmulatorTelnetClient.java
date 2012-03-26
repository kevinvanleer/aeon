package vanleer.android.aeon.test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

public final class EmulatorTelnetClient {
	static void sendLocation(double latitude, double longitude) {
		try {
			Socket socket = new Socket("10.0.2.2", 5554); // usually 5554
			socket.setKeepAlive(true);
			String str = "geo fix " + longitude + " " + latitude ;
			Writer w = new OutputStreamWriter(socket.getOutputStream());
			w.write(str + "\r\n");
			w.flush();
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	static void unlockScreen() {
		try {
			Socket socket = new Socket("10.0.2.2", 5554); // usually 5554
			socket.setKeepAlive(true);
			String str = "event send EV_KEY:KEY_MENU:1 EV_KEY:KEY_MENU:0";
			Writer w = new OutputStreamWriter(socket.getOutputStream());
			w.write(str + "\r\n");
			w.flush();
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
