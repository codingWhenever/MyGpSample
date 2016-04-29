# MyGpSample
Android平台连接佳搏5890XIII打印机通讯实例

1.导入jiar包--->jbsdk.jar

2.在manifest注册服务JbPrintService，同时添加网络和wifi等相关权限；

3.然后在MainActivity中绑定服务,并且可以把连接状态改变的回调以及查询打印机状态的回调一起注册了

4.使用服务

使用之前先说说这个服务提供的接口

打印机连接状态改变的接口

interface JBPrinterConnectCallback {

    void onConnecting(int mId);

    void onDisconnect(int mId);

    void onConnected(int mId);
}

获取实时状态

interface JBPrinterRealStatusCallback {
    void onPrinterRealStatus(int mId ,int status,int requestCode);
}

打印服务提供的相关打印操作

interface JBService {
    void openPort(int id, int portType, String portName, int portNumber);
    
    void closePort(int id);
    
    void printeTestPage(int id);
    
    void queryPrinterStatus(int id,long timeout, int requestCode);
    
    void sendReceiptCommand(int id,in byte[] receiptByte);
    
    void sendLabelCommand(int id,in byte[] labelByte);
    
    int getPrinterConnectStatus(int id);
    
    int getPrinterCommandType(int id);
    
    void registerConnectCallback(JBPrinterConnectCallback callback);
    
    void registerPrinterStatusCallback(JBPrinterRealStatusCallback callback);
    
}


具体方法使用可参照MainActivity中的代码实现；
