package com.fuse.ui.extensions;

import processing.core.PVector;
import processing.core.PGraphics;

import com.fuse.ui.Node;


/**
 * The TransformationExtension bass class provides the smoothing logic
 * for extension to perform common -and safe- tranformation operations
 * op the Node's position, scale and rotation attributes.
 */
public class GrowWithContent extends TransformerExtension {
  public GrowWithContent(){
    super();
    this.disableSmoothing(); // by default doesn't smooth
  }

  @Override
  public void enable() {
	  // all future children
	  this.node.newChildEvent.addListener((Node newChild) -> {
		  registerChild(newChild);
	  }, this);
	  
	  // all curent children
	  for(Node child : this.node.getChildNodes()) {
		  registerChild(child);
	  }
  }

  @Override
  public void disable() {
	  this.node.newChildEvent.removeListeners(this);
	  this.logger.warning("TODO: GrowWithContent node extension should unregister listeners from all children");
  }

  private void registerChild(Node child) {
	  child.positionChangeEvent.addListener((Node n) -> {this.onChildChange(n);}, this);
	  child.scaleChangeEvent.addListener((Node n) -> {this.onChildChange(n);}, this);
	  child.sizeChangeEvent.addListener((Node n) -> {this.onChildChange(n);}, this);
	  this.onChildChange(child);
  }

  private void onChildChange(Node n) {
	  if(!this.node.hasChild(n)) {
		  this.logger.warning("got child change notification from node that is no longer child");
		  return;
	  }
	  
	  float fl = n.getRightScaled();
	  if(fl > this.node.getSize().x) {
		  this.transformWidth(fl);
	  }
	  
	  fl = n.getBottomScaled();
	  if(fl > this.node.getSize().y) {
		  this.transformHeight(fl);
	  }
  }

  // static factory methods // // // // //

  public static GrowWithContent enableFor(Node n){
	// find existing
    for(ExtensionBase ext : n.getExtensions())
      if(GrowWithContent.class.isInstance(ext))
        return (GrowWithContent)ext;

    // create new
    GrowWithContent d = new GrowWithContent();
    n.use(d);
    return d;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--){
      if(GrowWithContent.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
    }
  }
}
