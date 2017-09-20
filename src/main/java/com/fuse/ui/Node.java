package com.fuse.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.List;

import processing.core.PGraphics;
import processing.core.PVector;
import processing.core.PMatrix3D;

import com.fuse.utils.Event;
import com.fuse.ui.extensions.ExtensionBase;
import com.fuse.ui.extensions.TouchEventForwarder;

/**
 * Base class for scenegraph UI functionality, heavily inspired by the ofxInterface
 * OpenFrameworks addon: https://github.com/galsasson/ofxInterface
 */
public class Node extends TouchReceiver {

  /** PGraphics instance, accessible to all Node instances (and instances of its inheriting classes) */
  protected static PGraphics pg;

  public static void setPGraphics(PGraphics newPg){ pg = newPg; }
  public static PGraphics getPGraphics(){ return pg; }

  private List<Node> childNodes;
  private Node parentNode;
  /** The name of this node, which can be used to find specific child-nodes */
  private String name;
  /** Flag that specifies if the node should be included in the render-loop */
  private boolean bVisible;
  /** Flag that specifies if the node should receive touch events */
  private boolean bInteractive;
  /** Position of the node (pixel based); only the 2D (x and y) attributes are consideren in the for handling touch events */
  private PVector position;
  /** Size of the node (pixel based); only the 2D (x and y) attributes are consideren in the for handling touch events */
  private PVector size;
  /** Rotation of this node along the three axis */
  private PVector rotation;
  /** Scaling of this node, along the three axis */
  private PVector scale;
  /** 3D Matrix that matches with the position, size and rotation attributes */
  private PMatrix3D localTransformMatrix;
  /** Makes sure all offspring Nodes only render within this node's boundaries */
  private Node clippingNode;
  private List<ExtensionBase> extensions = null;

  /** Float-based z-level attribute used for re-ordering Nodes in the render-queue;
   * a higher plane value will put the Node later in the queue, which means
   * it is rendered 'on top' of Nodes with a lower plance value.
   */
  private float plane;

  public Event<Node> newParentEvent;
  /** Triggered when a -direct- child is added to this node */
  public Event<Node> newChildEvent;
  /** Triggered when a child is added to this node, or any of its offspring */
  public Event<Node> newOffspringEvent;
  public Event<Node> positionChangeEvent, sizeChangeEvent, scaleChangeEvent, rotationChangeEvent;

  /** Comparator for ordering a list of Nodes from lower plane to higher plane (used for rendering) */
  static public Comparator<Node> bottomPlaneFirst = (a,b) -> {
    return Float.valueOf(a.getPlane()).compareTo(b.getPlane());
  };

  /** Comparator for ordering a list of Nodes from higher plane to lower plane (used by TouchManager) */
  static public Comparator<Node> topPlaneFirst = (a,b) -> {
    return Float.valueOf(b.getPlane()).compareTo(a.getPlane());
  };

  /** The private _init method is only used in constructor (to keep them DRY) */
  private void _init(){
    childNodes = new ArrayList<Node>();
    parentNode = null;
    bVisible = true;
    bInteractive = true;
    position = new PVector();
    size = new PVector();
    rotation = new PVector();
    scale = new PVector(1.0f, 1.0f, 1.0f);
    localTransformMatrix = new PMatrix3D();
    name = "";
    newParentEvent = new Event<>();
    newChildEvent = new Event<>();
    newOffspringEvent = new Event<>();
    positionChangeEvent = new Event<>();
    sizeChangeEvent = new Event<>();
    rotationChangeEvent = new Event<>();
    scaleChangeEvent = new Event<>();
  }

  /** Default constructor; intializes default value (visible, interactive, empty name, position zero, size zero) */
  public Node(){
    _init();
  }

  /** Constructor which initializes default values but lets caller specify the node's name */
  public Node(String nodeName){
    _init();
    setName(nodeName);
  }

  public void destroy(){
    newParentEvent.destroy();
    newOffspringEvent.destroy();
    newChildEvent.destroy();
    positionChangeEvent.destroy();
    sizeChangeEvent.destroy();

    // detach from scenegraph if still connected
    if(getParent() != null)
      getParent().removeChild(this);

    // recursively destroy this node's subtree
    while(!childNodes.isEmpty()){
      Node childNode = childNodes.get(0);
      this.removeChild(childNode);
      childNode.destroy();
    }
    
    // cleanup this node's extensions
    if(extensions != null){
      while(extensions != null && !extensions.isEmpty()){
        ExtensionBase ext = extensions.get(0);
        this.stopUsing(ext);
        ext.destroy();
      }
    }
  }

