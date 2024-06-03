package com.pc.informamee.forecast;

import com.pc.informamee.common.Credentials;

public class Container {
    private static Container Instance=null;
    private Credentials CurrentCredentials;
    private GUILogin dialog;

    public static Container getInstance() {
        if (Instance==null)
            Instance=new Container();
        return Instance;
    }

    public Credentials getCredentials() {
        return CurrentCredentials;
    }

    private Container() {
        dialog=new GUILogin();
    }

    protected void SetCredentials (Credentials ToSet) {
        CurrentCredentials=ToSet;
    }

    public static void main(String[] args) {
        getInstance().dialog.pack();
        getInstance().dialog.setVisible(true);
    }
}
