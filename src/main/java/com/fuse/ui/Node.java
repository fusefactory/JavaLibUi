package com.fuse.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

  public static PApplet papplet;
  public static PGraphics pg;

  public static void setPApplet(PApplet newPapplet){
    papplet = newPapplet;
  }

  public static void setPGraphics(PGraphics newPg){
    pg = newPg;
  }

  private List<Node> childNodes;
  private Node parentNode;
  /// should it be rendered?
  private boolean bVisible;
  /// should it react to touch events?
  private boolean bInteractive;
  /// is the node currently being touched
  private boolean bTouched;
  /// 2D position and size attributes (pixel based)
  private PVector position, size;
  private String name;
  private PVector rotation;
  private PMatrix3D localTransformMatrix;

  /// simple float attribute which is used to sort elements before rendering them.
  // Nodes with a higher plane number are rendered later and thus end up 'on top' (like z-index in CSS)
  private float plane;

  public Event<Node> newParentEvent;

  static public Comparator<Node> bottomPlaneFirst = (a,b) -> {
    return Float.valueOf(a.getPlane()).compareTo(b.getPlane());
  };

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
    return toGlobal(position);
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
  }

  public void removeChild(Node n){
    childNodes.remove(n);
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
    return childNodes;
  }

  public boolean hasChild(Node child){
    return childNodes.contains(child);
  }

  /**
   * \brief render will render this component and its subtree.
   * usually should be called on the root scene object,
   * but can be used also for offline rendering of any branch of the graph
   *
   * render is done as follows:
   * 1. get list of subtree nodes (only visible ones by default)
   * 2. sort by plane value
   * 3. call draw from back to front
   */
  public void render(){ render(false /* forceAll */); }
  public void render(boolean forceAll){
    // Get order list of subtree nodes
    List<Node> nodes = getOrderedSubtreeList(!forceAll);

    // call draw on each node
    for(Node node : nodes){
      pg.pushMatrix();
      {
        pg.applyMatrix(node.getGlobalTransformMatrix());
        node.draw();
      }
      pg.popMatrix();
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
}