  public void update(float dt){
    if(extensions!=null){
      // Copy all extension into a temporary collection before iteration,
      // because extensions might be added/removed while iterating
      List<ExtensionBase> tmpExtensions = new ArrayList<>();
      tmpExtensions.addAll(extensions);

      for(ExtensionBase ext : tmpExtensions)
        if(ext.isEnabled())
          ext.update(dt);
    }
  }

  public void draw(){
    // virtual method
  }

  public void drawDebug(){
    int clr = pg.color(255,this.isTouched()?0:255,0);
    pg.noFill();
    pg.stroke(clr);
    pg.strokeWeight(1.0f);
    pg.rect(0.0f, 0.0f, size.x, size.y);

    pg.noStroke();
    pg.fill(clr);
    pg.text(getName(), 0.0f, 15.0f);

    if(extensions!=null)
      for(ExtensionBase ext : extensions)
        if(ext.isEnabled())
          ext.drawDebug();
  }

  public boolean isVisible(){
    return bVisible;
  }

  public void setVisible(boolean newVisible){
     bVisible = newVisible;
  }

  /// get interactive state (if it responds to touch events)
  public boolean isInteractive(){
    return bInteractive;
  }

  /// set interactive state (if it responds to touch events)
  public Node setInteractive(boolean newValue){
    bInteractive = newValue;
    return this;
  }

  /// returns this node's plane value
  public float getPlane(){
    return plane;
  }

  public Node setPlane(float newPlane){
    plane = newPlane;
    return this;
  }

  public String getName(){
    return new String(name);
  }

  public Node setName(String newName){
    name = newName;
    return this;
  }

  public PVector getPosition(){
    return position.get();
  }

  public PVector getGlobalPosition(){
	  if(this.parentNode != null) {
		  return this.parentNode.toGlobal(this.position); // more 'accurate'
    }

	  return toGlobal(new PVector(0.0f, 0.0f, 0.0f));
  }

  /** @return A PVector which is a translation of the Node's size PVector from local space into screen-space */
  public PVector getGlobalBottomRight(){
    return toGlobal(size);
  }

  public Node setX(float newX){
    return setPosition(newX, position.y, position.z);
  }

  public Node setY(float newY){
    return setPosition(position.x, newY, position.z);
  }

  public Node setZ(float newZ){
    return setPosition(position.x, position.y, newZ);
  }

  public Node setPosition(PVector newPos){
    return setPosition(newPos.x, newPos.y, newPos.z);
  }

  public Node setPosition(float x, float y){
    return this.setPosition(x,y,0f);
  }

  public Node setPosition(float x, float y, float z){
    boolean change = position.x != x || position.y != y || position.z != z;

    if(change){
      position.set(x,y,z);
      updateLocalTransformMatrix();
      positionChangeEvent.trigger(this);
    }

    return this;
  }

  public PVector getSize(){
    return size.get();
  }

  public PVector getSizeScaled(){
    PVector result = size.get();
    result.x = result.x * scale.x;
    result.y = result.y * scale.y;
    result.z = result.z * scale.z;
    return result;
  }

  public Node setWidth(float newWidth){
    return this.setSize(newWidth, size.y);
  }

  public Node setHeight(float newHeight){
    return this.setSize(size.x, newHeight);
  }

  public Node setSize(PVector newSize){
    size = newSize.get();
    sizeChangeEvent.trigger(this);
    return this;
  }

  public Node setSize(float newWidth, float newHeight){
    setSize(new PVector(newWidth, newHeight, 0.0f));
    return this;
  }

  public PVector getScale(){
    return scale.get();
  }

  public Node setScale(float newScale){
    return setScale(new PVector(newScale, newScale, 1.0f));
  }

  public Node setScale(PVector newScale){
    scale = newScale;
    updateLocalTransformMatrix();
    this.scaleChangeEvent.trigger(this);
    return this;
  }

  private void updateLocalTransformMatrix(){
    localTransformMatrix.reset();
    localTransformMatrix.translate(position.x, position.y, position.z);
    localTransformMatrix.rotateX(rotation.x);
    localTransformMatrix.rotateY(rotation.y);
    localTransformMatrix.rotateZ(rotation.z);
    localTransformMatrix.scale(scale.x, scale.y, scale.z);
  }

  public PVector getRotation(){
    return this.rotation.get();
  }

  public Node setRotation(PVector newRot){
    this.rotation = newRot.get();
    updateLocalTransformMatrix();
    this.rotationChangeEvent.trigger(this);
    return this;
  }

