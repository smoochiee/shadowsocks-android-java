package com.licomm.papercraft.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.licomm.papercraft.R;
import com.licomm.papercraft.core.AppInfo;
import com.licomm.papercraft.core.AppProxyManager;
import com.licomm.papercraft.core.LocalVpnService;
import com.licomm.papercraft.core.ProxyConfig;

import java.util.Calendar;

public class ConfigActivity extends Activity implements
        View.OnClickListener,
        OnCheckedChangeListener,
        LocalVpnService.onStatusChangedListener {


    private static String GL_HISTORY_LOGS;

    private static final String TAG = ConfigActivity.class.getSimpleName();

    private static final String CONFIG_URL_KEY = "CONFIG_URL_KEY";

    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;

    private Switch switchProxy;
    private TextView textViewLog;
    private ScrollView scrollViewLog;
    private TextView textViewProxyApp;
    private Calendar mCalendar;
    private Spinner mSpinner;

    private SharedPreferences spf;
    private static final String PREF_NAME = "config";
    private static final String SERVER_NAME = "server_name";
    private static final String REMOTE_PORT = "remote_port";
    private static final String PASSWORD = "password";
    private static final String ENCRYPT_METHOD = "encrypt_method";
    private EditText mEditServer = null;
    private EditText mEditPort = null;
    private EditText mEditPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        scrollViewLog = findViewById(R.id.scrollViewLog);
        textViewLog = findViewById(R.id.textViewLog);
        //findViewById(R.id.ProxyUrlLayout).setOnClickListener(this);
        findViewById(R.id.AppSelectLayout).setOnClickListener(this);
        mSpinner = findViewById(R.id.spinner1);
        mEditServer = findViewById(R.id.editText1);
        mEditPort = findViewById(R.id.editText2);
        mEditPassword = findViewById(R.id.editText3);
        //textViewProxyUrl = findViewById(R.id.textViewProxyUrl);
        // String ProxyUrl = readProxyUrl();
        // if (TextUtils.isEmpty(ProxyUrl)) {
        //  textViewProxyUrl.setText(R.string.config_not_set_value);
        //} else {
        // textViewProxyUrl.setText(ProxyUrl);
        //   }

        textViewLog.setText(GL_HISTORY_LOGS);
        scrollViewLog.fullScroll(ScrollView.FOCUS_DOWN);

        mCalendar = Calendar.getInstance();
        LocalVpnService.addOnStatusChangedListener(this);

        final Button button = findViewById(R.id.reset);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mEditPort.setText("");
                mEditPassword.setText("");
                mEditServer.setText("");
            }
        });


        //Pre-App Proxy
        if (AppProxyManager.isLollipopOrAbove) {
            new AppProxyManager(this);
            textViewProxyApp = findViewById(R.id.textViewAppSelectDetail);
        } else {
            ((ViewGroup) findViewById(R.id.AppSelectLayout).getParent()).removeView(findViewById(R.id.AppSelectLayout));
            // findViewById(R.id.textViewAppSelectLine).setVisibility(View.GONE);
        }

        if (LocalVpnService.IsRunning) {
            disableEditText();
        }
        else {
            enableEditText();
        }
    }

    void initConfig() {
        spf = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String name = spf.getString(SERVER_NAME, "");
        String port = spf.getString(REMOTE_PORT, "");
        String password = spf.getString(PASSWORD, "");
        String[] methods = getResources().getStringArray(R.array.encrypt);
        int methodId = spf.getInt(ENCRYPT_METHOD, 0);

        if (!name.isEmpty()) {
            mEditServer.setText(name);
        }
        if (!port.isEmpty()) {
            mEditPort.setText(port);
        }
        if (!password.isEmpty()) {
            mEditPassword.setText(password);
        }
        if (methodId > -1) {
            mSpinner.setSelection(methodId);

        }
    }

    String readProxyUrl() {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEY, "");
    }

    void setProxyUrl(String ProxyUrl) {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(CONFIG_URL_KEY, ProxyUrl);
        editor.apply();
    }

    String getVersionName() {
        PackageManager packageManager = getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "null package manager is impossible");
            return null;
        }

        try {
            return packageManager.getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "package not found is impossible", e);
            return null;
        }
    }

    boolean isValidUrl(String url) {
        try {
            if (url == null || url.isEmpty())
                return false;

            if (url.startsWith("ss://")) {//file path
                return true;
            } else { //url
                Uri uri = Uri.parse(url);
                if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme()))
                    return false;
                if (uri.getHost() == null)
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    public void onClick(View v) {
        if (switchProxy.isChecked()) {
            return;
        }

        if (v.getTag().toString().equals("AppSelect")){
            System.out.println("abc");
            startActivity(new Intent(this, AppManager.class));
        }
    }

//    private void showProxyUrlInputDialog() {
//        final EditText editText = new EditText(this);
//        editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
//        editText.setHint(getString(R.string.config_url_hint));
//        editText.setText(readProxyUrl());
//
//        new AlertDialog.Builder(this)
//                .setTitle(R.string.config_url)
//                .setView(editText)
//                .setPositiveButton(R.string.btn_ok, new OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (editText.getText() == null) {
//                            return;
//                        }
//
//                        String ProxyUrl = editText.getText().toString().trim();
//                        if (isValidUrl(ProxyUrl)) {
//                            setProxyUrl(ProxyUrl);
//                            //textViewProxyUrl.setText(ProxyUrl);
//                        } else {
//                            Toast.makeText(ConfigActivity.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                })
//                .setNegativeButton(R.string.btn_cancel, null)
//                .show();
//    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLogReceived(String logString) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        logString = String.format("[%1$02d:%2$02d:%3$02d] %4$s\n",
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND),
                logString);

        System.out.println(logString);

        if (textViewLog.getLineCount() > 200) {
            textViewLog.setText("");
        }
        textViewLog.append(logString);
        scrollViewLog.fullScroll(ScrollView.FOCUS_DOWN);
        GL_HISTORY_LOGS = textViewLog.getText() == null ? "" : textViewLog.getText().toString();
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        switchProxy.setEnabled(true);
        switchProxy.setChecked(isRunning);
        onLogReceived(status);
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (LocalVpnService.IsRunning != isChecked) {
            switchProxy.setEnabled(false);
            if (isChecked) {
                Intent intent = LocalVpnService.prepare(this);
                if (intent == null) {
                    startVPNService();
                } else {
                    startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                }
            } else {
                LocalVpnService.IsRunning = false;
                enableEditText();
                closeNotification();
            }
        }
    }

    private void startVPNService() {
        //String ProxyUrl = readProxyUrl();
        String ProxyUrl = "ss://";
        String[] methods = getResources().getStringArray(R.array.encrypt);
        ProxyUrl += methods[mSpinner.getSelectedItemPosition()];
        ProxyUrl += ":" + mEditPassword.getText().toString();
        ProxyUrl += "@" + mEditServer.getText().toString() + ":" + mEditPort.getText().toString();
        if (!isValidUrl(ProxyUrl)) {
            Toast.makeText(this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
            switchProxy.post(new Runnable() {
                @Override
                public void run() {
                    switchProxy.setChecked(false);
                    switchProxy.setEnabled(true);
                }
            });
            return;
        }
        spf.edit().putString(SERVER_NAME, mEditServer.getText().toString()).apply();
        spf.edit().putString(REMOTE_PORT, mEditPort.getText().toString()).apply();
        spf.edit().putString(PASSWORD, mEditPassword.getText().toString()).apply();
        spf.edit().putInt(ENCRYPT_METHOD, mSpinner.getSelectedItemPosition()).apply();
        textViewLog.setText("");
        GL_HISTORY_LOGS = null;
        onLogReceived("WHEN BAD MEETS EVIL>>>>>.....");
        LocalVpnService.ProxyUrl = ProxyUrl;

        showNotification();

        disableEditText();

        startService(new Intent(this, LocalVpnService.class));
    }

    private void showNotification() {
        //TODO: Add persist notification
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_running))
                .setSubText(getString(R.string.notification_click_enter))
                .setSmallIcon(R.drawable.vpn_lock)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_DEFAULT);
        Notification notification = builder.build();
        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, ConfigActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        notificationManger.notify(01, notification);
    }

    private void closeNotification() {
        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.cancel(01);
    }

    private void disableEditText() {
        findViewById(R.id.editText1).setEnabled(false);
        findViewById(R.id.editText2).setEnabled(false);
        findViewById(R.id.editText3).setEnabled(false);
        findViewById(R.id.editText1).setFocusable(false);
        findViewById(R.id.editText2).setFocusable(false);
        findViewById(R.id.editText3).setFocusable(false);
        findViewById(R.id.editText1).setFocusableInTouchMode(false);
        findViewById(R.id.editText2).setFocusableInTouchMode(false);
        findViewById(R.id.editText3).setFocusableInTouchMode(false);
        findViewById(R.id.spinner1).setEnabled(false);
    }

    private void enableEditText() {
        findViewById(R.id.editText1).setEnabled(true);
        findViewById(R.id.editText2).setEnabled(true);
        findViewById(R.id.editText3).setEnabled(true);
        findViewById(R.id.editText1).setFocusable(true);
        findViewById(R.id.editText2).setFocusable(true);
        findViewById(R.id.editText3).setFocusable(true);
        findViewById(R.id.editText1).setFocusableInTouchMode(true);
        findViewById(R.id.editText2).setFocusableInTouchMode(true);
        findViewById(R.id.editText3).setFocusableInTouchMode(true);
        findViewById(R.id.spinner1).setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVPNService();
            } else {
                switchProxy.setChecked(false);
                switchProxy.setEnabled(true);
                onLogReceived("canceled.");
            }
            return;
        }

