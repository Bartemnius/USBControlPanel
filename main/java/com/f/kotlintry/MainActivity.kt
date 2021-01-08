package com.f.kotlintry

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

class MainActivity : AppCompatActivity() {

    lateinit var m_usbManager: UsbManager;
    var m_device: UsbDevice? = null;
    var m_serialDevice: UsbSerialDevice? = null;
    var m_connection: UsbDeviceConnection? = null;
    val ACTION_USB_PERMISSION = "permission";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        m_usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        filter.addAction((UsbManager.ACTION_USB_DEVICE_ATTACHED))
        registerReceiver(broadcastReceiver, filter)
        val on = findViewById<Button>(R.id.on)
        val off = findViewById<Button>(R.id.off)
        val connect = findViewById<Button>(R.id.connect)
        val disconnect = findViewById<Button>(R.id.disconnect)
        on.setOnClickListener { sendData("7") }
        off.setOnClickListener { sendData("8") }
        connect.setOnClickListener { startUsbConnecting() }
        disconnect.setOnClickListener { disconnecting() }
    }

    private fun startUsbConnecting() {
        val usbDevices: HashMap<String, UsbDevice>? = m_usbManager.deviceList;
        if (!usbDevices?.isEmpty()!!) {
            var keep = true
            usbDevices.forEach { entry ->
                m_device = entry.value
                val deviceVendorId: Int? = m_device?.vendorId
                Log.i("Serial", "vendorId:" + deviceVendorId)
                //ja mam rózne od null ale  usb-device vendor-id="0483" product-id="0374"
                if (deviceVendorId != null) {
                    val intent: PendingIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
                    m_usbManager.requestPermission(m_device, intent)
                    keep = false
                    Log.i("Serial", "connection successful")
                } else {
                    m_connection = null
                    m_device = null
                    Log.i("Serial", "unable to connect")
                }
                if (!keep) {
                    return
                }
            }
        } else Log.i("Serial,", "no usb device connection")
    }

    private fun sendData(input: String) {
        m_serialDevice?.write(input.toByteArray());
        Log.i("Serial", "sending data: " + input.toByteArray());
    }

    private fun disconnecting() {
        m_serialDevice?.close();
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action!! == ACTION_USB_PERMISSION) {
                val granted: Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    m_connection = m_usbManager.openDevice((m_device));
                    m_serialDevice = UsbSerialDevice.createUsbSerialDevice(m_device, m_connection);
                    if (m_serialDevice != null) {
                        if (m_serialDevice!!.open()) {
                            m_serialDevice!!.setBaudRate(9600);
                            m_serialDevice!!.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            m_serialDevice!!.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            m_serialDevice!!.setParity(UsbSerialInterface.PARITY_NONE);
                            m_serialDevice!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                        } else {
                            Log.i("Serial", "port not open");
                        }

                    } else {
                        Log.i("Serial", "port is null");
                    }

                } else {
                    Log.i("Serial", "permission not granted"); }
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                startUsbConnecting();
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                disconnecting();
            }

        }
    }
}

//The !! operator tells the compiler that even though the type is nullable, the programmer is sure that the value will not be null in this particular place
//Znaku zapytania używa się do typu, który normalnie nie może być nullem, aby nadać mu taką możliwość. To oznacza, że właściwości A możesz przypisać nulla i kompilator nie będzie się burzył.