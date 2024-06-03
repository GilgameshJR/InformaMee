package com.pc.informamee.mobile;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.pc.informamee.R;
import com.pc.informamee.common.requests.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

public class RequestUploader {
    private Request CurrRequest;
    public Handler CurrHandler;
    private Thread T;

    public static final int UPLOAD_OK=0;
    public static final int SERVERSQLEX=1;
    public static final int SERVERCLASSEX =2;
    public static final int CLIENTIOEX =-1;
    public static final int CLIENTCLASSEX =-2;
    public static final int GENERICERROR =-3;
   // public static final int REQUESTNOTDONE=-4;

    public static final String CLIENTCLASSEXSTRING="I didn't received object I was expecting from server (ClassNotFound). Please try again.";
    public static final String CLIENTIOEXSTRING="Impossibile connettersi al server. Controlla la tua connessione";
    public static final String SERVERSQLEXSTRING="Server couldn't connect to database. It's our fault! Please try later.";
    public static final String SERVERCLASSEXSTRING="Server didn't receive object it was expecting (ClassNotFound or ClassCastException). Please try again.";
    public static final String REQUESTNOTDONESTRING="Il sistema non ha eseguito la richiesta. Riprova più tardi";
    public static final String GENERICERRORSTRING="Qualcosa è andato storto";

    public static void ShowErrorMessage(Context CurrentContext, int State)
    {
        switch (State)
        {
            case UPLOAD_OK:
                break;
            case CLIENTCLASSEX: AlertDialogOKBuilder.ShowAlert(CurrentContext, CLIENTCLASSEXSTRING);
                break;
            case CLIENTIOEX: AlertDialogOKBuilder.ShowAlert(CurrentContext, CLIENTIOEXSTRING);
                break;
            case SERVERSQLEX: AlertDialogOKBuilder.ShowAlert(CurrentContext, SERVERSQLEXSTRING);
                break;
            case SERVERCLASSEX: AlertDialogOKBuilder.ShowAlert(CurrentContext, SERVERCLASSEXSTRING);
                break;
            default: AlertDialogOKBuilder.ShowAlert(CurrentContext, GENERICERRORSTRING);
                break;
        }
    }

    public void RequestHandleState(int State)
    {
        Message completeMessage= CurrHandler.obtainMessage(State, CurrRequest);
        completeMessage.sendToTarget();
    }
    public RequestUploader(Request toUpload, Handler handl, Context context)
    {
        this.CurrHandler=handl;
        this.CurrRequest =toUpload;
        final String MyHost=context.getString(R.string.servername);
        final int MyPort=context.getResources().getInteger(R.integer.serverport);
        T=new Thread(new Runnable() {
            @Override
            public void run() {
                /*
                Date pippo=new Date(5555);
                CurrRequest =new FindRequestDummy(pippo);
                RequestHandleState(UPLOAD_OK);*/
                System.out.println("uploading Request to host " + MyHost + " on port " + MyPort);
                Socket echoSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                int ReqState=GENERICERROR;
                try {
                    echoSocket = new Socket(MyHost, MyPort);
                    echoSocket.setKeepAlive(true);
                    out = new ObjectOutputStream(echoSocket.getOutputStream());
                    in = new ObjectInputStream(echoSocket.getInputStream());
                    out.writeObject(CurrRequest);
                    Object Res = in.readObject();
                    if (Res instanceof Request) {
                        CurrRequest = (Request) Res;
                        ReqState=UPLOAD_OK;
                    } else if (Res instanceof SQLException) {
                        ReqState=SERVERSQLEX;
                    } else if (Res instanceof ClassNotFoundException || Res instanceof ClassCastException) {
                        ReqState= SERVERCLASSEX;
                    }
                } catch (ClassNotFoundException | ClassCastException e) {
                    ReqState= CLIENTCLASSEX;
                }
                catch (IOException ex) {
                    ReqState= CLIENTIOEX;
                }
                finally {
                    RequestHandleState(ReqState);
                    try {
                        if (in != null) in.close();
                        if (out != null) out.close();
                        if (echoSocket != null) echoSocket.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        });
    }
    public void Upload()
    {
        T.start();
    }
}