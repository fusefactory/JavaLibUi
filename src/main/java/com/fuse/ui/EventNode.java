package com.fuse.ui;

import com.fuse.utils.Event;

/**
 * Custom Node type that extends the Node class with two events;
 * drawEvent and updateEvent, which are triggered in respectively the
 * draw() and update(float dt) methods.
 * Using Events in the update and draw loops could strongly affect
 * performance, therefore this class is primarily menat for development
 * and prototyping purposes.
 * For slightly better performance, consider using the LambdaNode.
 */
public class EventNode extends Node {

  public Event<Node> drawEvent;
  public Event<Float> updateEvent;

  private void _init(){
    drawEvent = new Event<>();
    updateEvent = new Event<>();
  }

  public EventNode(){
    _init();
  }

  public EventNode(String nodeName){
    super(nodeName);
    _init();
  }

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    drawEvent.trigger(this);
  }

  @Override public void update(float dt){
    updateEvent.trigger(dt);
  }
}
