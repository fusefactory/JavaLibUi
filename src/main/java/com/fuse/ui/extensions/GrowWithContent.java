package com.fuse.ui.extensions;

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

  @Override protected void setup() {
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

  @Override protected void teardown() {
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

    // TODO; calculate Node's bounding box to also consider rotational factors

	  float fl = n.getRightScaled();
	  if(fl > this.node.getSize().x) {
      // use TransformerExtension's smoothing options (disabled by default)
		  super.transformWidth(fl);
	  }

	  fl = n.getBottomScaled();
	  if(fl > this.node.getSize().y) {
      // use TransformerExtension's smoothing options (disabled by default)
		  super.transformHeight(fl);
	  }
  }

  // static factory methods // // // // //

  public static GrowWithContent enableFor(Node n){
	  // find existing
    GrowWithContent ext = getFor(n);
    if(ext != null)
      return ext;

    // create new
    ext = new GrowWithContent();
    n.use(ext);
    return ext;
  }

  public static GrowWithContent getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(GrowWithContent.class.isInstance(ext))
        return (GrowWithContent)ext;
    return null;
  }

  public static void disableFor(Node n){
    for(ExtensionBase ext : n.getExtensions()) {
      if(GrowWithContent.class.isInstance(ext))
          n.stopUsing(ext);
    }
  }
}
