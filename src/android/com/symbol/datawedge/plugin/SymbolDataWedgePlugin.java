package com.symbol.datawedge.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

import com.symbol.datawedge.BarcodeScan;
import com.symbol.datawedge.DataWedgeIntentHandler;
import com.symbol.datawedge.ScanCallback;

public class SymbolDataWedgePlugin extends CordovaPlugin {
    
    private DataWedgeIntentHandler wedge;
    protected static String TAG = "SymbolDatawedgePlugin";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView)
    {
        super.initialize(cordova, webView);
        wedge = new DataWedgeIntentHandler(cordova.getActivity().getBaseContext());
    }
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if ("scanner.register".equals(action)) {
            wedge.setScanCallback(new ScanCallback<BarcodeScan>() {
                @Override
                public void execute(BarcodeScan scan) {
                    Log.i(TAG, "Scan result [" + scan.LabelType + "-" + scan.Barcode + "].");
                    
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("type", scan.LabelType);
                        obj.put("barcode", scan.Barcode);
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);
                    } catch(JSONException e){
                        Log.e(TAG, "Error building json object", e);
                        
                    }
                }
            });
        }
        else if ("scanner.unregister".equals(action)) {
            wedge.setScanCallback(null);
            if (!wedge.hasListeners()) {
                wedge.stop();
            }
        }
        else if ("scanner.softScanOn".equals(action)){
            wedge.startScanning(true);
            callbackContext.success();
        }
        else if ("scanner.softScanOff".equals(action)) {
            wedge.startScanning(false);
            callbackContext.success();
        }

        //register for plugin callbacks
        else if ("switchProfile".equals(action)){
            wedge.switchProfile(args.getString(0));
        }

        else if ("stop".equals(action)){
            wedge.stop();
        }


        //start plugin now if not already started
        if ("start".equals(action) || "scanner.register".equals(action)) {

            //try to read intent action from inbound params
            String intentAction = null;
            if (args.length() > 0) {
                intentAction = args.getString(0);  
            } 
            if (intentAction != null && intentAction.length() > 0) {
                Log.i(TAG, "Intent action length  " + intentAction.length());

                wedge.setDataWedgeIntentAction(intentAction);
            }
            wedge.start();
        } 

        return true;
    }
    /**
    * Always close the current intent reader
    */
    @Override
    public void onPause(boolean multitasking)
    {
        super.onPause(multitasking);
        wedge.stop();
    }

    @Override
    public void onNewIntent(Intent intent) {
        
        Log.i(TAG, "Got inbound intent  " + intent.getAction());
        wedge.handleIntent(intent);
    }

    /**
    * Always resume the current activity
    */
    @Override
    public void onResume(boolean multitasking)
    {
        super.onResume(multitasking);
        wedge.start();
    }
}
