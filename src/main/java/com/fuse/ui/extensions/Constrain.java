package com.fuse.ui.extensions;

import com.fuse.ui.Node;

import processing.core.PApplet;
import processing.core.PVector;

public class Constrain extends TransformerExtension {
  private static float scaleIgnoreLimit = 0.001f;
  private static float positionIgnoreLimit = 0.01f;

  // constrain parameter attributes
  private Float[] axisMinValues = {null, null, null}; // position, TODO: rename
  private Float[] axisMaxValues = {null, null, null}; // position, TODO: rename
  private Float[] minScale = {null, null, null}; // x,y,z axisMaxValues
  private Float[] maxScale = {null, null, null}; // x,y,z axisMaxValues
  private boolean bFillParent = false;
  private boolean bCenterWhenFitting = false;
  private boolean bLock = false;

  public Constrain(){
    super();
    super.setOnlyWhenNotTouched(true);
    super.setMaxTransformationTime(10.0f); /// by default Constrain extension is very persistant
  }

  @Override protected void setup(){
    node.positionChangeEvent.whenTriggered(() -> { this.applyConstrains(); }, this);
    node.sizeChangeEvent.whenTriggered(() -> { this.applyConstrains(); }, this);
    //node.touchMoveEvent.whenTriggered(()->{ this.onNodeChange(); }, this);
    //node.touchUpEvent.whenTriggered(()->{ this.onNodeChange(); }, this);
  }

  @Override protected void teardown(){
    node.positionChangeEvent.stopWhenTriggeredCallbacks(this);
    node.sizeChangeEvent.stopWhenTriggeredCallbacks(this);
    //node.touchMoveEvent.stopWhenTriggeredCallbacks(this);
    //node.touchUpEvent.stopWhenTriggeredCallbacks(this);
  }

  @Override public void update(float dt){
    applyConstrains();
    //bLock = true;
    super.update(dt);
  }

  @Override public void transformPosition(PVector vec){
    //bLock = true;
    super.transformPosition(vec);
    bLock = false;
  }

  @Override public void transformRotation(PVector vec){
    //bLock = true;
    super.transformRotation(vec);
    bLock = false;
  }

  @Override public void transformScale(PVector vec){
    //bLock = true;
    super.transformScale(vec);
    bLock = false;
  }

  private PVector getConstrainedPosition(){
    PVector result = node.getPosition();
    PVector targetScale = this.getTargetScale();

    // currently aplying scale constrain? then we'll probably want to apply
    // translation so scaling doesn't warp to top left
    if(targetScale != null){
      PVector curSize = this.node.getSize();
      PVector curscale = this.node.getScale();
      curSize.x = curSize.x * curscale.x;
      curSize.y = curSize.y * curscale.y;
      curSize.z = curSize.z * curscale.z;

      PVector targetSize = this.node.getSize();
      targetSize.x = targetSize.x * targetScale.x;
      targetSize.y = targetSize.y * targetScale.y;
      targetSize.z = targetSize.z * targetScale.z;

      PVector deltaSize = targetSize.get();
      deltaSize.sub(curSize);

      // apply position correction to constrainedPosition result
      deltaSize.mult(0.5f);
      result.sub(deltaSize);
    }

    if(axisMinValues[0] != null && axisMinValues[0] > result.x) result.x = axisMinValues[0];
    if(axisMinValues[1] != null && axisMinValues[1] > result.y) result.y = axisMinValues[1];
    if(axisMinValues[2] != null && axisMinValues[2] > result.z) result.z = axisMinValues[2];

    if(axisMaxValues[0] != null && axisMaxValues[0] < result.x) result.x = axisMaxValues[0];
    if(axisMaxValues[1] != null && axisMaxValues[1] < result.y) result.y = axisMaxValues[1];
    if(axisMaxValues[2] != null && axisMaxValues[2] < result.z) result.z = axisMaxValues[2];

    if(bFillParent){
      Node parentNode = node.getParent();
      if(parentNode != null){
        //PVector sizeScaled = node.getSizeScaled();
        PVector sizeScaled = node.getSize();
        PVector scaler = (targetScale != null ? targetScale : this.node.getScale());
        sizeScaled.x = sizeScaled.x * scaler.x;
        sizeScaled.y = sizeScaled.y * scaler.y;
        sizeScaled.z = sizeScaled.z * scaler.z;

        if(sizeScaled.x >= parentNode.getSize().x){ // can only fill if bigger
          result.x = Math.min(0.0f, Math.max((parentNode.getSize().x-sizeScaled.x), result.x));
        }

        if(sizeScaled.y >= parentNode.getSize().y){
          result.y = Math.min(0.0f, Math.max((parentNode.getSize().y-sizeScaled.y), result.y));
        }
      }
    }

    if(bCenterWhenFitting){
      Node parentNode = node.getParent();
      if(parentNode != null){
          //PVector sizeScaled = node.getSizeScaled();
      	PVector sizeScaled = node.getSize();
      	PVector scaler = (targetScale != null ? targetScale : this.node.getScale());
      	sizeScaled.x = sizeScaled.x * scaler.x;
      	sizeScaled.y = sizeScaled.y * scaler.y;
      	sizeScaled.z = sizeScaled.z * scaler.z;

        if(sizeScaled.x <= parentNode.getSize().x){
          result.x = (parentNode.getSize().x - sizeScaled.x) * 0.5f;
        }

        if(sizeScaled.y <= parentNode.getSize().y){
          result.y = (parentNode.getSize().y - sizeScaled.y) * 0.5f;
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

  private void applyConstrains(){
    if(bLock)
      return;

    PVector vec = this.getConstrainedScale();
    if(vec.dist(node.getScale()) > scaleIgnoreLimit){
      super.transformScale(vec);
    }

    vec = this.getConstrainedPosition();
    if(vec.dist(node.getPosition()) > positionIgnoreLimit){
      super.transformPosition(vec);
    }

    // TODO: rotation constrains
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

  public void setMinX(Float min){ axisMinValues[0] = min; if(min != null && node.getPosition().x < min) applyConstrains(); }
  public void setMinY(Float min){ axisMinValues[1] = min; if(min != null && node.getPosition().y < min) applyConstrains(); }
  public void setMinZ(Float min){ axisMinValues[2] = min; if(min != null && node.getPosition().z < min) applyConstrains(); }

  public void setMaxX(Float max){ axisMaxValues[0] = max; if(max != null && node.getPosition().x > max) applyConstrains(); }
  public void setMaxY(Float max){ axisMaxValues[1] = max; if(max != null && node.getPosition().y > max) applyConstrains(); }
  public void setMaxZ(Float max){ axisMaxValues[2] = max; if(max != null && node.getPosition().z > max) applyConstrains(); }

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

  @Override
  public void setFillParent(boolean enable){
    bFillParent = enable;
    applyConstrains();
  }

  public void setCenterWhenFitting(boolean enable){
    bCenterWhenFitting = enable;
  }

  public boolean getCenterWhenFitting(){
    return bCenterWhenFitting;
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
	for(ExtensionBase ext : n.getExtensions()) {
		if(Constrain.class.isInstance(ext))
				n.stopUsing(ext);
	}
  }
}
