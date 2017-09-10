package com.fuse.utils;

import java.util.logging.*;

import processing.core.PApplet;
import processing.core.PVector;
import com.fuse.ui.TouchEvent;
import com.fuse.ui.TouchManager;
import com.fuse.utils.tuio.*;

public class TuioInput implements TuioListener {

    private TouchManager touchManager;
    private int oscPort;
    private TuioClient tuioClient;
    private PApplet papplet;
    /// position of the window within the screen (necessary to convert tuio screen-percentage-based coordinates to window-origin-relative coordinates)
    private PVector windowPos;
    /// size of the screen in pixels (necessary to convert tuio screen-percentage-based coordinates to window-origin-relative coordinates)
    private PVector screenSize;

    private Logger logger;

    public TuioInput(){
      logger = Logger.getLogger(TuioInput.class.getName());
      screenSize = new PVector();
      windowPos = new PVector();
      papplet = null;
    }

    public void setup(TouchManager touchManager, int port){
        this.touchManager = touchManager;
        if(this.touchManager==null)
        	logger.warning("TuioInput got null TouchManager; not properly setup");

        oscPort = port;
        setupTuio(oscPort);
    }

    public void setWindowPos(PVector newPos){
    	windowPos = newPos;
    }

    public void setScreenSize(PVector newSize){
    	screenSize = newSize.get(); // newer processing API: .copy();
    }

    public void setPapplet(PApplet papplet){
      this.papplet = papplet;
      setWindowPos(new PVector(this.papplet.frame.getX(), this.papplet.frame.getY(), 0f));
      setScreenSize(new PVector(this.papplet.displayWidth, this.papplet.displayHeight, 0f));
      logger.info("TuioInput uses window pos: "+windowPos.toString()+", and screen size: "+screenSize.toString());
      logger.warning("TODO: TuioInput should register resize listener on papplet?");
    }

    public void addedCursor(TuioCursor cursor){
      setWindowPos(new PVector(this.papplet.frame.getX(), this.papplet.frame.getY(), 0f));
      setScreenSize(new PVector(this.papplet.displayWidth, this.papplet.displayHeight, 0f));

      logger.finest("addedCursor: " + Float.toString(cursor.positionX())+", "+Float.toString(cursor.positionY()) );
      // this.touchManager.touchDown(cursor.cursorID(), convertVector(new PVector(cursor.positionX(), cursor.positionY(), 0f)));

      TouchEvent e = new TouchEvent();
      e.touchId = cursor.cursorID();
      e.eventType = TouchEvent.EventType.TOUCH_DOWN;
      e.position = convertVector(new PVector(cursor.positionX(), cursor.positionY(), 0f));
      e.velocity = convertVector(new PVector(cursor.velocityX(), cursor.velocityY(), 0f));
      e.velocitySmoothed = e.velocity;
      touchManager.submitTouchEvent(e);
    }


    public void updatedCursor(TuioCursor cursor){
      setWindowPos(new PVector(this.papplet.frame.getX(), this.papplet.frame.getY(), 0f));
      setScreenSize(new PVector(this.papplet.displayWidth, this.papplet.displayHeight, 0f));

      logger.finest("updatedCursor: " + Float.toString(cursor.positionX())+", "+Float.toString(cursor.positionY()) );
      // this.touchManager.touchMove(cursor.cursorID(), convertVector(new PVector(cursor.positionX(), cursor.positionY(), 0f)));
      TouchEvent e = new TouchEvent();
      e.touchId = cursor.cursorID();
      e.eventType = TouchEvent.EventType.TOUCH_MOVE;
      e.position = convertVector(new PVector(cursor.positionX(), cursor.positionY(), 0f));
      e.velocity = convertVector(new PVector(cursor.velocityX(), cursor.velocityY(), 0f));
      e.velocitySmoothed = e.velocity;
      touchManager.submitTouchEvent(e);
    }

    public void removedCursor(TuioCursor cursor){
      setWindowPos(new PVector(this.papplet.frame.getX(), this.papplet.frame.getY(), 0f));
      setScreenSize(new PVector(this.papplet.displayWidth, this.papplet.displayHeight, 0f));

      logger.finest("removedCursor: " + Float.toString(cursor.positionX())+", "+Float.toString(cursor.positionY()) );
      // this.touchManager.touchUp(cursor.cursorID(), convertVector(new PVector(cursor.positionX(), cursor.positionY(), 0f)));
      TouchEvent e = new TouchEvent();
      e.touchId = cursor.cursorID();
      e.eventType = TouchEvent.EventType.TOUCH_UP;
      e.position = convertVector(new PVector(cursor.positionX(), cursor.positionY(), 0f));
      e.velocity = convertVector(new PVector(cursor.velocityX(), cursor.velocityY(), 0f));
      e.velocitySmoothed = e.velocity;
      touchManager.submitTouchEvent(e);
    }

    private PVector convertVector(PVector vec){
      return new PVector(
        vec.x * screenSize.x - windowPos.x,
        vec.y * screenSize.y - windowPos.y,
        0f);
    }

    private void setupTuio(int port){
        System.out.println( "Setting up Tuio touch interface..." );
        tuioClient = new TuioClient(port);
        tuioClient.addListener(this);
        tuioClient.connect();
    }
};
