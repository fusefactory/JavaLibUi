package com.fuse.ui.extensions;

import processing.core.PVector;
import processing.core.PGraphics;

import com.fuse.ui.Node;


/**
 * The TransformationExtension bass class provides the smoothing logic
 * for extension to perform common -and safe- tranformation operations
 * op the Node's position, scale and rotation attributes.
 */
public class TransformerExtension extends ExtensionBase {
  private static float doneScaleDeltaMag = 0.01f;
  private static float donePositionDeltaMag = 0.1f;
  private static float doneRotationDeltaMag = 0.1f;
  // smoothing
  private PVector targetPosition, targetRotation, targetScale;
  private float smoothValue = 7.0f;
  private Float smoothValueScale = null; // when null, smoothValue is used for scaling as well
  // time-based transformation expiration
  private Float maxTransformationTime = 3.0f;
  private float positionTimer;
  private float scaleTimer;
  // limits
  private Float[] minScale = {null, null, null}; // x,y,z axis
  private Float[] maxScale = {null, null, null}; // x,y,z axis
  private boolean bFillParent = false; // only if bigger than parent
  // touch-related
  private boolean bOnlyWhenNotTouched = false;
  private boolean bStopOnTouch = false;
  // endless recursion detection
  private static int maxTransformationsPerUpdate = 9;
  private int transformationsThisUpdate = 0;

  // lifecycle methods // // // // //

  @Override public void disable(){
    super.disable();
    targetPosition = null;
    targetScale = null;
    targetRotation = null;
  }

  @Override public void update(float dt){
    transformationsThisUpdate = 0; // reset endless recursion detection counter

    if(this.node.isTouched()){
      if(this.bStopOnTouch){
        this.stopActiveTransformations();
        return;
      }

      if(bOnlyWhenNotTouched)
        return;
    }

    if(targetPosition != null){
      this.positionTimer += dt;
      if( this.maxTransformationTime != null && this.positionTimer > this.maxTransformationTime){
        logger.fine("position transformation EXPIRED");
        this.targetPosition = null;
      } else if(smoothValue <= 1.0f){
        // smoothing disabled, apply directly
        this.node.setPosition(targetPosition);
        targetPosition = null;
      } else {
        PVector vec = targetPosition.get();
        // logger.info("TransformExtension update pos smoothing to: "+vec.toString());
        // delta
        vec.sub(this.node.getPosition());
        // smoothed delta
        vec.mult(1.0f / this.smoothValue);

        if(vec.mag() < donePositionDeltaMag){
          // finalize
          this.node.setPosition(targetPosition);
          //logger.info("smoothed position DONE");
          targetPosition = null;
        } else {
          // apply delta to current node value
          vec.add(this.node.getPosition());
          //logger.info("TransformExtension applying smoothed pos: "+vec.toString());
          // apply update to node
          this.node.setPosition(vec);
          //logger.info("smoothed position:"+vec.toString());
        }
      }
    }

    if(targetRotation != null){
      if(smoothValue <= 1.0f){
        // smoothing disabled, apply directly
        this.node.setRotation(targetRotation);
        targetRotation = null;
      } else {
        PVector vec = targetRotation.get();
        // delta
        vec.sub(this.node.getRotation());
        // smoothed delta
        vec.mult(1.0f / this.smoothValue);

        if(vec.mag() < doneRotationDeltaMag){
          // finalize
          this.node.setRotation(targetRotation);
          targetRotation = null;
        } else {
          // apply delta to current node value
          vec.add(this.node.getRotation());
          // apply update to node
          this.node.setRotation(vec);
        }
      }
    }

    if(targetScale != null){
      float smoother = this.getSmoothValueForScaling();

      this.scaleTimer += dt;
      if( this.maxTransformationTime != null && this.scaleTimer > this.maxTransformationTime){
        logger.fine("scale transformation expired");
        this.targetScale = null;
      } else if(smoother <= 1.0f){
        // smoothing disabled, apply directly
        this.node.setScale(targetScale);
        this.targetScale = null;
      } else {
        PVector vec = targetScale.get();
        // delta
        vec.sub(this.node.getScale());
        // smoothed delta
        vec.mult(1.0f / smoother);

        if(vec.mag() < doneScaleDeltaMag){
          // finalize
          this.node.setScale(targetScale);
          targetScale = null;
          // logger.fine("scale transformation FINISHED");
        } else {
          // apply delta to current node value
          vec.add(this.node.getScale());
          // apply update to node
          this.node.setScale(vec);
        }
      }
    }
  }

  @Override public void drawDebug(){
    PGraphics pg = Node.getPGraphics();
    pg.colorMode(pg.RGB, 255);
    pg.stroke(pg.color(255,100,255,100));
    pg.strokeWeight(2.0f);

    if(this.targetPosition != null){
      PVector vec = this.node.parentToLocalSpace(targetPosition);
      pg.line(0.0f, 0.0f, vec.x, vec.y);
    }
  }

  // operation methods // // // // //

  public void stopActiveTransformations(){
    targetPosition = null;
    targetRotation = null;
    targetScale = null;
  }

  public void stopPositionTransformation(){
    targetPosition = null;
  }

  public PVector getPositionTransformationVelocity(){
    if(this.targetPosition == null)
      return new PVector(0,0,0);

    PVector vec = this.targetPosition.get();
    // delta
    vec.sub(this.node.getPosition());
    // smoothed delta
    vec.mult(1.0f / this.smoothValue);

    return vec;
  }

