/** Copyright 2012 Dmitriy Rogatkin, All rights reserved.
 * 
 */
package rogatkin.mobile.web;

import java.net.InetAddress;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
	public static final String APP_HOME = "atjeews.home";

	static final String P_PORT = "port";

	static final String P_SSL = "ssl";

	static final String P_HIDDENAPPS = "hidden_apps";

	static final String P_ALTSETTINGS = "alt_settings";

	static final String P_ROOTAPP = "root_app";

	static final String P_PASSWRD = "password";

	static final String P_WEBROOT = "wwwroor";	// TODO shouldn't it be "wwwroot" ?

	static final String P_VIRTUAL = "virtual";

	static final String P_BINDADDR = "bind_addr";

	static final String P_HOMEDIR = "home_dir";
	
	static final String P_APPLOCK = "applock";			// should be replaced by "app_new"

	static final String P_APPREMOVE = "app_removal";

	static final String P_APPSTOP = "app_stop";

	static final String P_WEBSOCKET = "websocket";
	
	static final String P_BACKLOG = "backlog";
	
	static final String P_TV_DEVICE = "tv_run";

	public InetAddress iadr;
	public int port;
	public boolean ssl;
	public boolean websocket_enab;
	public boolean app_deploy_lock;
	public boolean app_remove_lock;
	public boolean app_stop_lock;
	public boolean logEnabled;
	public boolean virtualHost;
	public boolean useSD = true;
	public boolean TV;
	public String alienOrigin;
	public String hidden_apps;
	public String rootApp;
	public String wwwFolder;
	public String password; // admin password
	public String bindAddr;
	public int backlog;
	
	protected void store(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Main.APP_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("log_enable", logEnabled);
		editor.putBoolean(P_SSL, ssl);
		editor.putInt(P_PORT, port <= 0?8080:port);
		editor.putString(P_HIDDENAPPS, hidden_apps);
		editor.putInt(P_BACKLOG, backlog <= 0?60:backlog);
		if (alienOrigin == null)
			editor.remove(P_ALTSETTINGS);
		else
			editor.putString(P_ALTSETTINGS, alienOrigin);
		editor.putString(P_PASSWRD, password);
		editor.putBoolean(P_APPLOCK, app_deploy_lock);
		editor.putBoolean(P_APPREMOVE, app_remove_lock);
		editor.putBoolean(P_APPSTOP, app_stop_lock);
		editor.putBoolean(P_WEBSOCKET, websocket_enab);
		if (System.getProperty(APP_HOME) != null)
			editor.putString(P_HOMEDIR, System.getProperty(APP_HOME));
		else
			editor.remove(P_HOMEDIR);
		if (bindAddr == null)
			editor.remove(P_BINDADDR);
		else
			editor.putString(P_BINDADDR, bindAddr);
		editor.putString(P_WEBROOT, wwwFolder);
		if (rootApp == null)
			editor.remove(P_ROOTAPP);
		else
			editor.putString(P_ROOTAPP, rootApp);
		editor.putBoolean(P_VIRTUAL, virtualHost);
		editor.commit();
	}
	
	protected void load(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Main.APP_NAME, Context.MODE_PRIVATE);
		port = prefs.getInt(P_PORT, 8080);
		ssl = prefs.getBoolean(P_SSL, false);
		hidden_apps = prefs.getString(P_HIDDENAPPS, "");
		alienOrigin = prefs.getString(P_ALTSETTINGS, null);
		backlog = prefs.getInt(P_BACKLOG, 60);
		logEnabled = prefs.getBoolean("log_enable", false);
		bindAddr = prefs.getString(P_BINDADDR, null);
		virtualHost = prefs.getBoolean(P_VIRTUAL, false);
		websocket_enab = prefs.getBoolean(P_WEBSOCKET, false);
		app_deploy_lock = prefs.getBoolean(P_APPLOCK, false);
		app_remove_lock = prefs.getBoolean(P_APPREMOVE, false);
		app_stop_lock = prefs.getBoolean(P_APPSTOP, false);
		String home = prefs.getString(P_HOMEDIR, null);
		if (home != null) {
			System.setProperty(APP_HOME, home);
			useSD = false;
		} else {
			System.getProperties().remove(APP_HOME);
			useSD = true;
		}
		rootApp = prefs.getString(P_ROOTAPP, null);
		wwwFolder = prefs.getString(P_WEBROOT, "/sdcard");
		password = prefs.getString(P_PASSWRD, null);
		TV = prefs.getBoolean(P_TV_DEVICE, false);
	}
	
	protected void setTV(Context context, boolean on) {
	    SharedPreferences prefs = context.getSharedPreferences(Main.APP_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(P_TV_DEVICE, on);
		editor.commit(); // apply();
	}

	public boolean assess(Context context, String settings_field) {
		SharedPreferences prefs = context.getSharedPreferences(Main.APP_NAME, Context.MODE_PRIVATE);
		return  prefs.getBoolean(settings_field, false);
	}
}
