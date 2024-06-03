package com.pc.informamee.forecast;

import com.pc.informamee.common.WrongCredentialsException;
import com.pc.informamee.common.events.EventInterface;
import com.pc.informamee.common.requests.ForecastRequest;
import com.pc.informamee.common.requests.Request;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class EventUploader {
	private static final String host = "78.4.103.218";
	private static final int port = 8081;

	public static synchronized ArrayList<Integer> UploadEvent(EventInterface toUpload) throws IOException, WrongCredentialsException {
		System.out.println("Uploading event to host "+host+" on port "+port + "  ");

		ArrayList<Integer> ToReturn=null;
		Socket echoSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		try {
			echoSocket = new Socket(host, port);
			echoSocket.setKeepAlive(true);
			out = new ObjectOutputStream(echoSocket.getOutputStream());
			in = new ObjectInputStream(echoSocket.getInputStream());

			Container Instance= Container.getInstance();
			out.writeObject(Instance.getCredentials());
			Boolean Res=(Boolean)in.readObject();
			if (Res==true) {
				out.writeObject(Boolean.TRUE);
				out.writeObject(toUpload);
				Boolean r=(Boolean)in.readObject();
				if (r==false) {
					EventInterface ReceivedBack = (EventInterface) in.readObject();
					ToReturn=ReceivedBack.getInvolvedCap();
				}
			}
			else throw new WrongCredentialsException();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in!=null) in.close();
				if (out!=null) out.close();
				if (echoSocket!=null) echoSocket.close();
			} catch (IOException e) {
				//ignore
			}
			return ToReturn;
		}
	}
	public static synchronized ForecastRequest UploadEvent(ForecastRequest toUpload) throws SQLException, ClassNotFoundException, ClassCastException, IOException {
		{
			/*String MyHost="127.0.0.1";*/
			int MyPort=8082;
					System.out.println("uploading Request to host " + host + " on port " + MyPort);
					Socket echoSocket = null;
					ObjectOutputStream out = null;
					ObjectInputStream in = null;
					try {
						echoSocket = new Socket(host, MyPort);
						out = new ObjectOutputStream(echoSocket.getOutputStream());
						in = new ObjectInputStream(echoSocket.getInputStream());
						out.writeObject(toUpload);
						Object Res = in.readObject();
						if (Res instanceof Request) {
							toUpload = (ForecastRequest) Res;
						} else if (Res instanceof SQLException) {
							throw (SQLException) Res;
						} else if (Res instanceof ClassNotFoundException) {
							throw (ClassNotFoundException) Res;
						} else if (Res instanceof ClassCastException) {
							throw (ClassCastException)Res;
						}
					}
					finally {
						try {
							if (in != null) in.close();
							if (out != null) out.close();
							if (echoSocket != null) echoSocket.close();
						} catch (IOException e) {
							//ignore
						}
					}
		}
		return toUpload;
	}
}