  protected void transformPosition(PVector vec){
    if(bOnlyWhenNotTouched && this.node.isTouched())
      return;

    vec = this.limitedPosition(vec);

    if(this.isSmoothing()){
      this.targetPosition = vec.get();
      this.positionTimer = 0.0f;
      return; // let the update method take it from here
    }

    // not smoothing, apply immediately
    if(this.endlessRecursionDetected()){
      this.logger.warning("TransformerExtension for Node: '"+this.node.getName()+"' detected endless recursion");
      return;
    }

    // apply position directly, unsmoothed
    this.node.setPosition(vec);
    this.transformationsThisUpdate++;
  }

  protected void transformPositionGlobal(PVector vec){
    Node parentNode = this.node.getParent();
    PVector localized;
    if(parentNode == null) {
    	localized = vec.get();
    } else {
    	localized = parentNode.toLocal(vec);
    }
    // logger.info("transform ext: global: "+vec.toString()+" to "+localized.toString());
    this.transformPosition(localized);
  }

  protected void transformRotation(PVector vec){
    if(bOnlyWhenNotTouched && this.node.isTouched())
      return;

    if(this.isSmoothing()){
      this.targetRotation = vec.get();
      return; // let the update method take it from here
    }

    // not smoothing, apply immediately
    if(this.endlessRecursionDetected()){
      this.logger.warning("TransformerExtension for Node: '"+this.node.getName()+"' detected endless recursion");
      return;
    }

    // apply position directly, unsmoothed
    this.node.setRotation(vec);
    this.transformationsThisUpdate++;
  }

  protected void transformScale(PVector vec){
    if(bOnlyWhenNotTouched && this.node.isTouched())
      return;

    vec = this.limitedScale(vec);

    if(this.isSmoothingScale()){
      this.targetScale = vec.get();
      scaleTimer = 0.0f;
      return; // let the update method take it from here
    }

    // not smoothing, apply immediately
    if(this.endlessRecursionDetected()){
      this.logger.warning("TransformerExtension for Node: '"+this.node.getName()+"' detected endless recursion");
      return;
    }

    // apply position directly, unsmoothed
    this.node.setScale(vec);
    this.transformationsThisUpdate++;
  }


  protected PVector limitedPosition(PVector vec){
    PVector result = vec.get();
    Node parentNode = this.node.getParent();

    if(bFillParent && parentNode != null){
      PVector sizeScaled = this.node.getSizeScaled();

      if(sizeScaled.x > parentNode.getSize().x){ // can only fill if bigger
        result.x = Math.min(0.0f, Math.max(-sizeScaled.x, result.x));
      }

      if(sizeScaled.y > parentNode.getSize().y){
        result.y = Math.min(0.0f, Math.max(-sizeScaled.y, result.y));
      }
    }

    return result;
  }

  private PVector limitedScale(PVector vec){
    PVector result = vec.get();

    if(minScale[0] != null && minScale[0] > result.x) result.x = minScale[0];
    if(minScale[1] != null && minScale[1] > result.y) result.y = minScale[1];
    if(minScale[2] != null && minScale[2] > result.z) result.z = minScale[2];

    if(maxScale[0] != null && maxScale[0] < result.x) result.x = maxScale[0];
    if(maxScale[1] != null && maxScale[1] < result.y) result.y = maxScale[1];
    if(maxScale[2] != null && maxScale[2] < result.z) result.z = maxScale[2];

    return result;
  }

  // state reader methods // // // // //

  /** @return boolean indicating if this extension is applying smoothing to all transformations */
  public boolean isSmoothing(){
    return smoothValue > 1.0f;
  }

  public boolean isSmoothingScale(){
    return this.getSmoothValueForScaling() > 1.0f;
  }

  private boolean endlessRecursionDetected(){
    return transformationsThisUpdate >= maxTransformationsPerUpdate;
  }

  public boolean isTransformingPosition(){
    return this.targetPosition != null;
  }

  // configuration methods // // // // //

  public float getSmoothValue(){
    return smoothValue;
  }

  public void setSmoothValue(float val){
    this.smoothValue = val; // <= 1.0f mean disable smoothing
  }

  public Float getSmoothValueScale(){
    return smoothValueScale;
  }

  public void setSmoothValueScale(Float val){
    this.smoothValueScale = val; // <= 1.0f mean disable smoothing
  }

  protected float getSmoothValueForScaling(){
    return this.smoothValueScale == null ? this.smoothValue : this.smoothValueScale;
  }

  public void disableSmoothing(){
    setSmoothValue(0.0f);
  }

  public void setMaxTransformationTime(Float time){
    maxTransformationTime = time;
  }

  public Float getMaxTransformationTime(){
    return maxTransformationTime;
  }

  public void setMinScale(Float value){
    this.minScale[0] = value;
    this.minScale[1] = value;
    this.minScale[2] = value;
  }

  public void setMaxScale(Float value){
    this.maxScale[0] = value;
    this.maxScale[1] = value;
    this.maxScale[2] = value;
  }

  public void setFillParent(boolean enable){
    bFillParent = enable;
  }

  public boolean getFillParent(){
    return bFillParent;
  }

  public void setOnlyWhenNotTouched(boolean enable){
    bOnlyWhenNotTouched = enable;
  }

  public boolean getOnlyWhenNotTouched(){
    return bOnlyWhenNotTouched;
  }

  public void setStopOnTouch(boolean enable){
    bStopOnTouch = enable;
  }

  public boolean getStopOnTouch(){
    return bStopOnTouch;
  }
}
