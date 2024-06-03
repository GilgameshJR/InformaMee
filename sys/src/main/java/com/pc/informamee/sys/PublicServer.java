package com.pc.informamee.sys;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;

import com.pc.informamee.common.events.MalformedEventException;
import com.pc.informamee.common.requests.Request;

public class PublicServer extends Thread {
    public static final int PORT_NUMBER = 8082;

    protected Socket socket;

    private PublicServer(Socket socket) {
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
            Request ReceivedReq=(Request)in.readObject();
            System.out.println("request received");
            Connection CurrConnection = DatabaseConnection.Connect();
            ReceivedReq.ExecuteRequest(CurrConnection);
            out.writeObject(ReceivedReq);
        } catch (ClassNotFoundException | ClassCastException e) {
            System.out.println("Received unexpected request from client");
            try {
                out.writeObject(e);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.out.println("Error communicating with client");
            e.printStackTrace();
        }
         catch (SQLException e)
        {
            System.out.println("can't connect to DB");
            try {
                out.writeObject(e);
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println("Error communicating with client");
                ex.printStackTrace();
            }
        } catch (MalformedEventException e) {
            System.out.println("Evento memorizzato erroneamente all'interno del DB");
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

    public static void main(String[] args) {
        System.out.println("Public Server started. Waiting for download requests...");
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT_NUMBER);
            while (true){
                new PublicServer(server.accept());
            }
        } catch (IOException ex) {
            System.out.println("Unable to start public server. Maybe the port is busy.");
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