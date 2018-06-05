package com.fuse.ui.extensions;

import java.util.function.Consumer;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;
import com.fuse.ui.TouchReceiver;

public class TouchEventForwarder extends ExtensionBase {

  private TouchReceiver source = null;

  public TouchEventForwarder(TouchReceiver source){
    this.source = source;
  }

  @Override public void destroy(){
    super.destroy();
    source = null;
  }

  @Override protected void setup(){
    if(this.source == null){
      logger.warning("no source");
      return;
    }

    source.touchEvent.addListener((TouchEvent evt) -> {
      //TouchEvent newEvent = evt.copy();
      Node originalNode = evt.node;
      Node originalMostRecentNode = evt.mostRecentNode;

      // change event so it seems to belong to our subject
      if(evt.node == source)
        evt.node = this.node;
      if(evt.mostRecentNode == source)
        evt.mostRecentNode = this.node;

      // forward event
      node.receiveTouchEvent(evt);

      // restore event
      evt.node = originalNode;
      evt.mostRecentNode = originalMostRecentNode;
    }, this);
  }

  @Override protected void teardown(){
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
	ExtensionBase ext = getForFromTo(from, to);

	if(ext != null)
		to.stopUsing(ext);

	return ext;
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

  public static void enableForChildNames(Node to, String[] childnames){
    // create copy logic func
    Consumer<Node> offspringProcessor = (Node childNode) -> {
      // register listeners to copy events from all children in the childNames list
      for(String name : childnames){
        if(childNode.getName().equals(name.trim()) && !name.trim().equals("")){
          enableFromTo(childNode, to);
        }
      }
    };

    for(Node n : to.getChildNodes())
      offspringProcessor.accept(n);

    // also register listener for when child (or more children with the same name) gets added later
    to.newOffspringEvent.addListener(offspringProcessor, TouchEventForwarder.class); // TODO this never gets unregistered currently
  }
}
