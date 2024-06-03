package com.pc.informamee.forecast;

import com.pc.informamee.common.Credentials;
import com.pc.informamee.common.WrongCredentialsException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CredentialsChecker {
    public static void CheckCredentials(Credentials toCheck) throws WrongCredentialsException, IOException
    {
        String Host = "78.4.103.218";
        int Port = 8081;
        Socket echoSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            echoSocket = new Socket(Host, Port);
            echoSocket.setKeepAlive(true);
            out = new ObjectOutputStream(echoSocket.getOutputStream());
            in = new ObjectInputStream(echoSocket.getInputStream());
            out.writeObject(toCheck);
            Boolean Res=(Boolean)in.readObject();
            if (Res.booleanValue()==true) out.writeObject(Boolean.FALSE);
            else throw new WrongCredentialsException();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally
        {
            try {
                if (in!=null) in.close();
                if (out!=null) out.close();
                if (echoSocket!=null) echoSocket.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
