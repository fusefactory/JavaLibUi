package com.fuse.ui.extensions;

import com.fuse.ui.Node;


/**
 * The TransformationExtension bass class provides the smoothing logic
 * for extension to perform common -and safe- tranformation operations
 * op the Node's position, scale and rotation attributes.
 */
public class ShrinkToContent extends TransformerExtension {
  public ShrinkToContent(){
    super();
    this.disableSmoothing(); // by default doesn't smooth
  }

  @Override
  public void setup() {
	  // all future children
	  this.node.newChildEvent.addListener((Node newChild) -> {
		  registerChild(newChild);
	  }, this);

    this.node.childRemovedEvent.addListener((Node child) -> {
      unregisterChild(child);
    }, this);

	  // all curent children
	  for(Node child : this.node.getChildNodes()) {
		  registerChild(child);
	  }
  }

  @Override
  public void teardown() {
	  this.node.newChildEvent.removeListeners(this);
    this.node.childRemovedEvent.removeListeners(this);
    for(Node child : this.node.getChildNodes()) {
		  unregisterChild(child);
	  }
  }

  private void registerChild(Node child) {
	  child.positionChangeEvent.addListener((Node n) -> {this.onChildChange(n);}, this);
	  child.scaleChangeEvent.addListener((Node n) -> {this.onChildChange(n);}, this);
	  child.sizeChangeEvent.addListener((Node n) -> {this.onChildChange(n);}, this);
	  this.onChildChange(child);
  }

  private void unregisterChild(Node child) {
    child.positionChangeEvent.removeListeners(this);
    child.scaleChangeEvent.removeListeners(this);
    child.sizeChangeEvent.removeListeners(this);
  }

  private void onChildChange(Node n) {
	  if(!this.node.hasChild(n)) {
		  this.logger.warning("got child change notification from node that is no longer child");
		  return;
	  }

    float maxx = 0.0f;
    float maxy = 0.0f;

    for(Node child : this.node.getChildNodes()){
      maxx = Math.max(maxx, child.getRightScaled());
      maxy = Math.max(maxy, child.getBottomScaled());
    }

    if(this.node.getSize().x > maxx)
      super.transformWidth(maxx);

    if(this.node.getSize().y > maxy)
      super.transformHeight(maxy);
  }

  // static factory methods // // // // //

  public static ShrinkToContent enableFor(Node n){
	  // find existing
    ShrinkToContent ext = getFor(n);
    if(ext != null)
      return ext;

    // create new
    ext = new ShrinkToContent();
    n.use(ext);
    return ext;
  }

  public static ShrinkToContent getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(ShrinkToContent.class.isInstance(ext))
        return (ShrinkToContent)ext;
    return null;
  }

  public static void disableFor(Node n){
    for(ExtensionBase ext : n.getExtensions()) {
      if(ShrinkToContent.class.isInstance(ext))
          n.stopUsing(ext);
    }
  }
}
