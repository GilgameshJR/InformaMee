package com.pc.informamee.mobile;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

public class AlertDialogOKBuilder {
    public static void ShowAlert(Context Cont, String ErrorString)
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(Cont);
        builder1.setMessage(ErrorString);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
