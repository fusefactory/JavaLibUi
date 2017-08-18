package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class PinchZoom extends ExtensionBase {

  public enum ZoomMode {
    SIZE,
    SCALE
  }

  private TouchEvent touchEvent1 = null;
  private TouchEvent touchEvent2 = null;
  /** This will hold the center of the _start_ of the two touches in global coordinate space */
  private PVector originalNodePositionGlobal = null;
  private PVector originalNodeSize = null;
  private ZoomMode zoomMode = ZoomMode.SIZE;

  public PinchZoom(){
  }

  @Override public void enable(){
    super.enable();

    node.touchDownEvent.addListener((TouchEvent event) -> {

      if(touchEvent1 == event)
        return; // already known

      if(touchEvent2 == event)
        return; // already known

      // not yet known

      if(touchEvent1 == null){
        touchEvent1 = event;
        return;
      }

      if(touchEvent2 == null){
        touchEvent2 = event;
        originalNodePositionGlobal = node.getGlobalPosition();
        originalNodeSize = node.getSize();
      }

      // already two touches; ignore any other touches

    }, this);

    getNode().touchUpEvent.addListener((TouchEvent event) -> {
      if(touchEvent1 == event){
        touchEvent1 = null; // touch#1 ended
        return;
      }

      if(touchEvent2 == event){
        touchEvent2 = null; // touch#2 ended
      }
    }, this);

    getNode().touchMoveEvent.addListener((TouchEvent event) -> {
      if(!isActive()) return;

      PVector scaler = getGlobalPinchScale();
      PVector translater = getGlobalPinchTranslate();
      // System.out.println("PinchZoom scale: "+scaler.toString());
      getNode().setGlobalPosition(translater.add(originalNodePositionGlobal));

      if(zoomMode == ZoomMode.SIZE){
        PVector newSize = new PVector(
          originalNodeSize.x * scaler.x,
          originalNodeSize.y * scaler.y,
          originalNodeSize.z * scaler.z);
        getNode().setSize(newSize);
      }

      if(zoomMode == ZoomMode.SCALE){
        getNode().setScale(scaler);
      }
    }, this);
  }

  @Override public void disable(){
    super.disable();
    getNode().touchMoveEvent.removeListeners(this);
  }

  boolean isActive(){
    return touchEvent1 != null && touchEvent2 != null;
  }

  /** @return PVector Distance between the start-points of the two touches */
  public PVector getGlobalStartDelta(){
    if(!isActive()) return new PVector();
    return touchEvent2.startPosition.copy().sub(touchEvent1.startPosition);
  }

  /** @return PVector Distance between the current positions of the two touches */
  public PVector getGlobalCurrentDelta(){
    if(!isActive()) return new PVector();
    return touchEvent2.position.copy().sub(touchEvent1.position);
  }

  public PVector getGlobalPinchScale(){
    if(!isActive()) return new PVector();
    PVector start = getGlobalStartDelta();
    PVector current = getGlobalCurrentDelta();
    return new PVector(
      Math.abs(start.x == 0.0f ? 1.0f : current.x / start.x),
      Math.abs(start.y == 0.0f ? 1.0f : current.y / start.y),
      Math.abs(start.z == 0.0f ? 1.0f : current.z / start.z));
  }

  public PVector getGlobalPinchTranslate(){
    if(!isActive()) return new PVector();


    // start center of two touches
    PVector delta = touchEvent2.startPosition.copy().sub(touchEvent1.startPosition);
    PVector startCenter = PVector.add(delta.mult(0.5f), touchEvent1.startPosition);

    PVector currentDelta = touchEvent2.position.copy().sub(touchEvent1.position);
    PVector currentCenter = PVector.add(currentDelta.mult(0.5f), touchEvent1.position);

    PVector scale = getGlobalPinchScale();
    PVector touchOffset = currentCenter.sub(startCenter).add(touchEvent1.offset());

    return new PVector(
      -Math.abs(scale.x*touchOffset.x),
      -Math.abs(scale.y*touchOffset.y),
      -Math.abs(scale.z*touchOffset.z));
  }

  public static PinchZoom enableFor(Node n){
    PinchZoom d = getFor(n);

    if(d == null){
      d = new PinchZoom();
      n.use(d);
    }

    return d;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--)
      if(PinchZoom.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
  }

  public static PinchZoom getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(PinchZoom.class.isInstance(ext))
        return (PinchZoom)ext;
    return null;
  }
}
