package com.fuse.ui.extensions;

import processing.core.PVector;
import processing.core.PGraphics;
import processing.core.PApplet;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Draggable extends TransformerExtension {
  private PVector originalNodePosition = null;
  private PVector originalNodePositionGlobal = null;
  private boolean bDragging = false;
  private TouchEvent dragEvent = null;
  // configurables
  private boolean bRestore = false;

  // events
  public Event<Draggable> startEvent;
  public Event<Draggable> endEvent;

  public Draggable(){
    super();
    startEvent = new Event<>();
    endEvent = new Event<>();
    this.setSmoothValue(4.0f); // default
  }

  @Override public void destroy(){
    super.destroy();
    startEvent.destroy();
    endEvent.destroy();
  }

  @Override public void enable(){
    super.enable();

    node.touchDownEvent.addListener((TouchEvent event) -> {
      if(bDragging)
        return;

      Node ourNode = this.getNode();

      if(event.node != ourNode)
        return; // touch didn't start on our node

      originalNodePosition = ourNode.getPosition();
      originalNodePositionGlobal = ourNode.getGlobalPosition();
      dragEvent = event;
      bDragging = true;
      startEvent.trigger(this);
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(bDragging && dragEvent == event){
        bDragging = false;
        dragEvent = null;
        endEvent.trigger(this);
      }
    }, this);
  }

  @Override public void disable(){
    super.disable();

    if(node != null){
      node.touchMoveEvent.removeListeners(this);
      node.touchUpEvent.removeListeners(this);
    }

    if(bDragging){
      bDragging = false;
      dragEvent = null;
      endEvent.trigger(this);
    }
  }

  @Override public void update(float dt){
    if(bDragging){
      if(this.node.getActiveTouchEvents().size() == 1){
        apply(this.dragEvent.offset());
      }
    }

    super.update(dt);
  }

  @Override public void drawDebug(){
    PGraphics pg = Node.getPGraphics();
    pg.textSize(12);
    pg.textAlign(PApplet.LEFT);
    pg.noStroke();
    pg.fill(pg.color(0,0,255));
    if(originalNodePosition != null && originalNodePositionGlobal != null)
      pg.text("Original pos (local/global): "+originalNodePosition.toString()+"/"+originalNodePositionGlobal.toString(),
        0, 20);

  }

  public void apply(PVector globalDragOffset){
    if(originalNodePositionGlobal == null) // should already be set at first processed touchMoveEvent, but just to be sure
      originalNodePositionGlobal = this.getNode().getGlobalPosition();

    PVector globPos = globalDragOffset.get();
    globPos.add(originalNodePositionGlobal);
    super.transformPositionGlobal(globPos);
  }

  // state-reader methods // // // // //

  public PVector getOffset(){
    if(originalNodePosition == null || node == null)
      return new PVector(0.0f,0.0f,0.0f);

    PVector result = node.getPosition();
    result.sub(originalNodePosition);
    return result;
  }

  public PVector getOriginalPosition(){
    return originalNodePosition == null ? null : originalNodePosition.get();
  }

  public boolean isDragging(){
    return bDragging;
  }

  // configuration methods // // // // //

  public boolean getRestore(){
    return bRestore;
  }

  public void setRestore(boolean enableRestore){
    bRestore = enableRestore;
  }

  // static factory methods // // // // //

  public static Draggable enableFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(Draggable.class.isInstance(ext))
        return (Draggable)ext;
    Draggable d = new Draggable();
    n.use(d);
    return d;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--)
      if(Draggable.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
  }
}