  public Node rotate(float amount){
    return this.rotateZ(amount);
  }

  public Node rotateZ(float amount){
    this.rotation.z += amount;
    localTransformMatrix.rotateZ(amount);
    return this;
  }

  public float getRight(){
    return position.x + size.x;
  }

  public float getRightScaled(){
    return position.x + size.x * scale.x;
  }

  public float getBottom(){
    return position.y + size.y;
  }

  public float getBottomScaled(){
    return position.y + size.y * scale.y;
  }

  public Node setGlobalPosition(PVector globalPos){
    Node parentNode = getParent();

    if(parentNode == null){
      setPosition(globalPos);
      return this;
    }

    setPosition(parentNode.toLocal(globalPos));
    return this;
  }

  public boolean isInside(PVector pos){
    PVector localPos = toLocal(pos);
    // return pos.x >= position.x && pos.y >= position.y && pos.x < getRight() && pos.y < getBottom();
    return localPos.x >= 0.0f
      && localPos.y >= 0.0f
      && localPos.x < size.x
      && localPos.y < size.y;
  }

  public PVector toLocal(PVector pos){
    // get and copy our global transformation matrix
    PMatrix3D mat = this.getGlobalTransformMatrix().get();

    // try to invert the matrix
    if(!mat.invert()){
      // System.out.println("could not invert Model's globalTransformMatrix");
      return pos;
    }

    // apply inverted matrix to given position
    PVector localized = new PVector();
    mat.mult(pos, localized);

    // return localised position
    return localized;
  }

  public PVector toGlobal(PVector pos){
    // get and copy our global transformation matrix
    PMatrix3D mat = this.getGlobalTransformMatrix().get();

    // apply inverted matrix to given position
    PVector globalized = new PVector();
    mat.mult(pos, globalized);
    //System.out.println("toGlobal: "+pos.toString()+" to "+globalized.toString());

    // return localised position
    return globalized;
  }

  public TouchEvent toLocal(TouchEvent event){
    TouchEvent newEvent = event.copy();
    newEvent.position = toLocal(event.position);
    newEvent.startPosition = toLocal(event.startPosition);
    if(event.velocity != null)
    	newEvent.velocity = toLocal(event.velocity);
    if(event.velocitySmoothed != null)
    	newEvent.velocitySmoothed = toLocal(event.velocitySmoothed);
    return newEvent;
  }

  public PVector parentToLocalSpace(PVector vec){
    // get and copy our global transformation matrix
    PMatrix3D mat = this.localTransformMatrix.get();

    // try to invert the matrix
    if(!mat.invert()){
      // System.out.println("could not invert Model's globalTransformMatrix");
      return vec;
    }

    // apply inverted matrix to given position
    PVector localized = new PVector();
    mat.mult(vec, localized);

    // return localised position
    return localized;
  }

  public void addChild(Node newChildNode){
    childNodes.add(newChildNode);
    newChildNode.setParent(this);
    newChildEvent.trigger(newChildNode);

    newOffspringEvent.trigger(newChildNode);
    for(Node n : newChildNode.getChildNodes(true /* recursive */)){
      newOffspringEvent.trigger(n);
    }

    newOffspringEvent.forward(newChildNode.newOffspringEvent);
  }

  public void removeChild(Node n){
    childNodes.remove(n);
    newOffspringEvent.stopForward(n.newOffspringEvent);
  }

  public void removeAllChildren(){
    while(!childNodes.isEmpty())
    removeChild(childNodes.get(0));
  }

  public Node getChildWithName(String name){
	  return getChildWithName(name, -1);
  }

  public Node getChildWithName(String name, int maxDepth){
    for(Node childNode : childNodes){
      if(childNode.getName().equals(name))
        return childNode;
    }

    if(maxDepth == 0)
     return null;

    for(Node childNode : childNodes){
      Node result;
      result = childNode.getChildWithName(name, maxDepth-1);
      if(result != null)
        return result;
    }

    return null;
  }

  public List<Node> getChildrenWithName(String name){
	  return getChildrenWithName(name, -1);
  }

  public List<Node> getChildrenWithName(String name, int maxDepth){
    List<Node> result = new ArrayList<>();

    for(Node childNode : childNodes){
      if(childNode.getName().equals(name))
        result.add(childNode);

      if(maxDepth != 0)
        result.addAll(childNode.getChildrenWithName(name, maxDepth-1));
    }

    return result;
  }

  public List<Node> getChildNodes(){
    return getChildNodes(false);
  }

