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

  protected void setup(){
    // virtual
  }

  protected void teardown(){
    // virtual
  }

  public void destroy(){
    if(isEnabled())
      this.disable();

    if(this.node != null)
      this.node.stopUsing(this);
  }

  public void enable(){
    if(!isEnabled()){
      this.bEnabled = true;
      this.setup();
    }
  }

  public void disable(){
    if(isEnabled()){
      this.bEnabled = false;
      this.teardown();
    }
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

  @Deprecated
  public void enable(Node newNode){
    setNode(newNode);
    this.enable();
  }


  public Node getNode(){
    return this.node;
  }

  public boolean isEnabled(){
    return bEnabled;
  }
}
