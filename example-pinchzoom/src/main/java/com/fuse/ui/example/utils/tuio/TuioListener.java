package com.fuse.ui.example.utils.tuio;


public interface TuioListener
{
	/**
	 * Si verifica quando viene aggiunto un nuovo TuioCursor alla sessione.
	 */
	void addedCursor(TuioCursor cursor);
	/**
	 * Si verifica quando viene aggiornato un TuioCursor presente nella sessione.
	 */
	void updatedCursor(TuioCursor cursor);
	/**
	 * Si verifica quando viene rimosso un TuioCursor dalla sessione.
	 */
	void removedCursor(TuioCursor cursor);
}
