package org.nodex.android.hotspot;
import android.app.Application;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import org.nodex.core.api.lifecycle.IoExecutor;
import org.nodex.android.hotspot.HotspotState.WebsiteConfig;
import org.nodex.android.qrcode.QrCodeUtils;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.core.util.NetworkUtils.getNetworkInterfaces;
import static org.nodex.android.hotspot.WebServer.PORT;
import static org.nodex.android.qrcode.QrCodeUtils.HOTSPOT_QRCODE_FACTOR;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class WebServerManager {
	interface WebServerListener {
		@IoExecutor
		void onWebServerStarted(WebsiteConfig websiteConfig);
		@IoExecutor
		void onWebServerError();
	}
	private static final Logger LOG =
			getLogger(WebServerManager.class.getName());
	private final WebServer webServer;
	private final DisplayMetrics dm;
	private volatile WebServerListener listener;
	@Inject
	WebServerManager(Application ctx) {
		webServer = new WebServer(ctx);
		dm = ctx.getResources().getDisplayMetrics();
	}
	@UiThread
	void setListener(WebServerListener listener) {
		this.listener = listener;
	}
	@IoExecutor
	void startWebServer() {
		try {
			webServer.start();
			onWebServerStarted();
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			listener.onWebServerError();
		}
	}
	@IoExecutor
	private void onWebServerStarted() {
		String url = "http:
		InetAddress address = getAccessPointAddress();
		if (address == null) {
			LOG.info(
					"Could not find access point address, assuming 192.168.49.1");
		} else {
			if (LOG.isLoggable(INFO)) {
				LOG.info("Access point address " + address.getHostAddress());
			}
			url = "http:
		}
		Bitmap qrCode = QrCodeUtils.createQrCode(
				(int) (dm.heightPixels * HOTSPOT_QRCODE_FACTOR), url);
		listener.onWebServerStarted(new WebsiteConfig(url, qrCode));
	}
	@IoExecutor
	void stopWebServer() {
		webServer.stop();
	}
	@Nullable
	private static InetAddress getAccessPointAddress() {
		for (NetworkInterface i : getNetworkInterfaces()) {
			if (i.getName().startsWith("p2p")) {
				for (InterfaceAddress a : i.getInterfaceAddresses()) {
					if (a.getAddress().getAddress().length == 4)
						return a.getAddress();
				}
			}
		}
		return null;
	}
}