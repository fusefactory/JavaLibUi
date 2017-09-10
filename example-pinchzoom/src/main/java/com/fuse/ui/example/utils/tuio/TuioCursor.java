package com.fuse.utils.tuio;

public class TuioCursor extends TuioPoint
{
	private int cursorID;
	
	// private constructor, only used in our copy method
	private TuioCursor(){		
	}

	public TuioCursor(long creatingTime, int sessionID, int cursorID, float positionX, float positionY, float velocityX, float velocityY, float motionAcceleration)
	{
		super(creatingTime, sessionID, positionX, positionY, velocityX, velocityY, motionAcceleration);
		this.cursorID = cursorID;
	}
	
	public TuioCursor(long creatingTime, int sessionID, float positionX, float positionY, float velocityX, float velocityY, float motionAcceleration)
	{
		this(creatingTime, sessionID, -1, positionX, positionY, velocityX, velocityY, motionAcceleration);
	}
	
	/**
	 * L'ID assegnato ad ogni TuioCursor all'interno della sessione.
	 */
	public int cursorID()
	{
		return cursorID;
	}
	
	// returns a new instance which is a copy of `this`
	public TuioCursor copy(){
		TuioCursor c = new TuioCursor();
		c.copy(this, true);
		return c;
	}

	// copies the values of the given instance into itself
	protected void copy(TuioCursor other, boolean recursive){
		super.copy((TuioPoint)other, recursive);
		cursorID = other.cursorID();
	}
}
