package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Swiper extends ExtensionBase {

  // private PVector originalNodePosition = null;
  private boolean bSnapBackEnabled = false;
  private boolean bInstantSwipe = false;
  private Draggable draggable;
  private float maxOffsetLeft = -100.0f;
  private float minOffsetRight = 100.0f;

  public Event<Swiper> swipeEvent, swipeLeftEvent, swipeRightEvent;

  public Swiper(){
    swipeEvent = new Event<>();
    swipeLeftEvent = new Event<>();
    swipeRightEvent = new Event<>();
  }

  @Override public void enable(){
    super.enable();

    // constrain vertical movement
    Constrain.enableFor(node).setFixY();
    draggable = Draggable.enableFor(node);

    draggable.endEvent.addListener((Draggable endedDraggable) -> {

      if(willSwipeLeft()){
        swipeLeftEvent.trigger(this);

        if(isInstantSwipeEnabled()){
          node.setX(endedDraggable.getOriginalPosition().x - (node.getParent() == null ? 0.0f : node.getParent().getSize().x));
        }

        return;
      }

      if(willSwipeRight()){
        swipeRightEvent.trigger(this);
        if(isInstantSwipeEnabled()){
          node.setX(endedDraggable.getOriginalPosition().x + (node.getParent() == null ? 0.0f : node.getParent().getSize().x));
        }

        return;
      }

      if(isSnapBackEnabled()){
        node.setPosition(endedDraggable.getOriginalPosition());
      }
    }, this);
  }

  @Override public void disable(){
    super.disable();

    if(draggable != null){
      draggable.endEvent.removeListeners(this);
      draggable.disable();
      if(this.node != null)
        this.node.stopUsing(draggable);

      draggable = null;
    }
  }

  public boolean willSwipe(){
    return willSwipeLeft() || willSwipeRight();
  }

  public boolean willSwipeLeft(){
    return draggable != null && draggable.getOffset().x <= getMaxOffsetLeft();
  }

  public boolean willSwipeRight(){
    return draggable != null && draggable.getOffset().x >= getMinOffsetRight();
  }

  public float getMaxOffsetLeft(){
    return maxOffsetLeft;
  }

  public void setMaxOffsetLeft(float max){
    maxOffsetLeft = max;
  }

  public float getMinOffsetRight(){
    return minOffsetRight;
  }

  public void setMinOffsetRight(float min){
    minOffsetRight = min;
  }

  public void setMinOffset(float offset){
    setMinOffsetRight(offset);
    setMaxOffsetLeft(-offset);
  }

  public boolean isSnapBackEnabled(){
    return bSnapBackEnabled;
  }

  public void setSnapBackEnabled(boolean enable){
    bSnapBackEnabled = enable;
  }

  public boolean isInstantSwipeEnabled(){
    return bInstantSwipe;
  }

  public void setInstantSwipeEnabled(boolean enable){
    bInstantSwipe = enable;
  }

  public static Swiper enableFor(Node n){
    Swiper d = getFor(n);

    if(d == null){
      d = new Swiper();
      n.use(d);
    }

    return d;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--)
      if(Swiper.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
  }

  public static Swiper getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(Swiper.class.isInstance(ext))
        return (Swiper)ext;
    return null;
  }

}
