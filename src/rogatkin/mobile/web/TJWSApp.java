/** Copyright 2012 Dmitriy Rogatkin, All rights reserved.
 *  $Id: TJWSApp.java,v 1.3 2012/09/15 17:47:27 dmitriy Exp $
 */
package rogatkin.mobile.web;

import android.app.Application;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import java.lang.Thread.UncaughtExceptionHandler;

public class TJWSApp extends Application {
	protected RCServ servCtrl;

	@Override
	public void onCreate() {
		super.onCreate();
		if (Thread.getDefaultUncaughtExceptionHandler() == null)
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				
				public void uncaughtException(Thread thread, Throwable ex) {
					if (Main.DEBUG)
						Log.e(Main.APP_NAME, "Unhandled exception " + ex + " in the thread: " + thread, ex);
				}
			});
		startServe();
	}
	
	RCServ getServiceControl() {
		return servCtrl;
	}
	
	void startServe() {
		if (servCtrl != null) // sanity
			return;
		bindService(new Intent(this, TJWSServ.class), new ServiceConnection() {				
			
			public void onServiceConnected(ComponentName name, IBinder service) {
				servCtrl = RCServ.Stub.asInterface(service);
				// can send notification to activities here
			}

		
			public void onServiceDisconnected(ComponentName name) {
				if (Main.DEBUG)
					Log.d(Main.APP_NAME, "Disconnected " + name);
				servCtrl = null;
			}
		}, BIND_AUTO_CREATE);
	}

}
