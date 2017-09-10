package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

/**
 * The TransformationExtension bass class provides the smoothing logic
 * for extension to perform common -and safe- tranformation operations
 * op the Node's position, scale and rotation attributes.
 */
public class TransformerExtension extends ExtensionBase {
  private static float doneScaleDeltaMag = 0.1f;
  private static float donePositionDeltaMag = 0.1f;
  private static float doneRotationDeltaMag = 0.1f;
  // smoothing
  private PVector targetPosition, targetRotation, targetScale;
  private float smoothValue = 7.0f;
  // time-based transformation expiration
  private Float maxTransformationTime = 3.0f;
  private float positionTimer;
  private float scaleTimer;

  // endless recursion detection
  private static int maxTransformationsPerUpdate = 9;
  private int transformationsThisUpdate = 0;

  @Override public void disable(){
    super.disable();
    targetPosition = null;
    targetScale = null;
    targetRotation = null;
  }

  @Override public void update(float dt){
    transformationsThisUpdate = 0; // reset endless recursion detection counter

    if(targetPosition != null){
      this.positionTimer += dt;
      if( this.maxTransformationTime != null && this.positionTimer > this.maxTransformationTime){
        logger.fine("position transformation expired");
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
          targetPosition = null;
        } else {
          // apply delta to current node value
          vec.add(this.node.getPosition());
          //logger.info("TransformExtension applying smoothed pos: "+vec.toString());
          // apply update to node
          this.node.setPosition(vec);
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
      this.scaleTimer += dt;
      if( this.maxTransformationTime != null && this.scaleTimer > this.maxTransformationTime){
        logger.fine("scale transformation expired");
        this.targetScale = null;
      } else if(smoothValue <= 1.0f){
        // smoothing disabled, apply directly
        this.node.setScale(targetScale);
        targetScale = null;
      } else {
        PVector vec = targetScale.get();
        // delta
        vec.sub(this.node.getScale());
        // smoothed delta
        vec.mult(1.0f / this.smoothValue);

        if(vec.mag() < doneScaleDeltaMag){
          // finalize
          this.node.setScale(targetScale);
          targetScale = null;
        } else {
          // apply delta to current node value
          vec.add(this.node.getScale());
          // apply update to node
          this.node.setScale(vec);
        }
      }
    }
  }

  protected void transformPosition(PVector vec){
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
    PVector localized = (parentNode == null) ? vec.get() : parentNode.toLocal(vec);
    this.transformPosition(localized);
  }

  protected void transformRotation(PVector vec){
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
    if(this.isSmoothing()){
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

  // state reader methods // // // // //

  /** @return boolean indicating if this extension is applying smoothing to all transformations */
  public boolean isSmoothing(){
    return smoothValue > 1.0f;
  }

  private boolean endlessRecursionDetected(){
    return transformationsThisUpdate >= maxTransformationsPerUpdate;
  }

  // configuration methods // // // // //

  public float getSmoothValue(){
    return smoothValue;
  }

  public void setSmoothValue(float val){
    this.smoothValue = val; // <= 1.0f mean disable smoothing
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
}
