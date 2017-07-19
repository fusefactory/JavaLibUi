package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Constrain extends ExtensionBase {
  private boolean[] bAxisConstrains;

  private PVector constrainPos;

  public Constrain(){
    bAxisConstrains = new boolean[3];
    setConstrainX(true);
    setConstrainY(true);
    setConstrainZ(true);
    constrainPos = new PVector();
  }

  @Override public void enable(Node newNode){
    super.enable(newNode);
    constrainPos = node.getPosition();

    node.positionChangeEvent.whenTriggered(() -> {
      PVector newPos = node.getPosition();
      if(bAxisConstrains[0]) newPos.x = constrainPos.x;
      if(bAxisConstrains[1]) newPos.y = constrainPos.y;
      if(bAxisConstrains[2]) newPos.z = constrainPos.z;
      node.setPosition(newPos);
    }, this);
  }

  @Override public void disable(){
    super.disable();
    node.positionChangeEvent.stopWhenTriggeredCallbacks(this);
  }

  public void setConstrainX(){ setConstrainX(true); }
  public void setConstrainX(boolean enable){
    bAxisConstrains[0] = enable;
    if(enable && constrainPos != null && node != null){
      constrainPos.x = node.getPosition().x;
    }
  }
  public void setConstrainY(){ setConstrainY(true); }
  public void setConstrainY(boolean enable){
    bAxisConstrains[1] = enable;
    if(enable && constrainPos != null && node != null){
      constrainPos.y = node.getPosition().y;
    }
  }
  public void setConstrainZ(){ setConstrainZ(true); }
  public void setConstrainZ(boolean enable){
    bAxisConstrains[2] = enable;
    if(enable && constrainPos != null && node != null){
      constrainPos.z = node.getPosition().z;
    }
  }

  public static Constrain enableFor(Node n){
    return enableFor(n, true);
  }

  public static Constrain enableFor(Node n, boolean onByDefault){
    for(ExtensionBase ext : n.getExtensions())
      if(Constrain.class.isInstance(ext))
        return (Constrain)ext;

    Constrain d = new Constrain();
    if(!onByDefault){
      d.setConstrainX(false);
      d.setConstrainY(false);
      d.setConstrainZ(false);
    }
    n.use(d);
    return d;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--)
      if(Constrain.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
  }
}
