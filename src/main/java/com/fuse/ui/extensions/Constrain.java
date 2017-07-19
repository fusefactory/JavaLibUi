package com.fuse.ui.extensions;

import processing.core.PApplet;
import processing.core.PVector;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Constrain extends ExtensionBase {
  private Float[] axisMinValues = {null, null, null};
  private Float[] axisMaxValues = {null, null, null};
  private PVector constrainPos;

  public Event<Float> xPercentageEvent, yPercentageEvent, zPercentageEvent;

  public Constrain(){
    setFixX(true);
    setFixY(true);
    setFixZ(true);
    constrainPos = new PVector();
    xPercentageEvent = new Event<>();
    yPercentageEvent = new Event<>();
    zPercentageEvent = new Event<>();
  }

  @Override public void enable(Node newNode){
    super.enable(newNode);
    constrainPos = node.getPosition();

    node.positionChangeEvent.whenTriggered(() -> {
      PVector newPos = node.getPosition();

      if(axisMinValues[0] != null && axisMinValues[0] > newPos.x) newPos.x = axisMinValues[0];
      if(axisMinValues[1] != null && axisMinValues[1] > newPos.y) newPos.y = axisMinValues[1];
      if(axisMinValues[2] != null && axisMinValues[2] > newPos.z) newPos.z = axisMinValues[2];

      if(axisMaxValues[0] != null && axisMaxValues[0] < newPos.x) newPos.x = axisMaxValues[0];
      if(axisMaxValues[1] != null && axisMaxValues[1] < newPos.y) newPos.y = axisMaxValues[1];
      if(axisMaxValues[2] != null && axisMaxValues[2] < newPos.z) newPos.z = axisMaxValues[2];

      node.setPosition(newPos);

      if(axisMinValues[0] != null && axisMaxValues[0] != null){
        if(axisMinValues[0].equals(axisMaxValues[0]))
          xPercentageEvent.trigger(1.0f);
        else
          xPercentageEvent.trigger(PApplet.map(newPos.x, axisMinValues[0], axisMaxValues[0], 0.0f, 1.0f));
      }

      if(axisMinValues[1] != null && axisMaxValues[1] != null){
        if(axisMinValues[1].equals(axisMaxValues[1]))
          yPercentageEvent.trigger(1.0f);
        else
          yPercentageEvent.trigger(PApplet.map(newPos.y, axisMinValues[1], axisMaxValues[1], 0.0f, 1.0f));
      }

      if(axisMinValues[2] != null && axisMaxValues[2] != null){
        if(axisMinValues[2].equals(axisMaxValues[2]))
          zPercentageEvent.trigger(1.0f);
        else
          zPercentageEvent.trigger(PApplet.map(newPos.z, axisMinValues[2], axisMaxValues[2], 0.0f, 1.0f));
      }
    }, this);
  }

  @Override public void disable(){
    super.disable();
    node.positionChangeEvent.stopWhenTriggeredCallbacks(this);
  }

  public void setFixX(){ setFixX(true); }
  public void setFixX(boolean enable){
    if(enable && node != null){
      setMinX(node.getPosition().x);
      setMaxX(node.getPosition().x);
    }else{
      setMinX(null);
      setMaxX(null);
    }
  }

  public void setFixY(){ setFixY(true); }
  public void setFixY(boolean enable){
    if(enable && node != null){
      setMinY(node.getPosition().y);
      setMaxY(node.getPosition().y);
    } else {
      setMinY(null);
      setMaxY(null);
    }
  }

  public void setFixZ(){ setFixZ(true); }
  public void setFixZ(boolean enable){
    if(enable && node != null){
      setMinZ(node.getPosition().z);
      setMaxZ(node.getPosition().z);
    } else {
      setMinZ(null);
      setMaxZ(null);
    }
  }

  public void setMinX(Float min){ axisMinValues[0] = min; if(min != null && node.getPosition().x < min) node.setX(min); }
  public void setMinY(Float min){ axisMinValues[1] = min; if(min != null && node.getPosition().y < min) node.setY(min); }
  public void setMinZ(Float min){ axisMinValues[2] = min; if(min != null && node.getPosition().z < min) node.setZ(min); }

  public void setMaxX(Float max){ axisMaxValues[0] = max; if(max != null && node.getPosition().x > max) node.setX(max); }
  public void setMaxY(Float max){ axisMaxValues[1] = max; if(max != null && node.getPosition().y > max) node.setY(max); }
  public void setMaxZ(Float max){ axisMaxValues[2] = max; if(max != null && node.getPosition().z > max) node.setZ(max); }

  public static Constrain enableFor(Node n){
    return enableFor(n, false);
  }

  public static Constrain enableFor(Node n, boolean onByDefault){
    for(ExtensionBase ext : n.getExtensions())
      if(Constrain.class.isInstance(ext))
        return (Constrain)ext;

    Constrain d = new Constrain();
    n.use(d);

    if(onByDefault){
      d.setFixX(true);
      d.setFixY(true);
      d.setFixZ(true);
    }

    return d;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--)
      if(Constrain.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
  }
}
