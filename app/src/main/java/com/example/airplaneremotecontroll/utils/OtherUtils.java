package com.example.airplaneremotecontroll.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.example.airplaneremotecontroll.R;
import com.example.airplaneremotecontroll.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OtherUtils {

    public static void popupMessage(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.getInstance());
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton("ok", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void popupAction(String title, String message, String actionName, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.getInstance());
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton("ok", null);
        alertDialogBuilder.setNeutralButton(actionName, listener);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static float reRangeNumber(float number) {
        return 9*number+90;
    }

    public static String getFormattedTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String date = formatter.format(new Date());

        return date;
    }
}