  public List<Node> getChildNodes(boolean recursive){
    List<Node> result = new ArrayList<>();

    if(!recursive){
      result.addAll(childNodes);
      return result;
    }

    for(Node n : childNodes){
      result.add(n);
      result.addAll(n.getChildNodes(true));
    }

    return result;
  }

  public boolean hasChild(Node child){
    return childNodes.contains(child);
  }

  /**
   * Render will render this component and its subtree.
   * usually should be called on the root scene object,
   * but can be used also for offline rendering of any branch of the graph
   *
   * render is done as follows:
   * 1. get list of subtree nodes (only visible ones by default)
   * 2. sort by plane value
   * 3. call draw from back to front
   */
  public void render(){ render(false /* forceAll */); }

  /**
   * Renders components and subtree, see render() for more info.
   * @param forceAll Ignores nodes' visibility flags if true
   */
  public void render(boolean forceAll){
    // Get order list of subtree nodes
    List<Node> nodes = getOrderedSubtreeList(!forceAll);

    // call draw on each node
    for(Node node : nodes){

      Node clipNode = node.getClippingNode();

      // enable clipping if necessary
      if(clipNode != null){
        PVector scrPos = clipNode.getGlobalPosition();
        // TODO this size conversion from local to global space, only really works
        // if the node is rotated to multiples of 90 degrees (or not rotated at all of course).
        PVector bottomRight = clipNode.getGlobalBottomRight();
        pg.imageMode(PGraphics.CORNERS);
        pg.clip(scrPos.x, scrPos.y, bottomRight.x, bottomRight.y);
      }

      pg.pushMatrix();
      {
        pg.applyMatrix(node.getGlobalTransformMatrix());
        node.draw();
      }
      pg.popMatrix();

      // disable clipping if necessary
      if(clipNode != null){
        pg.noClip();
      }
    }
  }

  /**
   * same as 'render' methods, but 'drawDebug' is called instead of 'draw' on each node
   */
  public void renderDebug(){ renderDebug(false); }
  void renderDebug(boolean forceAll){
    // Get order list of subtree nodes
    List<Node> nodes = getOrderedSubtreeList(!forceAll);

    // call draw on each node
    for(Node node : nodes){
      pg.pushMatrix();
      {
        pg.applyMatrix(node.getGlobalTransformMatrix());
        node.drawDebug();
      }
      pg.popMatrix();
    }
  }

  public void updateSubtree(float dt){
    updateSubtree(dt, false);
  }

  public void updateSubtree(float dt, boolean forceAll){
    // update self
    update(dt);

    // loop over all of our direct children
    for(Node node : getChildNodes()){
      if(forceAll || node.isVisible()){
        node.updateSubtree(dt, forceAll);
      }
    }
  }

  public void loadSubtreeList(List<Node> targetList, boolean onlyVisible){
    if(onlyVisible && !isVisible())
      return;

    targetList.add(this);

    for(Node n : childNodes){
      n.loadSubtreeList(targetList, onlyVisible);
    }
  }

  public List<Node> getOrderedSubtreeList(boolean onlyVisible){
    List<Node> targetList = new ArrayList<Node>();
    loadSubtreeList(targetList, onlyVisible);

    // sort nodes by plane value
    Collections.sort(targetList, bottomPlaneFirst);

    return targetList;
  }

  public int indexOf(Node n){
    int idx=0;
    for(Node childNode : getChildNodes()){
      if(childNode == n)
        return idx;
      idx++;
    }

    return -1;
  }

  public PMatrix3D getLocalTransformMatrix(){
    return localTransformMatrix;
  }

  public PMatrix3D getGlobalTransformMatrix(){
    Node parent = getParent();

    // no parent? Then our localTransformMatrix IS our globalTransformMatrix
    if(parent == null)
      return getLocalTransformMatrix();

   // create copy of our local transform matrix so we don't modify the original
   PMatrix3D localMat = getLocalTransformMatrix().get();
   // get our parent's global transform matrix and use it to transform our local matrix
   localMat.preApply(parent.getGlobalTransformMatrix());
   // return our globalized matrix
   return localMat;
  }

  public Node getParent(){
    return parentNode;
  }

  protected void setParent(Node newParent){
    if(newParent == parentNode)
      return;

    parentNode = newParent;
    updateClipping();
    newParentEvent.trigger(this);
  }

  public void forAllChildren(Consumer<Node> func){
    newChildEvent.addListener(func);

    for(Node n : childNodes){
      func.accept(n);
    }
  }

  public void forAllOffspring(Consumer<Node> func){
    forAllOffspring(func, null);
  }

