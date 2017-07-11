package com.fuse.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.List;

import com.fuse.utils.Event;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.core.PMatrix3D;

/**
 * Base class for scenegraph UI functionality, heavily inspired by the ofxInterface
 * OpenFrameworks addon: https://github.com/galsasson/ofxInterface
 */
public class Node extends TouchReceiver {

  /** PApplet instance, accessible to all Node instances (and instances of its inheriting classes) */
  public static PApplet papplet;
  /** PGraphics instance, accessible to all Node instances (and instances of its inheriting classes) */
  protected static PGraphics pg;

  public static void setPApplet(PApplet newPapplet){ papplet = newPapplet; }
  public static PApplet getPApplet(){ return papplet; }
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
  /** Flag that specifies if the node currently being touched */
  private boolean bTouched;
  /** Position of the node (pixel based); only the 2D (x and y) attributes are consideren in the for handling touch events */
  private PVector position;
  /** Size of the node (pixel based); only the 2D (x and y) attributes are consideren in the for handling touch events */
  private PVector size;
  /** Rotation of this node along the three axis */
  private PVector rotation;
  /** 3D Matrix that matches with the position, size and rotation attributes */
  private PMatrix3D localTransformMatrix;
  /** Makes sure all offspring Node render within this node's boundaries */
  private boolean clipContent;
  private Node clippingNode;

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

  /** Comparator for ordering a list of Nodes from lower plane to higher plane (used for rendering) */
  static public Comparator<Node> bottomPlaneFirst = (a,b) -> {
    return Float.valueOf(a.getPlane()).compareTo(b.getPlane());
  };

  /** Comparator for ordering a list of Nodes from higher plane to lower plane (used by TouchManager) */
  static public Comparator<Node> topPlaneFirst = (a,b) -> {
    return Float.valueOf(b.getPlane()).compareTo(a.getPlane());
  };

  public Node(){
    childNodes = new ArrayList<Node>();
    parentNode = null;
    bVisible = true;
    bInteractive = true;
    bTouched = false;
    position = new PVector();
    size = new PVector();
    rotation = new PVector();
    localTransformMatrix = new PMatrix3D();
    name = "";
    newParentEvent = new Event<>();
    newChildEvent = new Event<>();
    newOffspringEvent = new Event<>();

    touchDownEvent.addListener((TouchEvent e) -> {
      bTouched = true;
    }, this);

    touchUpEvent.addListener((TouchEvent e) -> {
      bTouched = false;
    }, this);

    touchEnterEvent.addListener((TouchEvent e) -> {
        bTouched = true;
    }, this);

    touchExitEvent.addListener((TouchEvent e) -> {
        bTouched = false;
    }, this);
  }

  public void update(float dt){
    // virtual method
  }

  public void draw(){
    // virtual method
  }

  public void drawDebug(){
    int clr = pg.color(255,bTouched?0:255,0);
    pg.noFill();
    pg.stroke(clr);
    pg.rect(0.0f, 0.0f, size.x, size.y);

    pg.noStroke();
    pg.fill(clr);
    pg.text(getName(), 0.0f, 15.0f);
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
  public void setInteractive(boolean newValue){
    bInteractive = newValue;
  }

  /// returns this node's plane value
  public float getPlane(){
    return plane;
  }

  public void setPlane(float newPlane){
    plane = newPlane;
  }

  public String getName(){ return name; }
  public void setName(String newName){
    name = newName;
  }

  public PVector getPosition(){
    return position;
  }

  public PVector getGlobalPosition(){
    return toGlobal(new PVector(0.0f, 0.0f, 0.0f));
  }

  public void setX(float newX){
    setPosition(newX, position.y, position.z);
  }

  public void setY(float newY){
    setPosition(position.x, newY, position.z);
  }

  public void setZ(float newZ){
    setPosition(position.x, position.y, newZ);
  }

  public void setPosition(PVector newPos){
    setPosition(newPos.x, newPos.y, newPos.z);
  }

  public void setPosition(float x, float y){
    this.setPosition(x,y,0f);
  }

  public void setPosition(float x, float y, float z){
    localTransformMatrix.translate(x - position.x, y - position.y, z - position.z);
    position.set(x,y,z);
  }

  public PVector getSize(){
    return size;
  }

  public void setWidth(float newWidth){
    size.x = newWidth;
  }

  public void setHeight(float newHeight){
    size.y = newHeight;
  }

  public void setSize(PVector newSize){
    size = newSize.copy();
  }

  public void setSize(float newWidth, float newHeight){
    size.set(newWidth, newHeight, 0.0f);
  }

  public void rotate(float amount){
    this.rotateZ(amount);
  }

  public void rotateZ(float amount){
    this.rotation.z += amount;
    localTransformMatrix.rotateZ(amount);
  }

  public float getRight(){
    return position.x + size.x;
  }

  public float getBottom(){
    return position.y + size.y;
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
      System.out.println("could not invert Model's globalTransformMatrix");
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

    // return localised position
    return globalized;
  }

  public TouchEvent toLocal(TouchEvent event){
    TouchEvent newEvent = event.copy();
    newEvent.position = toLocal(event.position);
    newEvent.startPosition = toLocal(event.startPosition);
    return newEvent;
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
    if(!recursive)
      return childNodes;

    List<Node> result = new ArrayList<>();
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
      if(clipNode != null){
        PVector scrPos = clipNode.getGlobalPosition();
        PVector size = clipNode.getSize();
        pg.clip(scrPos.x, scrPos.y, scrPos.x+size.x, scrPos.y+size.y);
      }

      pg.pushMatrix();
      {
        pg.applyMatrix(node.getGlobalTransformMatrix());
        node.draw();
      }
      pg.popMatrix();

      if(clipNode != null){
        pg.noClip();
        // pg.popMatrix();
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
    boolean change = (newParent != parentNode);
    parentNode = newParent;
    if(change){
      newParentEvent.trigger(this);
    }
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

  public void setClipContent(boolean enable){
    boolean enabled = (enable && !this.clipContent);
    boolean disabled = (this.clipContent && !enable);
    this.clipContent = enable;

    if(enabled){
      this.forAllOffspring((Node n) -> {
        n.setClippingNode(this);
      }, this);
    }

    if(disabled){
      newOffspringEvent.removeListeners(this);
      for(Node n : getChildNodes(true /* recursive */)){
        n.setClippingNode(null);
      }
    }
  }

  public void setClippingNode(Node n){
    clippingNode = n;
  }

  public Node getClippingNode(){
    return clippingNode;
  }
}