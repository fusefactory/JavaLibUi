package com.fuse.ui.extensions;

import processing.core.PApplet;
import processing.core.PVector;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Constrain extends ExtensionBase {
  // constrain parameters
  private Float[] axisMinValues = {null, null, null};
  private Float[] axisMaxValues = {null, null, null};
  private boolean bFillParent = false;
  // avoiding inifinite loops
  private static int maxPositionCorrectionPerUpdate = 3;
  private int positionCorrectionsThisUpdate = 0;
  // smoothing
  private Float smoothing = null;
  private PVector targetPos = null;

  public Constrain(){
    setFixX(true);
    setFixY(true);
    setFixZ(true);
  }

  @Override public void destroy(){
    super.destroy();
  }

  private PVector getConstrainedPosition(){
    positionCorrectionsThisUpdate = 0;

    PVector result = node.getPosition();

    if(axisMinValues[0] != null && axisMinValues[0] > result.x) result.x = axisMinValues[0];
    if(axisMinValues[1] != null && axisMinValues[1] > result.y) result.y = axisMinValues[1];
    if(axisMinValues[2] != null && axisMinValues[2] > result.z) result.z = axisMinValues[2];

    if(axisMaxValues[0] != null && axisMaxValues[0] < result.x) result.x = axisMaxValues[0];
    if(axisMaxValues[1] != null && axisMaxValues[1] < result.y) result.y = axisMaxValues[1];
    if(axisMaxValues[2] != null && axisMaxValues[2] < result.z) result.z = axisMaxValues[2];

    if(bFillParent){
      Node parent = node.getParent();
      if(parent != null){
        // TODO; consider node's scaling property?

        PVector nodeSize = node.getSize();
        PVector parentSize = parent.getSize();

        if(nodeSize.x >= parentSize.x){
          if(result.x > 0.0f) result.x = 0.0f;
          else if(node.getRightScaled() < parent.getSize().x) result.x = parent.getSize().x - node.getSize().x * node.getScale().x;
        }

        if(nodeSize.y >= parentSize.y){
          if(result.y > 0.0f) result.y = 0.0f;
          else if(node.getBottomScaled() < parent.getSize().y) result.y = parent.getSize().y - node.getSize().y * node.getScale().y;
        }
      }
    }

    return result;
  }

  @Override
  public void update(float dt){
    positionCorrectionsThisUpdate = 0; // reset

    if(targetPos != null){
      PVector delta = targetPos.get();
      delta.sub(node.getPosition());
      delta.mult(1.0f/smoothing);
      if(delta.mag() < 0.1f){
        node.setPosition(targetPos);
        targetPos = null;
      } else {
        delta.add(node.getPosition());
        node.setPosition(delta);
      }
    }
  }

  private void onNodeChange(){
    PVector pos = this.getConstrainedPosition();

    if(pos.dist(node.getPosition()) < 0.1f) // negligable
      return;

    if(smoothing == null && positionCorrectionsThisUpdate < maxPositionCorrectionPerUpdate){
      positionCorrectionsThisUpdate++;
      node.setPosition(pos);
    } else {
      targetPos = pos;
    }
  }

  @Override public void enable(){
    super.enable();

    node.positionChangeEvent.whenTriggered(() -> { this.onNodeChange(); }, this);
    node.sizeChangeEvent.whenTriggered(() -> { this.onNodeChange(); }, this);
  }

  @Override public void disable(){
    super.disable();
    node.positionChangeEvent.stopWhenTriggeredCallbacks(this);
    node.sizeChangeEvent.stopWhenTriggeredCallbacks(this);
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

  public Float getPercentageX(){
    if(axisMinValues[0] == null || axisMaxValues[0] == null)
      return null;

    if(axisMinValues[0].equals(axisMaxValues[0]))
      return 1.0f;

    return PApplet.map(node.getPosition().x, axisMinValues[0], axisMaxValues[0], 0.0f, 1.0f);
  }

  public Float getPercentageY(){
    if(axisMinValues[1] == null || axisMaxValues[1] == null)
      return null;

    if(axisMinValues[1].equals(axisMaxValues[1]))
      return 1.0f;

    return PApplet.map(node.getPosition().y, axisMinValues[1], axisMaxValues[1], 0.0f, 1.0f);
  }

  public Float getPercentageZ(){
    if(axisMinValues[2] == null || axisMaxValues[2] == null)
      return null;

    if(axisMinValues[2].equals(axisMaxValues[2]))
      return 1.0f;

    return PApplet.map(node.getPosition().z, axisMinValues[2], axisMaxValues[2], 0.0f, 1.0f);
  }

  public void setPercentageX(float percentage){
    if(axisMinValues[0] != null && axisMaxValues[0] != null)
      node.setX(PApplet.lerp(axisMinValues[0], axisMaxValues[0], percentage));
  }

  public void setPercentageY(float percentage){
    if(axisMinValues[1] != null && axisMaxValues[1] != null)
      node.setY(PApplet.lerp(axisMinValues[1], axisMaxValues[1], percentage));
  }

  public void setPercentageZ(float percentage){
    if(axisMinValues[2] != null && axisMaxValues[2] != null)
      node.setZ(PApplet.lerp(axisMinValues[2], axisMaxValues[2], percentage));
  }

  public void setFillParent(boolean enable){
    bFillParent = true;
  }

  public static Constrain enableFor(Node n){
    return enableFor(n, false);
  }

  public static Constrain enableFor(Node n, boolean onByDefault){
    Constrain d = getFor(n);

    if(d == null){
      d = new Constrain();
      n.use(d);
    }


    d.setFixX(onByDefault);
    d.setFixY(onByDefault);
    d.setFixZ(onByDefault);
    return d;
  }

  public static Constrain getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(Constrain.class.isInstance(ext))
        return (Constrain)ext;
    return null;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--)
      if(Constrain.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
  }
}
