package com.fuse.ui.extensions;

import java.util.logging.Logger;
import com.fuse.ui.Node;

public class ExtensionBase {
  protected Logger logger;
  protected Node node;

  public ExtensionBase(){
    logger = Logger.getLogger(ExtensionBase.class.getName());
  }

  public void enable(Node newNode){
    this.node = newNode;
  }

  // virtual
  public void disable(){
  }

  public Node getNode(){
    return this.node;
  }
}
