package com.example.android.bluetoothlegatt.BluetoothLe;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String JDY08 = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String JDY08_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String JDY08_CHARACTERISTIC_READ = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String JDY08_CHARACTERISTIC_WRITE = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public static String RN4871U_SERVICE = "49535343-FE7D-4AE5-8FA9-9FAFD205E455";
    public static String RN4871U_CHARACTERISTIC_READ = "49535343-1E4D-4BD9-BA61-23C647249616";
    public static String RN4871U_CHARACTERISTIC_WRITE = "49535343-1E4D-4BD9-BA61-23C647249616";
    public static String RN4871U_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
}
