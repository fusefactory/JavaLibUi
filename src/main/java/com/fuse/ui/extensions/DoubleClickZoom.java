package com.fuse.ui.extensions;

import java.util.List;

import processing.core.PVector;
import processing.core.PGraphics;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class DoubleClickZoom extends TransformerExtension {
  // attributes
  private PVector originalScale, originalPosition;
  private Long lastClickTime = null;
  // configurables
  private Long doubleClickMaxIntervalMillis = 850l; // TODO; make configurable (and refactor to TouchManager)
  private PVector scaleFactor = new PVector(2.0f, 2.0f, 1.0f);

  @Override public void enable(){
    if(this.isEnabled() || this.node == null) return;
    super.enable();

    this.originalScale = this.node.getScale();
    this.originalPosition = this.node.getPosition();

    this.node.touchClickEvent.addListener((TouchEvent event) -> {
      Long t = System.currentTimeMillis();

      // no earlier click...
      if(this.lastClickTime == null) {
        this.lastClickTime = t;
        return;
      }

      // last click too long ago...
      if(t - this.lastClickTime > this.doubleClickMaxIntervalMillis) {
        this.lastClickTime = t;
        return;
      }

      // double click!
      this.lastClickTime = null;
      this.onDoubleClick(event);
    }, this);
  }

  @Override public void disable(){
    System.out.println("222");
    if(this.node != null)
      this.node.touchClickEvent.removeListeners(this);
    super.disable();
  }

  private void onDoubleClick(TouchEvent event) {
    if(Math.abs(this.node.getScale().x - this.originalScale.x) > 0.03f){
      this.transformScale(this.originalScale.get());
      this.transformPosition(this.originalPosition.get());
    } else {
      PVector newScale = new PVector(this.originalScale.x*this.scaleFactor.x, this.originalScale.y*this.scaleFactor.y, this.originalScale.z*this.scaleFactor.z);

      //TouchEvent localEvent = this.node.toLocal(event);
      //PVector localPos = localEvent.position;

      PVector originalSize = this.node.getSize();
      originalSize.x = originalSize.x * this.originalScale.x;
      originalSize.y = originalSize.y * this.originalScale.y;

      PVector originalCenter = this.originalPosition.get();
      originalSize.mult(0.5f);
      originalCenter.add(originalSize);

      PVector newSize = this.node.getSize();
      newSize.x = newSize.x * newScale.x;
      newSize.y = newSize.y * newScale.y;

      PVector newPos = originalCenter.get();
      newSize.mult(-0.5f);
      newPos.add(newSize);

      this.transformScale(newScale); // zoom-in
      this.transformPosition(newPos);
    }
  }

  // config methods // // // // //

  public PVector getScaleFactor(){
    return this.scaleFactor.get();
  }

  public void setScaleFactor(PVector factor){
    this.scaleFactor.set(factor);
  }

  // static factory methods // // // // //

  public static DoubleClickZoom enableFor(Node n){
    DoubleClickZoom d = getFor(n);

    if(d == null){
      d = new DoubleClickZoom();
      n.use(d);
    }

    return d;
  }

  public static DoubleClickZoom getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(DoubleClickZoom.class.isInstance(ext))
        return (DoubleClickZoom)ext;
    return null;
  }

  public static void disableFor(Node n){
    for(ExtensionBase ext : n.getExtensions()) {
      if(DoubleClickZoom.class.isInstance(ext))
          n.stopUsing(ext);
    }
  }
}
