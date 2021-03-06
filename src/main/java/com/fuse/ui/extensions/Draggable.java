package com.fuse.ui.extensions;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;
import com.fuse.utils.Event;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Draggable extends TransformerExtension {
  private PVector originalNodePosition = null;
  private PVector originalNodePositionGlobal = null;
  private boolean bDragging = false;
  private boolean bAbortOnSecondTouch = true;
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
    startEvent.destroy();
    endEvent.destroy();
    super.destroy();
  }

  @Override protected void setup(){
    node.touchDownEvent.addListener((TouchEvent event) -> {
      if(bDragging) {
        if(bAbortOnSecondTouch) {
          stop();
        }

        return;
      }

      Node ourNode = this.getNode();

      if(event.node != ourNode)
        return; // touch didn't start on our node

      start(event);
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(bDragging && dragEvent == event){
    	  stop();
      }
    }, this);
  }

  @Override public void teardown(){
    if(node != null){
      node.touchDownEvent.removeListeners(this);
      node.touchUpEvent.removeListeners(this);
    }

    if(bDragging){
      bDragging = false;
      dragEvent = null;
      endEvent.trigger(this);
    }
  }

  @Override public void update(float dt){
    if(bDragging && this.dragEvent != null){
      if(this.node.getActiveTouchEvents().size() == 1){
        apply(this.dragEvent.offset());
      }

      if(this.dragEvent.isFinished()){
    	  stop();
      }
    }

    super.update(dt);
  }

  private void start(TouchEvent event){
	  if(event == null) return;
      originalNodePosition = this.node.getPosition();
      originalNodePositionGlobal = this.node.getGlobalPosition();
      dragEvent = event;
      bDragging = true;
      //logger.info("START DRAGGING");
      startEvent.trigger(this);
  }

  private void stop(){
	  if(bDragging || dragEvent != null) {
		  bDragging = false;
		  dragEvent = null;
		  super.stopActiveTransformations();
		  endEvent.trigger(this);
	  }
  }

  @Override public void drawDebug(){
    super.drawDebug();

    PGraphics pg = Node.getPGraphics();
    pg.textSize(12);
    pg.textAlign(PApplet.LEFT);
    pg.ellipseMode(PGraphics.CENTER);
    pg.noStroke();
    pg.colorMode(PGraphics.RGB, 255);
    pg.fill(pg.color(0,0,255));

    if(originalNodePosition != null && originalNodePositionGlobal != null){
      pg.text("Original pos (local/global): "+originalNodePosition.toString()+"/"+originalNodePositionGlobal.toString(),
        0, 20);
    }

    if(originalNodePositionGlobal != null){
      pg.fill(pg.color(0,0,255));
      PVector pos = this.node.toLocal(originalNodePositionGlobal);
      pg.ellipse(pos.x, pos.y, 20, 20);
    }

    if(this.dragEvent != null){
      pg.fill(pg.color(255,0,0, 200));
      PVector pos = this.node.toLocal(dragEvent.startPosition);
      pg.ellipse(pos.x, pos.y, 20, 20);

      pg.fill(pg.color(0,255,0, 200));
      pos = this.node.toLocal(dragEvent.position);
      pg.ellipse(pos.x, pos.y, 25, 25);
    }
  }

  public void apply(PVector globalDragOffset){
    if(originalNodePositionGlobal == null) // should  be set at first touch
    	return;

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
    Draggable d = getFor(n);

    if(d == null){
      d = new Draggable();
      n.use(d);
    }
    return d;
  }

  public static Draggable getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(Draggable.class.isInstance(ext))
        return (Draggable)ext;
    return null;
  }

  public static void disableFor(Node n){
    for(ExtensionBase ext : n.getExtensions()) {
      if(Draggable.class.isInstance(ext))
          n.stopUsing(ext);
    }
  }

  public void setAbortOnSecondTouch(boolean enable) {
	  this.bAbortOnSecondTouch = enable;
  }

  public boolean getAbortOnSecondTouch() {
	  return bAbortOnSecondTouch;
  }
}
