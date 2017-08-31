package com.fuse.ui.extensions;

import java.util.logging.Logger;
import com.fuse.ui.Node;

public class ExtensionBase {
  protected Logger logger;
  protected Node node;
  private boolean bEnabled;

  public ExtensionBase(){
    logger = Logger.getLogger(ExtensionBase.class.getName());
  }

  public void destroy(){
    disable();
    node = null;
  }

  public void update(float dt){
    // override this method extension-specific functionality
  }

  public void drawDebug(){
    // override this method with extension-specific functionality
  }

  public void setNode(Node newNode){
    node = newNode;
  }

  public void enable(){
    this.bEnabled = true;
  }

  public void enable(Node newNode){
    setNode(newNode);
    this.enable();
  }

  // virtual
  public void disable(){
    this.bEnabled = false;
  }

  public Node getNode(){
    return this.node;
  }

  public boolean isEnabled(){
    return bEnabled;
  }
}
