/** Copyright 2011 Dmitriy Rogatkin, All rights reserved.
 *  Copyright 2021 Hendrik Schwachenwalde, All rights reserved.
 *  $Id: Settings.java,v 1.10 2012/04/03 06:13:58 dmitriy Exp $
 */
package rogatkin.mobile.web;

import android.os.RemoteException;
import android.util.Log;
import android.content.Context ;
import android.content.pm.PackageManager.NameNotFoundException ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Acme.Utils;
import Acme.Serve.Serve;

/**
 * manages Atjeews settings
 * 
 * @author drogatki
 * 
 */
public class Settings extends HttpServlet {
	public static final String APP_NAME = Main.APP_NAME + ".Settings";
	private static final String SET_PASSWORD = "*******";
	private static final String OK_MSG = "Okay";
	private static final String FULL_ORIGIN = "///";
	private static final String LOCKED_ACCESS = "none";
	private static final String OPEN_ACCESS = "";
	private static final String[] GET_SCRIPT =
			{	"function updateBindAddr(sel){if(sel.selectedIndex==0){document.forms[0].bind_addr.value='';}else{document.forms[0].bind_addr.value=sel.options[sel.selectedIndex].value;}}",
				"function updateApps(sel){var m=\"01234567\".indexOf(sel.options[sel.selectedIndex].value);document.forms[0].lock_app.value=m&4?'false':'true';document.forms[0].deny_app_rem.value=m&2?'false':'true';document.forms[0].deny_app_stop.value=m&1?'false':'true';}",
				"function app_file_change(inp){if(confirm('Do you want to upload '+inp.value.substring(inp.value.lastIndexOf('/')+1).substring(inp.value.lastIndexOf('\\u005c')+1)+'?\\n\\nWarning: Unsaved changes will be lost.\\n\\n')===true){document.forms.upload_form.submit();}else{inp.value='';};}",
				"function access_state_click(){var val,prompt_valid,prompt_value,form;if(confirm('The current settings form values will be saved after some following security questions.\\n\\nDo you want to proceed?\\n\\n')!==true){ return;}val=false;if(confirm('Proceed allowing only this page to make settings changes?\\n\\nCancel for other options.\\n\\n')===true){val='none';}else{if(confirm('Proceed allowing only deployed apps to make settings changes?\\n\\n')===true){if(confirm('Proceed allowing all deployed apps to make settings changes?\\n\\n')===true){ val='"+FULL_ORIGIN+"';}else{prompt_valid=false;prompt_value='';while(prompt_valid===false){prompt_value=prompt('Enter a space separated list of allowed partial URL paths starting and ending with \"/\":\\n\\n',prompt_value);if(prompt_value===null||prompt_value.trim().length>0){prompt_valid=true;}else{alert('Invalid entry.\\nPlease try again.\\n\\n');}}if(prompt_value!==null){val=prompt_value.trim();if(val.indexOf(' ')===-1){ val=val+' ';}}}}else if(confirm('By continuing you allow any external program to make changes to the settings!\\n\\nThis option is dangerous and should only be used temporarily!\\n\\n')===true){val='';}else{value=false;}}if(val!==false){form=document.forms.settings;form.alt_settings.value=val;form.submit();}}",
				"function feature_click(){alert('In \"Config State\" the settings can be altered by either this form or programmatically.\\n\\n\"Config State\" ends with the first form submission excluding any file uploads.\\n\\nPermission to alter settings by external programs can be set from password protected settings forms only.\\n\\n');}",
				"var form_settings_nd=document.forms.settings;var unsaved_changes_g=false;var orig_values_g={\"root_app\":form_settings_nd.root_app.value,\"webroot\":form_settings_nd.webroot.value,\"virt_host\":form_settings_nd.virt_host.checked,\"websocket_enab\":form_settings_nd.websocket_enab.checked,\"password\":form_settings_nd.password.value,\"password2\":form_settings_nd.password2.value,\"bind_addr\":form_settings_nd.bind_addr.value,\"app_rights\":form_settings_nd.app_rights.value,\"home_dir\":form_settings_nd.home_dir.value};function update_uc(){var e,change=false;for(e in orig_values_g){if((!orig_values_g.hasOwnProperty)||orig_values_g.hasOwnProperty(e)){if(form_settings_nd[e][(e==='virt_host'||e==='websocket_enab')?'checked':'value']!==orig_values_g[e]){change=true;break;}}}unsaved_changes_g=change;form_settings_nd.dismiss.hidden=!unsaved_changes_g;}"
			};
	private static final String POST_SCRIPT = "if(self!==top){parent.postMessage({\"frame\":window.name,\"id\":\"load\"},\"*\");}";
	private static String configurator;
	private static String form_id_1;
	private static String form_id_2;
	private final TJWSServ atjeews;

