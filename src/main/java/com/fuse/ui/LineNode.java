package com.fuse.ui;

import processing.core.PVector;

public class LineNode extends Node {

  private PVector[] coordinates;
  private int lineColor;
  private float strokeWeight = 1;

  /** Default constructor; intialized with default values: image=null and mode=NORMAL */
  public LineNode(){
    coordinates = new PVector[2];
    coordinates[0] = new PVector(0.0f, 0.0f, 0.0f);
    coordinates[1] = new PVector(1.0f, 1.0f, 0.0f);
    lineColor = pg.color(255);
  }

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    pg.stroke(lineColor);
    pg.strokeWeight(strokeWeight);
    pg.beginShape(pg.LINES);

    PVector from = coordinates[0];
    for(int i=1; i<coordinates.length; i++){
      PVector to = coordinates[i];
      pg.vertex(from.x,from.y,from.z);
      pg.vertex(to.x,to.y,to.z);
      from = to;
    }
    pg.endShape();
  }

  public void setCoordinates(PVector[] newCoords){
    coordinates = new PVector[newCoords.length];
    for(int i=0; i<coordinates.length; i++){
      coordinates[i] = newCoords[i];
    }
  }

  public PVector[] getCoordinates(){
    PVector[] coords = new PVector[coordinates.length];

    for(int i=0; i<coordinates.length; i++){
      coords[i] = coordinates[i];
    }

    return coords;
  }

  public void setFrom(PVector newFrom){ coordinates[0] = newFrom; }
  public PVector getFrom(){ return coordinates[0]; }
  public void setTo(PVector newTo){ coordinates[1] = newTo; }
  public PVector getTo(){ return coordinates[1]; }

  public void setLineColor(int clr){ lineColor = clr; }
  public int getLineColor(){ return lineColor; }
  public void setStrokeWeight(float weight){ strokeWeight = weight; }
  public float getStrokeWeight(){ return strokeWeight; }
}
