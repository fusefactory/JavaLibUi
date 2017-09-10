package com.fuse.ui.extensions;

import processing.core.PApplet;
import processing.core.PVector;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Constrain extends TransformerExtension {
  // constrain parameter attributes
  private Float[] axisMinValues = {null, null, null}; // position, TODO: rename
  private Float[] axisMaxValues = {null, null, null}; // position, TODO: rename
  private Float[] minScale = {null, null, null}; // x,y,z axisMaxValues
  private Float[] maxScale = {null, null, null}; // x,y,z axisMaxValues
  private boolean bFillParent = false;
  private boolean bLock = false;

  public Constrain(){
    super();
    super.setMaxTransformationTime(10.0f); /// by default Constrain extension is very persistant
  }

  @Override public void destroy(){
    super.destroy();
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

  @Override public void update(float dt){
    bLock = true;
    super.update(dt);
    bLock = false;
  }

  @Override protected void transformPosition(PVector vec){
    bLock = true;
    super.transformPosition(vec);
    bLock = false;
  }

  @Override protected void transformRotation(PVector vec){
    bLock = true;
    super.transformRotation(vec);
    bLock = false;
  }

  @Override protected void transformScale(PVector vec){
    bLock = true;
    super.transformScale(vec);
    bLock = false;
  }

  private PVector getConstrainedPosition(){
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

  private PVector getConstrainedScale(){
    PVector result = node.getScale().get();

    if(minScale[0] != null && minScale[0] > result.x) result.x = minScale[0];
    if(minScale[1] != null && minScale[1] > result.y) result.y = minScale[1];
    if(minScale[2] != null && minScale[2] > result.z) result.z = minScale[2];

    if(maxScale[0] != null && maxScale[0] < result.x) result.x = maxScale[0];
    if(maxScale[1] != null && maxScale[1] < result.y) result.y = maxScale[1];
    if(maxScale[2] != null && maxScale[2] < result.z) result.z = maxScale[2];

    return result;
  }

  private void onNodeChange(){
    if(bLock)
      return;

    PVector vec = this.getConstrainedScale();
    if(vec.dist(node.getScale()) > 0.01f){ // negligable
      super.transformScale(vec);
    }

    vec = this.getConstrainedPosition();
    // logger.info("Constrained pos:"+pos.toString()+", cur pos: "+node.getPosition());
    if(vec.dist(node.getPosition()) > 0.1f){ // negligable
      // logger.info("constrain transforming to: "+pos.toString());
      super.transformPosition(vec);
    }
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

  public void setMinX(Float min){ axisMinValues[0] = min; if(min != null && node.getPosition().x < min) onNodeChange(); }
  public void setMinY(Float min){ axisMinValues[1] = min; if(min != null && node.getPosition().y < min) onNodeChange(); }
  public void setMinZ(Float min){ axisMinValues[2] = min; if(min != null && node.getPosition().z < min) onNodeChange(); }

  public void setMaxX(Float max){ axisMaxValues[0] = max; if(max != null && node.getPosition().x > max) onNodeChange(); }
  public void setMaxY(Float max){ axisMaxValues[1] = max; if(max != null && node.getPosition().y > max) onNodeChange(); }
  public void setMaxZ(Float max){ axisMaxValues[2] = max; if(max != null && node.getPosition().z > max) onNodeChange(); }

  @Override
  public void setMinScale(Float value){
    this.minScale[0] = value;
    this.minScale[1] = value;
    this.minScale[2] = value;
  }

  @Override
  public void setMaxScale(Float value){
    this.maxScale[0] = value;
    this.maxScale[1] = value;
    this.maxScale[2] = value;
  }

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

  public void setFillParent(boolean enable){
    bFillParent = true;
    onNodeChange();
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
