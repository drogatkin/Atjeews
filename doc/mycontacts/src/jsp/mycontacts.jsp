<%@ page language="java" import="android.content.ContentResolver,
android.database.Cursor,android.provider.ContactsContract, android.content.Context" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
   <meta name="viewport" content="width=device-width, user-scalable=no" />
   <title>My Contacts First Atjeews app demo</title>
</head>
<body>
<h1>My Android contacts list</h1>
<table>
  <tr><th>Name</th><th>Phone</th><th>E-mail</th></tr>
<%
        Context context = (Context)application.getAttribute("##RuntimeEnv");
         ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
					String id = (cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
					out.print("<tr><td>");
					out.print(cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
					out.print("</td><td>");
					if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
						Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
						while (pCur.moveToNext()) {
						    out.print("<span style=\"color:green\">");
							out.print(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
							out.print("</span>&nbsp;");
							out.print( pCur.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                             out.print("<br/>");
						}
						pCur.close();
					}
					out.print("</td><td>");

					Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);
					while (emailCur.moveToNext()) {
						// This would allow you get several email addresses
						// if the email addresses were stored in an array
						String email = emailCur.getString(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						String emailType = emailCur.getString(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
						out.print(email);
						out.print("&nbsp;<span style=\"color:blue\">");
						out.print(emailType);
						out.print("</span><br/>");
					}
					emailCur.close();
                   out.print("</td></tr>");
			}
		} else {
			out.print("<tr><td colspan=3>No contacts</td></tr>");
		}
		cur.close(); // finally
	%>
  </table>
</body>
</html>