<%@ page language="java" import="android.content.ContentResolver,
android.database.Cursor,android.provider.ContactsContract, android.content.Context" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
   <meta name="viewport" content="width=device-width, user-scalable=no" />
   <title>My Contacts First Atjeews app demo</title>
   <style>
      tr:nth-child(even) {
        background-color: #f2f2f2;
      }
</style>
</head>
<body>
<h1>My Android contacts list</h1>
<table>
  <tr><th>Name</th><th>Phone</th><th>E-mail</th></tr>
<%
        Context context = (Context)application.getAttribute("##RuntimeEnv");
         ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		String colors[] = {"blue","red", "purple", "green"};
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
					    	int colorIdx = limit(pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
						    out.print("<span style=\"color:");
						    out.print(colors[colorIdx]);
						    out.print("\">");
							out.print( pCur.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                             out.print("</span><br/>");
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
						int emailTypeInt = emailCur.getInt(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
						String customLabel = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
                        CharSequence emailType1 = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.getResources(), emailTypeInt, customLabel);
                        out.print("<span style=\"color:blue\">");
                        out.print(emailType1);
                        out.print(":</span><br/>< a href=\"mailto:");
                        out.print(email);
                        out.print("\">");
						out.print(email);
						out.print("< /a><br/>");
					}
					emailCur.close();
                   out.print("</td></tr>");
			}
		} else {
			out.print("<tr><td colspan=3>No contacts</td></tr>");
		}
		cur.close(); // finally
	%>
	<%!

public int limit(int i){
   if (i < 0 || i > 3)
      return 0;

   return i;
}

%>


  </table>
</body>
</html>