	public Settings(TJWSServ server) {
		atjeews = server;
	}

	@Override
	public String getServletInfo() {
		return Serve.Identification.serverName + " "
				+ Serve.Identification.serverVersion;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		/*
		StringBuilder sb = new StringBuilder();
		sb.append(atjeews.config.rootApp).append('\b');
		if (atjeews.config.wwwFolder != null)
			sb.append(atjeews.config.wwwFolder);
		sb.append('\b').append(atjeews.config.virtualHost).append('\b').append(atjeews.config.websocket_enab).append('\b');
		if (atjeews.config.bindAddr != null)
			sb.append(atjeews.config.bindAddr);
		sb.append('\b').append(atjeews.config.app_deploy_lock).append('\b').append(atjeews.config.app_remove_lock).append('\b')
				.append(atjeews.config.app_stop_lock).append('\b').append(atjeews.config.useSD).append('\b');
		*/
		String myUrl = req.getRequestURL().toString();
		String query = req.getQueryString();
		String ref = req.getHeader("Referer");

		if (myUrl.endsWith("/favicon.ico")) {
			File icof = new File(atjeews.deployDir,"favicon.ico");
			if (icof.exists()) {
				FileInputStream fis = new FileInputStream(icof);
				if (fis != null) {
					byte[] favicon = new byte[16 * 1024]; // we don't need to loop because the upload was limited to 16K
					fis.read(favicon);
					fis.close();
					if (favicon.length > 0) {
						resp.setContentType("image/x-icon");
						resp.setContentLength(favicon.length);
						ServletOutputStream sos = resp.getOutputStream();
						sos.write((byte[]) favicon);
						sos.close();
						return;
					}
				}
			}
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		resp.setHeader("Cache-Control", "no-cache");
		resp.setDateHeader("Expires", 0);
		PrintWriter pw = resp.getWriter();
		boolean self_ref = (ref != null && (ref.equals(myUrl) || (ref.startsWith(myUrl) && ref.charAt(myUrl.length()) == '?'))) || atjeews.config.alienOrigin == null;
		boolean serve_about = "about".equals(query);
		if (serve_about) {
			resp.setContentType("application/json");
			pw.print("{\"server_name\":\"");
			pw.print(Serve.Identification.serverName);
			pw.print("\",\"server_version\":\"");
			pw.print(Serve.Identification.serverVersion.substring(Serve.Identification.serverVersion.indexOf(' ')+1));
			pw.print("\",\"atjeews\":\"");
			Context context = (Context)getServletContext().getAttribute("##RuntimeEnv");
			if (context != null)
				try {
					pw.print(context.getPackageManager()
    .getPackageInfo(context.getPackageName(), 0).versionName);
    				} catch(NameNotFoundException nnf) {
    				}
			pw.print("\"}");
			pw.close();
			return;
		}

		boolean serve_configurator = "configurator".equals(query);
		if (serve_configurator) {
			if (!self_ref || configurator == null) {
				configurator = null;
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			resp.setContentType("text/html");
			pw.print(configurator);
			pw.close();
			configurator = null;
			return;
		}

		if (query != null && !query.contains("=")) {
			Log.i(APP_NAME,"unsupported query: "+query);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String now =System.currentTimeMillis() + "";
		form_id_1 = "form_id_" + now.substring(6);
		form_id_2 = now.substring(4,9);
		String form_script = GET_SCRIPT[0] + GET_SCRIPT[1] + GET_SCRIPT[2] + GET_SCRIPT[3] + GET_SCRIPT[4];
		printHtmlHead(pw, form_script);
		pw.print("<form name=\"settings\" method=\"POST\" action=\"/settings\" autocomplete=\"off\">");
		pw.print("<table width=\"100%\"><tr><td colspan=\"2\" align=\"right\">ROOT(/)&nbsp;&nbsp;<br />web app&nbsp;&nbsp;</td><td colspan=\"4\"><select name=\"root_app\" size=\"1\" onchange=\"update_uc();\">");
		pw.print("<option value=\"-\">none</option>");
		pw.print("<option value=\"/\"");
		if ("/".equals(atjeews.config.rootApp))
			pw.print(" selected");
		pw.print(">/ (File servlet)</option>");
		for (String servletName : atjeews.servletsList) {
			if ("/".equals(servletName) || "/favicon.ico".equals(servletName) || "/settings".equals(servletName))
				continue;
			pw.print("<option");
			if (servletName.equals(atjeews.config.rootApp))
				pw.print(" selected");
			pw.print(">");
			pw.print(Utils.htmlEncode(servletName, true));
			pw.print("</option>");
		}
		pw.print("</select></td></tr>");
		String access = atjeews.config.alienOrigin;
		String password = atjeews.config.password != null ? SET_PASSWORD : "";
		pw.print("<tr><td colspan=\"2\" bgcolor=\"#EEEEEE\" align=\"right\">Serviced folder</td><td colspan=\"4\"><input type=\"text\" name=\"webroot\" value=\"");
		if (atjeews.config.wwwFolder != null)
			pw.print(Utils.htmlEncode(atjeews.config.wwwFolder, false));
		pw.print("\" onchange=\"update_uc();\"></td></tr><tr><td rowspan=\"2\" align=\"right\" valign=\"center\">Virtual&nbsp;&nbsp;<br />Host&nbsp;&nbsp;</td><td rowspan=\"2\" valign=\"center\" align=\"center\"><input type=\"checkbox\" name=\"virt_host\" value=\"true\"");
		if (atjeews.config.virtualHost)
			pw.print("true\" checked");
		pw.print(" onchange=\"update_uc();\"></td><td rowspan=\"2\" valign=\"center\" align=\"right\">Web<br />Socket</td><td rowspan=\"2\" valign=\"center\" align=\"right\"><input type=\"checkbox\" name=\"websocket_enab\" value=\"true\"");
		if (atjeews.config.websocket_enab)
			pw.print(" checked");
		pw.print(" onchange=\"update_uc();\">&nbsp;&nbsp;</td><td rowspan=\"2\"><span>&nbsp;&nbsp;|<br />&nbsp;&nbsp;|</span></td><td rowspan=\"2\" align=\"right\"");

		if (access == null) {
			pw.print(" onclick=\"feature_click();\"><i><font color=\"blue\"><u");
		} else if (password.length()>0) {
			pw.print(" onclick=\"access_state_click();\"><i><font color=\"blue\"><u");
		}
		if (access == null) {
			pw.print(">Config<br />State");
		} else if (access.length() == 0){			// instead of access.equals(OPEN_ACCESS)
			pw.print(">Open<br />Access");
		} else if (access.indexOf(' ') >= 0){
			pw.print(">Limited<br />Origin");
		} else if (access.charAt(0) == '/'){		// instead of access.equals(FULL_ORIGIN)
			pw.print(">Full<br />Origin");
		} else {									// instead of access.equals(LOCKED_ACCESS)
			pw.print(">Locked<br />Access");
		}
		if (password.length()>0 || access==null)
			pw.print("</u></font></i>");
		pw.print("</td></tr><tr></tr>");

		pw.print("<tr><td colspan=\"2\" bgcolor=\"#EEEEEE\" align=\"right\">Admin&nbsp;&nbsp;<br />password&nbsp;&nbsp;</td><td colspan=\"4\"><input type=\"password\" name=\"password\" value=\"");
		pw.print(Utils.htmlEncode(password, false));
		pw.print("\" onchange=\"update_uc();\"><br /><input type=\"password\" name=\"password2\" value=\"");
		pw.print(Utils.htmlEncode(password, false));
		pw.print("\" onchange=\"update_uc();\"></td></tr>");
		pw.print("<tr><td rowspan=\"2\" colspan=\"2\" align=\"right\" bgcolor=\"white\">Binding addr</td><td colspan=\"4\"><input type=\"text\" name=\"bind_addr\" value=\"");
		if (atjeews.config.bindAddr == null)
			// new initial default "127.0.0.1" that applies only to first "GET /settings" call after Atjeews installation
			atjeews.config.bindAddr = "127.0.0.1";
		pw.print(atjeews.config.bindAddr);
		pw.print("\" onchange=\"update_uc();\"></td></tr>");
		pw.print("<tr><td colspan=\"4\"><select onChange=\"updateBindAddr(this);update_uc();\"><option value=\"\">Custom</option>");
		for (Enumeration<NetworkInterface> en = NetworkInterface
				.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
					.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				pw.print("<option value=\"");
				pw.print(inetAddress.getHostAddress());
				pw.print("\"");
				if (inetAddress.getHostAddress().equals(atjeews.config.bindAddr))
					pw.print(" selected");
				pw.print(">");
				pw.print(inetAddress.getHostName());
				pw.print("</option>");
			}
		}
		pw.print("<option value=\"0.0.0.0\"");
		if("0.0.0.0".equals(atjeews.config.bindAddr))
			pw.print(" selected");
		pw.print(">All interfaces</option>");
		pw.print("</select></td></tr>");
		pw.print("<tr><td colspan=\"2\" bgcolor=\"#EEEEEE\" align=\"right\">Deployment&nbsp;&nbsp;</td><td align=\"right\">rights</td><td colspan=\"3\" align=\"right\"><select name=\"app_rights\" onChange=\"updateApps(this);update_uc();\">");
		int rightsNdx = (atjeews.config.app_deploy_lock ? 0 : 4) + (atjeews.config.app_remove_lock ? 0 : 2) + (atjeews.config.app_stop_lock ? 0 : 1);
		pw.print("<option value=\"7\"");
		if (rightsNdx == 7)
			pw.print(" selected");
		pw.print(">granted</option><option value=\"6\"");
		if (rightsNdx == 6)
			pw.print(" selected");
		pw.print(">can't stop</option><option value=\"5\"");
		if (rightsNdx == 5)
			pw.print(" selected");
		pw.print(">no delete</option><option value=\"3\"");
		if (rightsNdx == 3)
			pw.print(" selected");
		pw.print(">can't add</option><option value=\"1\"");
		if (rightsNdx == 1)
			pw.print(" selected");
		pw.print(">only stop</option><option value=\"0\"");
		if (rightsNdx == 0)
			pw.print(" selected");
		pw.print(">denied</option>");
		pw.print("</select></td></tr>");
		// TODO add backlog
		pw.print("<tr><td colspan=\"2\" align=\"right\">Home dir</td><td colspan=\"4\"><input type=\"text\" name=\"home_dir\" value=\"");
		pw.print(System.getProperty(Config.APP_HOME, ""));
		if (atjeews.config.useSD == false)
			pw.print("\" onchange=\"update_uc();\" disabled" );
		else
			pw.print("\" onchange=\"update_uc();\"");
		pw.print("></td></tr>");
		pw.print("<tr><td colspan=\"2\" align=\"right\"><input type=\"submit\" value=\"Save\">&nbsp;&nbsp;</td><td colspan=\"4\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"button\" name=\"dismiss\" value=\"Dismiss\" onclick=\"window.location.reload(true);\" hidden=\"true\" /></td></tr></table>");
		if (atjeews.config.alienOrigin != null) {  // always true once x-www-form-urlencoded "POST /settings" has been called
			pw.print("<input type=\"hidden\" name=\"alt_settings\" value=\"");
			pw.print(atjeews.config.alienOrigin);
			pw.print("\">");
		}
		pw.print("<input type=\"hidden\" name=\"lock_app\" value=\"");
		pw.print(atjeews.config.app_deploy_lock);
		pw.print("\"><input type=\"hidden\" name=\"deny_app_rem\" value=\"");
		pw.print(atjeews.config.app_remove_lock);
		pw.print("\"><input type=\"hidden\" name=\"deny_app_stop\" value=\"");
		pw.print(atjeews.config.app_stop_lock);
		pw.print("\"><input type=\"hidden\" name=\"");
		pw.print(form_id_1);
		pw.print("\" value=\"");
		pw.print(form_id_2);
		pw.print("\"></form>");
		pw.print("<div>Upload web application (.war) or keystore</div><form name=\"upload_form\" method=\"POST\" action=\"/settings\" enctype=\"multipart/form-data\">");
		pw.print("<input type=\"file\" name=\"app_file\" onchange=\"app_file_change(this);\"></form>");
		printFooter(pw, GET_SCRIPT[5]);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		boolean isXHR = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));

		String access = atjeews.config.alienOrigin;

		String ref = req.getHeader("Referer");
		String myUrl = req.getRequestURL().toString();
		if (myUrl.endsWith("/favicon.ico")) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		boolean self_ref = myUrl.equals(ref);
		boolean from_configurator = false; 		// needed for special response to GET /setting?configurator

		if (access!=null && access.length() > 0 && !self_ref) {
//System.err.println("potential POST access violation!\nUrl: "+myUrl+"\nRef: "+ref+"\naccess = "+access+", self_ref = "+self_ref);
			if (ref == null || access.charAt(0) != '/' || access.length() < 3) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			int path_ndx = ref.indexOf('/', 9);
			if (!myUrl.regionMatches(0,ref,0,path_ndx+1)) { // not same origin
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			} else if (!access.equals(FULL_ORIGIN)) {	// FULL_ORIGIN means all deployed apps can access settings, no filter needed
				int sep = path_ndx < 0 ? -1 : 0;
				String ref_path = path_ndx < 0 ? null : ref.substring(path_ndx);
				int acc_limit = access.length() - 2;
				while (sep >= 0 && sep < acc_limit) {
					int comp_len = access.indexOf("/ ", sep) + 1;
					if (comp_len > 2 && ref_path.regionMatches(0, access, sep, comp_len - sep)) {
						comp_len = -9;            // match found; save match flag (-9) in 'sep'
					}
					sep = comp_len + 1;
				}
				if (sep > -8) {
					resp.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				}
			}
		}

		String message = OK_MSG;
		PrintWriter pw = resp.getWriter();

		String contentType = req.getContentType();
		Log.i(APP_NAME,contentType + " POST");
		if (contentType != null
				&& contentType.toLowerCase().indexOf("multipart/form-data") >= 0) {
			MultipartParser mpp = new MultipartParser(req, resp);
			Object data = mpp.getParameter("app_file");
			String confi = (String)mpp.getParameter("configurator");
			from_configurator = confi != null;
			if (isXHR && !from_configurator) {
				resp.setContentType("application/json");
			} else {
				printHtmlHead(pw, "");
			}
			if (confi != null) {
				configurator = confi;
				message = "Configurator has been uploaded";
			} else {
				String fileName = (String) mpp.getParameter("app_file+"
						+ MultipartParser.FILENAME);
				boolean isWar = fileName.toLowerCase().endsWith(".war");
				byte[] bytes = data instanceof byte[] ? (byte[]) data : null;
				boolean emptyData = (bytes == null ? -1 : bytes.length) == 0;
				message = "Nothing uploaded";
				if (emptyData) {
					if (atjeews.config.app_remove_lock && isWar) {
						message = "Removing apps is not allowed";
						fileName = null;
					}
				} else {
					if (atjeews.config.app_deploy_lock) {
						message = (isWar ? "Deploying new apps" : "Uploading new files") + " is not allowed";
						fileName = null;
					}
				}
				if (fileName != null) {
					int sp = fileName.lastIndexOf('\\');
					if (sp >= 0)
						fileName = fileName.substring(sp + 1);
					sp = fileName.lastIndexOf('/');
					if (sp >= 0)
						fileName = fileName.substring(sp + 1);

					if (bytes == null) {
						message = "Ignoring invalid data";
					} else {
						String appName = fileName;
						File of = new File(atjeews.deployDir, fileName);
						boolean known = of.exists();
						RCServ servCtrl = ((TJWSApp) atjeews.getApplication()).getServiceControl();
						FileOutputStream fos = null;
						if (isWar) {
							if (emptyData && known) {        // storing empty war file over existing one
								appName = fileName.substring(0, fileName.length() - 4);
								try {
									servCtrl.removeApp(appName);
								} catch (RemoteException e) {
									message = "App " + fileName + " could not be removed";
								}
							} else
								fos = new FileOutputStream(of);
						} else if (TJWSServ.KEYSTORE.equals(fileName)) {
							fos = new FileOutputStream(new File(atjeews.getKeyDir(), TJWSServ.KEYSTORE));
						} else if ("favicon.ico".equals(fileName)) {
							if (emptyData) {
								if (known) {
									of.delete();
								}
							} else {
								fos = new FileOutputStream(of);
							}
						} else {
							if (emptyData && fileName.equalsIgnoreCase("RESCAN")) {
								message = "Apps rescanned";
								try {
									servCtrl.rescanApps();
								} catch (RemoteException e) {
									message = "Rescan apps failed";
								}
							} else
								message = "File " + fileName + " isn't supported type";
						}
						if (fos != null) {
							fos.write(bytes);
							fos.close();
							String msgPart = " stored";
							if (isWar) {
								msgPart = known ? " redeployed" : " deployed";
								try {
									if (known)
										servCtrl.redeployApp(appName);
									else
										servCtrl.deployApp(appName);
								} catch (RemoteException e) {
									msgPart = " not" + msgPart;
								}
							}
							message = (isWar ? "App " : "File ") + fileName + msgPart;
						}
					}
				}
			}
		} else {
			// update data
			if (isXHR && !from_configurator) {
				resp.setContentType("application/json");
			} else {
				printHtmlHead(pw, "");
			}


			if (isXHR) {
				// we only control Atjeews run from Phone Storage because we don't need extra permissions for that
				// TODO solve the permission issue for external storage
				String singleParName = null;
				Enumeration en = req.getParameterNames();
				while (en.hasMoreElements()) {
					if (singleParName != null) {
						singleParName = null;
						break;
					}
					Object o = en.nextElement();
					singleParName = (String) o;
				}
				if (singleParName != null) {
					if (!atjeews.deployDir.toString().startsWith("/data/")) {
						Log.e(APP_NAME,"XHR > "+atjeews.deployDir);
						resp.sendError(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
					if (access == null || access.length() > 0) {
						// only same origin calls, possibly restricted to certain apps, are allowed, i.e.
						// only access value strings except for the empty one allow execution of this block
						String singleParVal = req.getParameter(singleParName);
						boolean warPar = singleParVal.endsWith(".war");
						RCServ servCtrl = ((TJWSApp) atjeews.getApplication()).getServiceControl();
						if ("rescan_apps".equals(singleParName) && "y".equalsIgnoreCase(singleParVal)) {
							message = "Apps rescanned";
							try {
								servCtrl.rescanApps();
							} catch (RemoteException e) {
								message = "Rescan apps failed";
							}
						} else if ("remove_app".equals(singleParName) && warPar) {
							message = "'" + singleParVal + "' has been removed";
							File wf = new File(atjeews.deployDir, singleParVal);
							try {
								if (wf.exists() && wf.isFile()) {
									wf.delete();
									servCtrl.rescanApps();
									servCtrl.removeApp(singleParVal.substring(0, singleParVal.length() - 4));
									servCtrl.stopApp(singleParVal.substring(0, singleParVal.length() - 4));
								} else
									message = "'" + singleParVal + "' file didn't exist";
							} catch(RemoteException e){
								message = "Removing '" + singleParVal + "' failed";
							}
						} else if ("stop_app".equals(singleParName) && warPar) {
							message = "'" + singleParVal + "' has been stopped";
							try {
								servCtrl.stopApp(singleParVal.substring(0, singleParVal.length() - 4));
							} catch (RemoteException e) {
								message = "Stopping '" + singleParVal + "' failed";
							}
						}
						if (message.equals(OK_MSG))
							message = "Special request '" + singleParName + "' failed";

						pw.print("{\"response\":\"");
						pw.print(message);
						pw.print("\"}");
						pw.close();
					} else {
						resp.sendError(HttpServletResponse.SC_FORBIDDEN);
					}
					return;
				}
			}

			if (self_ref) {
				boolean isUptoDate = false;
				if (form_id_1 != null && form_id_2 != null) {
					String fid = req.getParameter(form_id_1);
					isUptoDate = form_id_2.equals(fid);
				}
				if (!isUptoDate) {
					form_id_1 = form_id_2 = null;
					pw.print("<h2><center>Settings changes rejected<br/>because of stale form data<br/><hr/><br/><a href=\"/settings\">Return to settings form</a></center><br/><hr/></h2>");
					printFooter(pw, null);
					pw.close();
					return;
				}
			}
			form_id_1 = form_id_2 = null;

			String alt_settings = req.getParameter("alt_settings");
			atjeews.config.alienOrigin = alt_settings!=null ? alt_settings : access != null ? access : LOCKED_ACCESS; // or for LOCKED_ACCESS: (atjeews.config.origin_lock ? LOCKED_ACCESS : OPEN_ACCESS);
			String val = req.getParameter("root_app");
			if ("-".equals(val))
				atjeews.config.rootApp = null;
			else
				atjeews.config.rootApp = val;
			val = req.getParameter("webroot");
			if (val != null) {
				if (val.trim().length() != 0) {
					atjeews.config.wwwFolder = val;
					File wwwFolder = new File(atjeews.config.wwwFolder);
					if (wwwFolder.exists() == false || wwwFolder.isDirectory() == false) {
						atjeews.config.wwwFolder = null;
						message = "Invalid web folder";
					} else
						atjeews.updateWWWServlet();
				}
			} else
				message = "Missing web folder";
			val = req.getParameter("hidden_apps");
			boolean altHiddenApps = val != null;
			if (altHiddenApps)
				atjeews.config.hidden_apps = val.trim();
			val = req.getParameter("password");
			if (SET_PASSWORD.equals(val) == false) {
				if (val != null && val.length() > 0) {
					if (val.equals(req.getParameter("password2")))
						atjeews.config.password = val;
					else
						message = "Passwords do not match";
				} else if (atjeews.config.password != null)
					atjeews.config.password = null;
				atjeews.updateRealm();
			}
			val = req.getParameter("bind_addr");
			if (val != null && val.length() > 0) {
				if (val.trim().length() > 0)
					atjeews.config.bindAddr = val;
			} else
				atjeews.config.bindAddr = null;
			atjeews.config.virtualHost = Boolean.TRUE.toString().equals(req.getParameter("virt_host"));
			atjeews.config.websocket_enab = Boolean.TRUE.toString().equals(req.getParameter("websocket_enab"));
			val = req.getParameter("home_dir");
			if (val != null && val.length() > 0) {
				System.setProperty(Config.APP_HOME, val);
			} else
				System.getProperties().remove(Config.APP_HOME);
			atjeews.config.app_deploy_lock = Boolean.TRUE.toString().equals(req.getParameter("lock_app"));
			atjeews.config.app_remove_lock = Boolean.TRUE.toString().equals(req.getParameter("deny_app_rem"));
			atjeews.config.app_stop_lock = Boolean.TRUE.toString().equals(req.getParameter("deny_app_stop"));
			atjeews.deployDir = null;
			atjeews.initDeployDirectory();
			atjeews.storeConfig();
			if (altHiddenApps) {
				RCServ servCtrl = ((TJWSApp) atjeews.getApplication()).getServiceControl();
				try {
					servCtrl.rescanApps();
				} catch (RemoteException e) {}
			}
		}
		if (from_configurator || !isXHR) {
			pw.print("<h2><center>" + message + "<br/><hr/><br/><a href=\"/settings\">Return to settings form</a></center><br/><hr/></h2>");
			printFooter(pw, from_configurator ? POST_SCRIPT : null);
		} else {
			pw.print("{\"response\":\"");
			pw.print(message);
			pw.print("\"}");
		}
		pw.close();
	}

	@Override
	protected long getLastModified(HttpServletRequest req) {
		return -1;
	}

	private void printHtmlHead(PrintWriter pw, String script) {
		pw.print("<html><head><title>Settings - ");
		pw.print(Main.APP_NAME);
		pw.print("</title><meta name=\"viewport\" content=\"width=device-width, user-scalable=no\" />");
		if (script!=null) {
			pw.print("<script type=\"text/javascript\">");
			pw.print(script);
			pw.print("</script>");
		}
		pw.print("</head><body bgcolor=\"#D1E9FE\">");
	}

	private void printFooter(PrintWriter pw, String script) {
		pw.print("<center>");
		pw.print(Serve.Identification.serverIdHtml);
		pw.print("<br/><a href=\"https://drogatkin.github.io\">Privacy Policy</a></center>");
		if (script!=null) {
			pw.print("<script type=\"text/javascript\">");
			pw.print(script);
			pw.print("</script>");
		}
		pw.print("</body></html>");
	}
}
