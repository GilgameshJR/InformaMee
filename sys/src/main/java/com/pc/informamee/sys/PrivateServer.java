package com.pc.informamee.sys;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;

import com.pc.informamee.common.events.*;
import com.pc.informamee.common.Credentials;
import com.pc.informamee.common.requests.RequestNotDoneException;

public class PrivateServer extends Thread {
    public static final int PORT_NUMBER = 8081;

    protected Socket socket;

    private PrivateServer(Socket socket) {
        try {
            socket.setKeepAlive(true);
        } catch (SocketException e) {
            System.out.println("Can't set KEEP ALIVE");
            e.printStackTrace();
        }
        this.socket = socket;
        System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
        start();
    }

    public void run() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        Connection DbConn=null;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

            Credentials Creds=(Credentials)in.readObject();
            //"Credentials received: username "+Creds.getID()+" password: "+ Creds.getPwd())
            DbConn = DatabaseConnection.Connect();
            if (Creds.areValid(DbConn))
            {
                    out.writeObject(Boolean.TRUE);
                    Boolean r=(Boolean)in.readObject();
                    if (r)
                    {
                        //"receiving obj"
                        Object o2 = in.readObject();
                        EventInterface e = (EventInterface) o2;
                        if (e.ExecuteQuery(DbConn, Creds.getID())) {
                            out.writeObject(Boolean.TRUE);
                        } else {
                            out.writeObject(Boolean.FALSE);
                            out.writeObject(e);
                        }
                    }
                    //else auth only
            }
            else {
                //Wrong credentials
                out.writeObject(Boolean.FALSE);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Unable to get streams from client (or client sent something else), or connection problems to DB");
            e.printStackTrace();
        }
        catch (ClassCastException e)
        {
            System.out.println("Handshake not respected, received something else");
            e.printStackTrace();
        }
         catch (SQLException e)
        {
            System.out.println("Can't connect to DB or query error (but shouldn't happen)");
            e.printStackTrace();
        } catch (RequestNotDoneException e) {
            System.out.println("DB or query error (but shouldn't happen)");
            e.printStackTrace();
        } finally {
            try {
                if (in!=null) in.close();
                if (out!=null) out.close();
                if (socket!=null) socket.close();
                if (DbConn!=null) DbConn.close();
            } catch (IOException | SQLException ex) {
                //ignore
            }
        }
    }

    public static void main(String[] args)  {
        System.out.println("Private Server started. Waiting for forecasts...");
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT_NUMBER);
           while (true){
                new PrivateServer(server.accept());
            }
        } catch (IOException ex) {
            System.out.println("Unable to start private server. Maybe the port is busy.");
        } finally {
            try {
                if (server != null)
                    server.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}