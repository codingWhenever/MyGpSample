package dbh.leo.com.mygpsample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jb.sdk.aidl.JBPrinterConnectCallback;
import com.jb.sdk.aidl.JBPrinterRealStatusCallback;
import com.jb.sdk.aidl.JBService;
import com.jb.sdk.io.GpDevice;
import com.jb.sdk.io.GpPort;
import com.jb.sdk.io.PortParameter;
import com.jb.sdk.service.JbPrintService;

public class MainActivity extends AppCompatActivity {

    /**
     * 显示当前的操作状态等
     */
    private TextView tvCurrent;
    private int mPrinterId = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrent = (TextView) findViewById(R.id.tvCurrent);

        /* 绑定服务，绑定成功后调用ServiceConnection中的onServiceConnected方法 */
        Intent intent = new Intent(this, JbPrintService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvCurrent.setText(getStatus(0));
    }

    private String getStatus(int id) {
        if (mService == null) {
            return "未连接";
        }
        int state = GpPort.STATE_NONE;
        try {
            state = mService.getPrinterConnectStatus(mPrinterId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String str;
        if (state == GpPort.STATE_CONNECTED) {
            str = "已连接";
        } else if (state == GpPort.STATE_CONNECTING) {
            str = "链接中";
        } else {
            str = "未连接";
        }
        return str;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            try {
                mService.closePort(mPrinterId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mServiceConnection);
    }

    /**
     * 打开连接
     *
     * @param view
     */
    public void openConnect(View view) {
        //id为打印服务操作的打印机的id，最大可以操作3台
        try {
            mService.openPort(mPrinterId, PortParameter.ETHERNET, "192.168.1.108", 9100);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 打印测试页
     *
     * @param view
     */
    public void printTestPaper(View view) {
        try {
            mService.printeTestPage(mPrinterId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     *
     * @param view
     */
    public void closeConnect(View view) {
        try {
            mService.closePort(mPrinterId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印toast信息
     *
     * @param m
     */
    private void msg(String m) {
        Toast.makeText(MainActivity.this, m, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取打印机状态
     *
     * @param view
     */
    public void getPrinterStatus(View view) {
        try {
            int connectStatus = mService.getPrinterConnectStatus(mPrinterId);

            if (connectStatus == GpPort.STATE_CONNECTED) {
                Log.d(TAG, "已连接");
                msg("已连接");
            } else if (connectStatus == GpPort.STATE_CONNECTING) {
                Log.d(TAG, "连接中");
                msg("连接中");
            } else if (connectStatus == GpPort.STATE_NONE) {
                Log.d(TAG, "未连接");
                msg("未连接");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取命令类型
     *
     * @param view
     */
    public void getCommandTypes(View view) {
        try {
            final int type = mService.getPrinterCommandType(mPrinterId);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvCurrent.setText(type == GpDevice.UNKNOWN_COMMAND ? "未知类型" :
                            (type == GpDevice.RECEIPT_COMMAND ? "票据模式" :
                                    "标签模式"));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    private static final String TAG = "ServiceConnection";
    private JBService mService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "========onServiceConnected=======");
            mService = JBService.Stub.asInterface(service);
            try {
                mService.registerConnectCallback(new ConnectCallback());
                mService.registerPrinterStatusCallback(new QueryPrinterRealStatus());
                Log.d(TAG, "绑定服务成功了");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "========onServiceDisconnected=======");
            mService = null;
        }
    };

    /**
     * 打印机连接回调
     */
    public class ConnectCallback extends JBPrinterConnectCallback.Stub {

        @Override
        public void onConnecting(final int mId) throws RemoteException {

            Log.d(TAG, "--------onConnecting----------");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvCurrent.setText(getStatus(mId));
                }
            });
        }

        @Override
        public void onDisconnect(final int mId) throws RemoteException {
            Log.d(TAG, "--------onDisconnect----------");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvCurrent.setText(getStatus(mId));
                }
            });
        }

        @Override
        public void onConnected(final int mId) throws RemoteException {
            Log.d(TAG, "--------onConnected----------");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvCurrent.setText(getStatus(mId));
                }
            });
        }

//        @Override
//        public IBinder asBinder() {
//            return null;
//        }
    }


    private static final int REQUEST_TOAST_PRINTER_STATUS = 1;

    /**
     * 打印机实时状态查询回调
     */
    public class QueryPrinterRealStatus extends JBPrinterRealStatusCallback.Stub {

        @Override
        public void onPrinterRealStatus(int mId, int status, int requestCode) throws RemoteException {
            switch (requestCode) {
                case REQUEST_TOAST_PRINTER_STATUS:
                    showPrinterStatus(mId, status);
                    break;
            }
        }

//        @Override
//        public IBinder asBinder() {
//            return null;
//        }
    }

    /**
     * 显示打印机的实时状态
     *
     * @param id
     * @param status
     */
    private void showPrinterStatus(final int id, int status) {
        String str;
        if (status == GpDevice.STATE_NO_ERR) {
            str = "打印机正常";
        } else {
            str = "打印机 ";
            if ((byte) (status & GpDevice.STATE_OFFLINE) > 0) {
                str += "脱机";
            }
            if ((byte) (status & GpDevice.STATE_PAPER_ERR) > 0) {
                str += "缺纸";
            }
            if ((byte) (status & GpDevice.STATE_COVER_OPEN) > 0) {
                str += "打印机开盖";
            }
            if ((byte) (status & GpDevice.STATE_ERR_OCCURS) > 0) {
                str += "打印机出错";
            }
            if ((byte) (status & GpDevice.STATE_TIMES_OUT) > 0) {
                str += "查询超时";
            }
        }

        final String statusStr = str;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "打印机：" + id + " 状态：" + statusStr, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
