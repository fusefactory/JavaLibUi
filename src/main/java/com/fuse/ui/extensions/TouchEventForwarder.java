package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchReceiver;
import com.fuse.ui.TouchEvent;

public class TouchEventForwarder extends ExtensionBase {

  private TouchReceiver source = null;

  public TouchEventForwarder(TouchReceiver source){
    this.source = source;
  }

  @Override public void enable(Node newNode){
    super.enable(newNode);

    if(this.source == null){
      logger.warning("no source");
      return;
    }

    source.touchEvent.addListener((TouchEvent evt) -> {
      TouchEvent newEvent = evt.copy();
      if(evt.node == source)
        newEvent.node = this.node;
      if(evt.mostRecentNode == source)
        newEvent.mostRecentNode = this.node;
      node.receiveTouchEvent(newEvent);
    }, this);
  }

  @Override public void disable(){
    if(this.source == null){
      logger.warning("no source");
      return;
    }

    source.touchEvent.removeListeners(this);
  }

  protected TouchReceiver getSource(){
    return source;
  }

  public static TouchEventForwarder enableFromTo(TouchReceiver from, Node to){
    TouchEventForwarder tef = getForFromTo(from, to);

    if(tef == null){
      tef = new TouchEventForwarder(from);
      to.use(tef);
    }

    return tef;
  }

  public static ExtensionBase disableFromTo(TouchReceiver from, Node to){
    for(int i=to.getExtensions().size()-1; i>=0; i--){
      ExtensionBase ext = to.getExtensions().get(i);
      if(TouchEventForwarder.class.isInstance(ext)){
        to.stopUsing(ext);
        return ext;
      }
    }

    return null;
  }

  public static TouchEventForwarder getForFromTo(TouchReceiver from, Node to){
    for(ExtensionBase ext : to.getExtensions()){
      if(TouchEventForwarder.class.isInstance(ext)){
        TouchEventForwarder tef = (TouchEventForwarder)ext;
        if(tef.getSource() == from)
          return tef;
      }
    }

    return null;
  }

}
