package com.f.kotlintry;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener
{
    //declaring the variables
    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    private JoystickListener joystickCallback;

    //it helps with scaling on different types of screens
    void setupDimensions(){
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) / 4;
        hatRadius = Math.min(getWidth(), getHeight()) / 6;

    }


    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    //next 3 constructors are just like the ones in super class
    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //adding this so the class now the callbacks are for this claas not the upper one. Same in every constructor
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    //drawing the joystick
    @Override
    public void surfaceCreated(SurfaceHolder holder){
        setupDimensions();
        drawJoystick(centerX,centerY);
    }


    //not using -function from upper class
    @Override
    public void surfaceChanged(SurfaceHolder holder,int format, int width, int height) {

    }
    //not using as well
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
    }

    //drawing the joystick using canvas
    private void drawJoystick(float positionX, float positionY)
    {
        Canvas myCanvas = this.getHolder().lockCanvas();
        Paint colors = new Paint();
        myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        myCanvas.drawRGB(255,255,255);
        colors.setARGB(255, 128,123,  124);
        myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
        colors.setARGB(255,0,50,50);
        myCanvas.drawCircle(positionX,positionY,hatRadius,colors);
        getHolder().unlockCanvasAndPost(myCanvas);
    }




    //onTouch function - main function which is used to declare what to do when joystick is touched
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.equals(this))
        {
            //drawing joystick in position where is pressed (event)
            if(event.getAction() != event.ACTION_UP)
            {
                //calculating the displacement between the center of the joystick and the point where u clicked
                float displacement = (float) Math.sqrt(Math.pow(event.getX() - centerX, 2) + Math.pow(event.getY() - centerY, 2));

                //its within the joystick
                float y;
                float x;
                if (displacement<baseRadius) {
                    drawJoystick(event.getX(), event.getY());
                    x =event.getX();
                    y =event.getY();

                    joystickCallback.onJoystickMoved((event.getX() - centerX) / baseRadius, (event.getY() - centerY) / baseRadius, getId());
                    joystickCallback.sendingData(x, y,centerX,centerY);
                }

                //its outside of our joystick
                else{
                    //calculating the ratio to calculate the center of the moved hat
                    float ratio = baseRadius / displacement;
                    float constrainedX = centerX + (event.getX() - centerX) * ratio;
                    float constrainedY = centerY + (event.getY() - centerY) * ratio;
                    drawJoystick(constrainedX, constrainedY);
                    x =constrainedX;
                    y =constrainedY;

                    joystickCallback.sendingData(x, y,centerX,centerY);
                    joystickCallback.onJoystickMoved((constrainedX - centerX) / baseRadius, (constrainedY - centerY) / baseRadius, getId());
                }

            }
            //if is not pressed it will go back to its' starting position
            else
            {
                drawJoystick(centerX,centerY);

                joystickCallback.onJoystickMoved(0, 0, getId());
                joystickCallback.sendingData(0,0,centerX,centerY);
            }


        }
        //returning true allowing use to multitouch it
        return true;
    }

    //creating interface so  u can use it outside. in our case in MainActivity
    interface JoystickListener
    {
        void onJoystickMoved(float xPercent, float yPercent, int source);
        void sendingData(float x,float y,float centerX, float centerY);
    }



}