  public void forAllOffspring(Consumer<Node> func, Object owner){
    newOffspringEvent.addListener(func, owner);

    List<Node> nodes = getChildNodes(true /* recursive */);
    for(Node n : nodes){
      func.accept(n);
    }
  }

  private boolean bClipContent = false;

  /**
   * When enabled it will set itself as the clipping node (see setClippingNode method)
   * on all its current and future offspring (subtree) nodes.
   * @param enable Enables when true, disables when false
   */
  public void setClipContent(boolean enable){
    if(bClipContent == enable)
      return;

    bClipContent = enable;
    updateClipping();
  }

  public boolean isClippingContent(){
    return bClipContent;
  }

  public void setClippingNode(Node n){
    clippingNode = n;
  }

  public Node getClippingNode(){
    return clippingNode;
  }

  /** @return Node First parent iwth clipping content enabled */
  public Node getFirstClippingParent(){
    for(Node p = getParent(); p != null; p=p.getParent())
      if(p.isClippingContent())
        return p;

    return null;
  }

  private void updateClipping(){
    this.setClippingNode(this.getFirstClippingParent());

    for(Node n : getChildNodes(true /* recursive */)){
      n.setClippingNode(n.getFirstClippingParent());
    }
  }

  public void use(ExtensionBase newExtension){
    newExtension.setNode(this);
    newExtension.enable();
    this.addExtension(newExtension);
  }

  public void addExtension(ExtensionBase ext){
    // lazy create so extensions attribute doesn't use any memory
    // unless this Node actually gets extensions
    if(extensions == null)
      extensions = new ArrayList<>();

    extensions.add(ext);
  }

  public void stopUsing(ExtensionBase ext){
    if(extensions == null)
      return;

    if(extensions.remove(ext))
      ext.disable();

    if(extensions == null){
      System.err.println("Node.stopUsing extensions suddenly null");
      return;
    }

    if(extensions.isEmpty())
      extensions = null; // cleanup
  }

  public List<ExtensionBase> getExtensions(){
    if(extensions == null)
      return new ArrayList<>();
    return extensions;
  }

  public Node enable(boolean _enable){
    this.setVisible(_enable);
    this.setInteractive(_enable);
    return this;
  }

  public Node enable(){ return this.enable(true); }
  public Node disable(){ return this.enable(false); }

  public void withChild(String name, Consumer<Node> func){
    withChild(name, -1, func);
  }

  public void withChild(String name, int maxLevel, Consumer<Node> func){
    Node n = getChildWithName(name, maxLevel);
    if(n != null)
      func.accept(n);
  }

  public void withChildren(String name, Consumer<Node> func){
    withChildren(name, -1, func);
  }

  public void withChildren(String name, int maxLevel, Consumer<Node> func){
    List<Node> nodes = getChildrenWithName(name, maxLevel);
    for(Node n : nodes)
      func.accept(n);
  }

  /**
   * Creates an extension that monitors the source for touch events and passed them on to this node
   * These two methods create a circular dependency between Node and TouchEventForwarder, however they
   * merely exist for providing an easy API
   */
  public ExtensionBase copyAllTouchEventsFrom(TouchReceiver source){
    return TouchEventForwarder.enableFromTo(source, this);
  }

  public ExtensionBase stopCopyingAllTouchEventsFrom(TouchReceiver source){
    return TouchEventForwarder.disableFromTo(source, this);
  }

  // layout methods

  public Node placeLeft(Node subject){
    return this.placeLeft(subject, false);
  }

  public Node placeLeft(Node subject, boolean active){
    if(active){
      logger.warning("active layouting in Node not yet implemented");
    }

    subject.setX(this.position.x - subject.getSizeScaled().x);
    return this;
  }

  public Node placeRight(Node subject){
    return this.placeRight(subject, false);
  }

  public Node placeRight(Node subject, boolean active){
    if(active){
      logger.warning("active layouting in Node not yet implemented");
    }

    subject.setX(this.getRightScaled());
    return this;
  }

  public Node placeAbove(Node subject){
    return this.placeAbove(subject, false);
  }

  public Node placeAbove(Node subject, boolean active){
    if(active){
      logger.warning("active layouting in Node not yet implemented");
    }

    subject.setY(this.position.y - subject.getSizeScaled().y);
    return this;
  }

  public Node placeBelow(Node subject){
    return this.placeBelow(subject, false);
  }

  public Node placeBelow(Node subject, boolean active){
    if(active){
      logger.warning("active layouting in Node not yet implemented");
    }

    subject.setY(this.getBottomScaled());
    return this;
  }
}
