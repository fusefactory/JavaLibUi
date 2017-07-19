package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Constrain extends ExtensionBase {
  private boolean[] fixedAxis;
  private Float[] axisMinValues = {null, null, null};
  private Float[] axisMaxValues = {null, null, null};
  private PVector constrainPos;

  public Constrain(){
    fixedAxis = new boolean[3];
    setFixX(true);
    setFixY(true);
    setFixZ(true);
    constrainPos = new PVector();
  }

  @Override public void enable(Node newNode){
    super.enable(newNode);
    constrainPos = node.getPosition();

    node.positionChangeEvent.whenTriggered(() -> {
      PVector newPos = node.getPosition();
      if(fixedAxis[0]) newPos.x = constrainPos.x;
      if(fixedAxis[1]) newPos.y = constrainPos.y;
      if(fixedAxis[2]) newPos.z = constrainPos.z;

      if(axisMinValues[0] != null && axisMinValues[0] > newPos.x) newPos.x = axisMinValues[0];
      if(axisMinValues[1] != null && axisMinValues[1] > newPos.y) newPos.y = axisMinValues[1];
      if(axisMinValues[2] != null && axisMinValues[2] > newPos.z) newPos.z = axisMinValues[2];

      if(axisMaxValues[0] != null && axisMaxValues[0] < newPos.x) newPos.x = axisMaxValues[0];
      if(axisMaxValues[1] != null && axisMaxValues[1] < newPos.y) newPos.y = axisMaxValues[1];
      if(axisMaxValues[2] != null && axisMaxValues[2] < newPos.z) newPos.z = axisMaxValues[2];

      node.setPosition(newPos);
    }, this);
  }

  @Override public void disable(){
    super.disable();
    node.positionChangeEvent.stopWhenTriggeredCallbacks(this);
  }

  public void setFixX(){ setFixX(true); }
  public void setFixX(boolean enable){
    fixedAxis[0] = enable;
    if(enable && constrainPos != null && node != null){
      constrainPos.x = node.getPosition().x;
    }
  }

  public void setFixY(){ setFixY(true); }
  public void setFixY(boolean enable){
    fixedAxis[1] = enable;
    if(enable && constrainPos != null && node != null){
      constrainPos.y = node.getPosition().y;
    }
  }

  public void setFixZ(){ setFixZ(true); }
  public void setFixZ(boolean enable){
    fixedAxis[2] = enable;
    if(enable && constrainPos != null && node != null){
      constrainPos.z = node.getPosition().z;
    }
  }

  public void setMinX(Float min){ axisMinValues[0] = min; if(min != null && node.getPosition().x < min) node.setX(min); }
  public void setMinY(Float min){ axisMinValues[1] = min; if(min != null && node.getPosition().y < min) node.setY(min); }
  public void setMinZ(Float min){ axisMinValues[2] = min; if(min != null && node.getPosition().z < min) node.setZ(min); }

  public void setMaxX(Float max){ axisMaxValues[0] = max; if(max != null && node.getPosition().x > max) node.setX(max); }
  public void setMaxY(Float max){ axisMaxValues[1] = max; if(max != null && node.getPosition().y > max) node.setY(max); }
  public void setMaxZ(Float max){ axisMaxValues[2] = max; if(max != null && node.getPosition().z > max) node.setZ(max); }

  public static Constrain enableFor(Node n){
    return enableFor(n, true);
  }

  public static Constrain enableFor(Node n, boolean onByDefault){
    for(ExtensionBase ext : n.getExtensions())
      if(Constrain.class.isInstance(ext))
        return (Constrain)ext;

    Constrain d = new Constrain();
    if(!onByDefault){
      d.setFixX(false);
      d.setFixY(false);
      d.setFixZ(false);
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