//        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//        if (scanResult != null) {
//            String ProxyUrl = scanResult.getContents();
//            if (isValidUrl(ProxyUrl)) {
//                setProxyUrl(ProxyUrl);
//                textViewProxyUrl.setText(ProxyUrl);
//            } else {
//                Toast.makeText(ConfigActivity.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_switch);
        if (menuItem == null) {
            return false;
        }

        switchProxy = (Switch) menuItem.getActionView();
        if (switchProxy == null) {
            return false;
        }

        switchProxy.setChecked(LocalVpnService.IsRunning);
        switchProxy.setOnCheckedChangeListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_about:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.app_name) + getVersionName())
                        .setMessage(R.string.about_info)
                        .setPositiveButton(R.string.btn_ok, null)
                        .setNegativeButton(R.string.btn_more, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/smoochiee")));
                            }
                        })
                        .show();

                return true;
            case R.id.menu_item_exit:
                if (!LocalVpnService.IsRunning) {
                    finish();
                    return true;
                }

                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_item_exit)
                        .setMessage(R.string.exit_confirm_info)
                        .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalVpnService.IsRunning = false;
                                LocalVpnService.Instance.disconnectVPN();
                                stopService(new Intent(ConfigActivity.this, LocalVpnService.class));
                                System.runFinalization();
                                System.exit(0);
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();

                return true;
            case R.id.menu_item_toggle_global:
                ProxyConfig.Instance.globalMode = !ProxyConfig.Instance.globalMode;
                if (ProxyConfig.Instance.globalMode) {
                    onLogReceived("Proxy global mode is on");
                } else {
                    onLogReceived("Proxy global mode is off");
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initConfig();
        if (AppProxyManager.isLollipopOrAbove) {
            if (AppProxyManager.Instance.proxyAppInfo.size() != 0) {
                String tmpString = "";
                for (AppInfo app : AppProxyManager.Instance.proxyAppInfo) {
                    tmpString += app.getAppLabel() + ", ";
                }
                textViewProxyApp.setText(tmpString);
            }
        }
    }

    @Override
    protected void onDestroy() {
        LocalVpnService.removeOnStatusChangedListener(this);
        super.onDestroy();
    }

}
