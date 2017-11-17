package com.fuse.ui.example.utils.tuio;

public class TuioPoint
{
	private int sessionID;
	private float positionX, positionY;
	private float velocityX, velocityY;
	private float motionAcceleration;
	private TuioPoint lastPoint, firstPoint;
	private long updatingTime;

	// protected constructor; only used in our copy method
	protected TuioPoint(){
	}

	public TuioPoint(long creatingTime, int sessionID, float positionX, float positionY, float velocityX, float velocityY, float motionAcceleration)
	{
		this.sessionID = sessionID;
		set(positionX, positionY, velocityX, velocityY, motionAcceleration);
		this.updatingTime = creatingTime;
		this.firstPoint = this.lastPoint = null;
	}

	public void update(long updatingTime, float positionX, float positionY, float velocityX, float velocityY, float motionAcceleration)
	{
		lastPoint = copy(false);

		if(firstPoint == null)
			firstPoint = lastPoint;

		set(positionX, positionY, velocityX, velocityY, motionAcceleration);
		this.updatingTime = updatingTime;
	}

	// returns a new instance which is a copy of `this`
	protected TuioPoint copy(){
		return copy(true);
	}

	protected TuioPoint copy(boolean recursive){
		TuioPoint c = new TuioPoint();
		c.copy(this, recursive);
		return c;
	}

	// copies the values of the given instance into itself
	protected void copy(TuioPoint other, boolean recursive){
		this.updatingTime = other.updatingTime();
		this.sessionID = other.sessionID();
		this.positionX = other.positionX();
		this.positionY = other.positionY();
		this.velocityX = other.velocityX();
		this.velocityY = other.velocityY();
		this.motionAcceleration = other.motionAcceleration();

		if(recursive){
			TuioPoint o = other.getFirstPoint();
			this.firstPoint = (o == null ? null : o.copy());
			o = other.lastPoint();
			this.lastPoint = (o == null ? null : o.copy());
		}
	}

	private void set(float positionX, float positionY, float velocityX, float velocityY, float motionAcceleration)
	{
		this.positionX = positionX;
		this.positionY = positionY;
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.motionAcceleration = motionAcceleration;
	}

	public TuioPoint getFirstPoint(){ return firstPoint; }
	protected void setFirstPoint(TuioPoint p){ firstPoint = p; }

	/**
	 * L'ID univoco assegnato ad ogni TuioPoint all'interno della sessione.
	 */
	public int sessionID()
	{
		return sessionID;
	}

	/**
	 * Indica la posizione normalizzata (0-1) lungo l'asse X.
	 */
	public float positionX()
	{
		return positionX;
	}

	public void positionX(float newX)
	{
		positionX = newX;
	}

	/**
	 * Indica la posizione normalizzata (0-1) lungo l'asse Y.
	 */
	public float positionY()
	{
		return positionY;
	}

	public void positionY(float newY)
	{
		positionY = newY;
	}

	/**
	 * Indica la posizione lungo l'asse X rispetto alla larghezza dello schermo.
	 * @param screenWidth La larghezza dello schermo.
	 */
	public float screenX(float screenWidth)
	{
		return positionX * screenWidth;
	}

	/**
	 * Indica la posizione lungo l'asse Y rispetto all'altezza dello schermo.
	 * @param screenHeight L'altezza dello schermo.
	 */
	public float screenY(float screenHeight)
	{
		return positionY * screenHeight;
	}

	/**
	 * Returns amount of pixels moved horizontally since last update
	 */
	public float deltaScreenX(float screenWidth){
		return screenX(screenWidth) - lastPoint.screenX(screenWidth);
	}

	/**
	 * Returns amount of pixels moved vertically since last update
	 */
	public float deltaScreenY(float screenHeight){
		return screenY(screenHeight) - lastPoint.screenY(screenHeight);
	}

	/**
	 * Returns amount of pixels moved horizontally since first touch
	 */
	public float totalDeltaScreenX(float screenWidth){
		if(firstPoint == null)
			return 0;
		return screenX(screenWidth) - firstPoint.screenX(screenWidth);
	}

	/**
	 * Returns amount of pixels moved vertically since first touch
	 */
	public float totalDeltaScreenY(float screenHeight){
		if(firstPoint == null)
			return 0;
		return screenY(screenHeight) - firstPoint.screenY(screenHeight);
	}

	/**
	 * Indica la velocit� lungo l'asse X.
	 */
	public float velocityX()
	{
		return velocityX;
	}

	public void velocityX(float newX)
	{
		velocityX = newX;
	}

	/**
	 * Indica la velocit� lungo l'asse Y.
	 */
	public float velocityY()
	{
		return velocityY;
	}

	public void velocityY(float newY)
	{
		velocityY = newY;
	}


	/**
	 * Indica il valore dell'accelerazione di movimento.
	 */
	public float motionAcceleration()
	{
		return motionAcceleration;
	}

	/**
	 * Restituisce l'ultimo TuioPoint.
	 */
	public TuioPoint lastPoint()
	{
		return lastPoint;
	}

	/**
	 * Indica il tempo di creazione. Espresso in millisecondi.
	 */
	public long creatingTime()
	{
		// we're the first? our updatng time, else the first point's updating time
		return (firstPoint == null ? updatingTime : firstPoint.updatingTime());
	}

	/**
	 * Indica il tempo di aggiornamento. Espresso in millisecondi.
	 */
	public long updatingTime()
	{
		return updatingTime;
	}

	/**
	 * Often a caller will want to know the time between
	 * the first touch and the current update, we'll provide
	 * this convenience method
	 */
	public long deltaTime()
	{
		return updatingTime - lastPoint.updatingTime();
	}

	public long touchTime()
	{
		return updatingTime - creatingTime();
	}

	/**
	 * Restituisce la distanza normalizzata (0-1) considerando le coordinate passate come parametro.
	 * @param x La coordinata rispetto all'asse X normalizzata (0-1) da cui calcolare la distanza.
	 * @param y La coordinata rispetto all'asse Y normalizzata (0-1) da cui calcolare la distanza.
	 */
	public float getDistance(float x, float y)
	{
		float dx = positionX - x;
		float dy = positionY - y;
		return (float)Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Restituisce la distanza normalizzata (0-1) con il TuioPoint desiderato.
	 * @param point Il TuioPoint da cui calcolare la distanza.
	 */
	public float getDistance(TuioPoint point)
	{
		return getDistance(point.positionX(), point.positionY());
	}

	/**
	 * Restituisce l'angolo rispetto alle coordinate passate, espresso in radianti.
	 * @param x La coordinata rispetto all'asse X normalizzata (0-1) di cui calcolare l'angolo.
	 * @param y La coordinata rispetto all'asse Y normalizzata (0-1) di cui calcolare l'angolo.
	 */
	public float getAngle(float x, float y)
	{
		float side = positionX - x;
		float rise = positionY - y;
		float distance = getDistance(x, y);

		float angle = (float)(Math.asin(side / distance) + Math.PI/2);
		if (rise < 0) angle = 2.0f * (float)Math.PI - angle;
		return angle;
	}

	/**
	 * Restituisce l'angolo rispetto al TuioPoint desiderato, espresso in radianti.
	 * @param point Il TuioPoint rispetto cui calcolare l'angolo.
	 */
	public float getAngle(TuioPoint point)
	{
		return getAngle(point.positionX(), point.positionY());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj.getClass() == this.getClass())
		{
			TuioPoint other = (TuioPoint)obj;
			if (this.sessionID() == other.sessionID()) return true;
		}
		return false;
	}
}
