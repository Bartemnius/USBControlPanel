package com.f.kotlintry

//Wojtek Dłuski
//The application was created to control THOR robotic arm (open source project) via stm32

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface



class MainActivity : AppCompatActivity(), JoystickView.JoystickListener  {

    lateinit var m_usbManager: UsbManager;
    var m_device: UsbDevice? = null;
    var m_serialDevice: UsbSerialDevice? = null;
    var m_connection: UsbDeviceConnection? = null;
    val ACTION_USB_PERMISSION = "permission";





    //on Create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //resources for USB
        m_usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        filter.addAction((UsbManager.ACTION_USB_DEVICE_ATTACHED))
        registerReceiver(broadcastReceiver, filter)
        startUsbConnecting();

        //buttons
//        val on = findViewById<Button>(R.id.on)
//        val off = findViewById<Button>(R.id.off)
//        val connect = findViewById<Button>(R.id.connect)
//        val disconnect = findViewById<Button>(R.id.disconnect)
        //actions for buttons
//        connect.setOnClickListener { startUsbConnecting() ;Log.d("msg", "d")}
//        disconnect.setOnClickListener { disconnecting() }
        //first joystick
        //testing the diode
//        on.setOnClickListener { sendData("7") }
//        off.setOnClickListener { sendData("8") }

        val joystick1 = findViewById<JoystickView>(R.id.joystick1)
        val stop =findViewById<Button>(R.id.STOP)
        val enable =findViewById<Button>(R.id.Enable)
        val homePosition =findViewById<Button>(R.id.HomePosition)
        val firstAxis = findViewById<Button>(R.id.FirstAxis)
        val secondAxis = findViewById<Button>(R.id.SecondAxis)
        val thirdAxis = findViewById<Button>(R.id.ThirdAxis)
        val fourthAxis = findViewById<Button>(R.id.FourthAxis)
        val fifthAxis = findViewById<Button>(R.id.FifthAxis)
        val grapple = findViewById<Button>(R.id.Grapple)
        val inverseKinematics = findViewById<Button>(R.id.InverseKinematics)


        stop.setOnClickListener{sendData("0")}
        enable.setOnClickListener{sendData("9")}
        homePosition.setOnClickListener{sendData("h")}
        inverseKinematics.setOnClickListener{sendData("k")}
        firstAxis.setOnClickListener{sendData("o");sendData("1")}
        secondAxis.setOnClickListener{sendData("o");sendData("2")}
        thirdAxis.setOnClickListener{sendData("o");sendData("3")}
        fourthAxis.setOnClickListener{sendData("o");sendData("4")}
        fifthAxis.setOnClickListener{sendData("o");sendData("5")}
        grapple.setOnClickListener{sendData("o");sendData("g")}
        joystick1.setOnClickListener{};

    }




    //function to connect to device (USB CONNECT BUTTON)
    private fun startUsbConnecting() {
        val usbDevices: HashMap<String, UsbDevice>? = m_usbManager.deviceList;
        if (!usbDevices?.isEmpty()!!) {
            var keep = true
            usbDevices.forEach { entry ->
                m_device = entry.value
                val deviceVendorId: Int? = m_device?.vendorId
                Log.i("Serial", "vendorId:" + deviceVendorId)
                //checking if any device is detected, we can set user-chosen id  (for stm32 vendor-id="0483" product-id="0374")
                if (deviceVendorId != null) {
                    val intent: PendingIntent = PendingIntent.getBroadcast(
                            this, 0, Intent(
                            ACTION_USB_PERMISSION
                    ), 0
                    )
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

    // function for sending data. String into byte array
    fun sendData(input: String) {
        m_serialDevice?.write(input.toByteArray());
        Log.i("Serial", "sending data: " + input.toByteArray());
    }

    // function for closing connection
    private fun disconnecting() {
        m_serialDevice?.close();
    }

    //setting desired parameters for our connection
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

    //NOT USED, DIFFERENT IDEA
    //this is the interface that i would use if I wanted to get different speed levels for my engines
     override fun onJoystickMoved(xPercent : Float, yPercent : Float, source: Int) {
         val joystick1 = findViewById<JoystickView>(R.id.joystick1)
         val joystick2 =  findViewById<JoystickView>(R.id.joystick2)

         if(joystick1.isActivated){
                Log.d("Right Joystick", "X: " + xPercent + " Y: " + yPercent)
           }

         if(joystick2.isActivated) {
                Log.d("Left Joystick", "X: " + xPercent + " Y: " + yPercent);
            }
        }

    override fun sendingData(x : Float, y : Float){


            //Calculating the position where is joystick and sending command which direction is correct

        Log.d("x",""+x);
        Log.d("y",""+y);

            //first quarter UP
            if (y > -5.67 * x && y > 5.67 * x) {
                sendData("U")
                Log.d("send", "U");
            }

            else if (y < 5.67 * x && y > 0.18 * x) {
                sendData("U")
                sendData("R")
            }

            else if (y < 0.18 * x && y > -0.18 * x) {
                sendData("R")
            }

            else if (y < -0.18 * x && y > -5.67 * x) {
                sendData("D")
                sendData("R")
            }

            else if (y < 5.67 * x && y < -5.67 * x) {
                sendData("D")
            }

            else if (y > 5.67 * x && y < 0.18 * x) {
                sendData("D")
                sendData("L")
            }

            else if (y >= 0.18 * x && y < -0.18 * x) {
                sendData("L")
            }

            else if (y > -0.18 * x && y < -5.67 * x) {
                sendData("U")
                sendData("L")
            }

            else {
                sendData("0")
            }
    }




 }


//The '!!' operator tells the compiler that even though the type is nullable, the programmer is sure that the value will not be null in this particular place
//Very similar to '?' operator