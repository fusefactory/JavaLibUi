package com.fuse.utils.tuio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


import com.fuse.utils.osc.OSCListener;
import com.fuse.utils.osc.OSCMessage;
import com.fuse.utils.osc.OSCReceiver;


public class TuioClient implements OSCListener
{
	private final String address2Dcur = "/tuio/2Dcur";
	private final String alive = "alive";
	private final String set = "set";
	private final String fseq = "fseq";
	
	private int port = 3333;
	private OSCReceiver receiver;
	private boolean connected = false;
	
	private List<TuioListener> listeners;
	private Map<Integer, TuioCursor> tuioCursors;
	private Map<Integer, TuioCursor> frameCursors;
	
	private List<Integer> aliveIds;
	private List<Integer> addedIds;
	private List<Integer> updatedIds;
	private List<Integer> removedIds;
	
	public TuioClient()
	{
		listeners = new CopyOnWriteArrayList<TuioListener>();
		tuioCursors = new ConcurrentHashMap<Integer, TuioCursor>();
		frameCursors = new HashMap<Integer, TuioCursor>();
		aliveIds = new ArrayList<Integer>();
		addedIds = new ArrayList<Integer>();
		updatedIds = new ArrayList<Integer>();
		removedIds = new ArrayList<Integer>();
	}
	
	public TuioClient(int port)
	{
		this();
		this.port = port;
	}
		
	/**
	 * Connette il client TUIO utilizzando la porta passata.
	 * @param port Il numero di porta da utilizzare.
	 */
	public void connect(int port)
	{
		this.port = port;
		this.connect();
	}
	
	/**
	 * Connette il client TUIO.
	 */
	public void connect()
	{
		try
		{
			receiver = new OSCReceiver(port);
			receiver.addListener(this);
			receiver.startListening();
			connected = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			connected = false;
		}
	}
	
	/**
	 * Indica se il client TUIO è connesso.
	 */
	public boolean connected()
	{
		return connected;
	}
	
	/**
	 * Disconnette il client TUIO.
	 */
	public void disconnect()
	{
		receiver.stopListening();
		receiver.close();
	}
	
	/**
	 * Restituisce l'elenco dei TuioCursor presenti nella sessione.
	 */
	public List<TuioCursor> tuioCursors()
	{
		return new ArrayList<TuioCursor>(tuioCursors.values());
	}
	
	public void addListener(TuioListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(TuioListener listener)
	{
		listeners.remove(listener);
	}
	
	public void removeAllListeners()
	{
		listeners.clear();
	}

	@Override
	public void acceptMessage(OSCMessage message)
	{
		String address = message.address();
		Object[] args = message.arguments();
		String command = (String)args[0];
		
		if (address.equals(address2Dcur))
		{
			if (command.equals(alive))
			{
				frameCursors.clear();
				addedIds.clear();
				updatedIds.clear();
				removedIds.clear();
				
				// creo le liste dei cursori presenti
				for (int i = 1; i < args.length; i++)
				{
					Integer sessionID = (Integer)args[i];
					if (aliveIds.contains(sessionID))
					{
						updatedIds.add(sessionID);
						aliveIds.remove(sessionID);
					}
					else addedIds.add(sessionID);
				}
				// creo la lista dei cursori rimossi
				for (int i = 0; i < aliveIds.size(); i++)
				{
					removedIds.add(aliveIds.get(i));
				}		
			}
			
			else if (command.equals(set))
			{
				Integer sessionID = (Integer)args[1];
				float positionX = (Float)args[2];
				float positionY = (Float)args[3];
				float velocityX = (Float)args[4];
				float velocityY = (Float)args[5];
				float motionAcceleration = (Float)args[6];
				
				TuioCursor cursor = tuioCursors.get(sessionID);
				if (cursor != null) cursor.update(System.currentTimeMillis(), positionX, positionY, velocityX, velocityY, motionAcceleration);
				else cursor = new TuioCursor(System.currentTimeMillis(), sessionID, positionX, positionY, velocityX, velocityY, motionAcceleration);
				frameCursors.put(sessionID, cursor);
			}
			
			else if (command.equals(fseq))
			{
				aliveIds.clear();
				
				// aggiorno i cursori rimossi
				for (int i = 0; i < removedIds.size(); i++)
				{
					Integer sessionID = removedIds.get(i);
					TuioCursor cursor = tuioCursors.get(sessionID);
					if (cursor != null)
					{
						tuioCursors.remove(sessionID);
						for (TuioListener listener : listeners) listener.removedCursor(cursor);
					}
				}
				
				// aggiorno i cursori aggiunti
				for (int i = 0; i < addedIds.size(); i++)
				{
					Integer sessionID = addedIds.get(i);
					aliveIds.add(sessionID);
					
					// cerco il cursorID del nuovo cursore
					int cursorID = 0;
					boolean valid = false;
					while (!valid)
					{
						valid = true;
						for (TuioCursor cursor : tuioCursors.values())
						{
							if (cursor.cursorID() == cursorID)
							{
								valid = false;
								cursorID++;
								break;
							}
						}
					}
					
					// aggiungo il nuovo cursore
					TuioCursor cursor = frameCursors.get(sessionID);
					if (cursor != null)
					{
						TuioCursor addedCursor = new TuioCursor(cursor.creatingTime(), cursor.sessionID(), cursorID, cursor.positionX(), cursor.positionY(), cursor.velocityX(), cursor.velocityY(), cursor.motionAcceleration());
						tuioCursors.put(addedCursor.sessionID(), addedCursor);
						for (TuioListener listener : listeners) listener.addedCursor(addedCursor);
					}
				}
				
				// aggiorno i cursori gi� presenti
				for (int i = 0; i < updatedIds.size(); i++)
				{
					Integer sessionID = updatedIds.get(i);
					aliveIds.add(sessionID);
					TuioCursor cursor = tuioCursors.get(sessionID);
					if (cursor != null)
					{
						for (TuioListener listener : listeners) listener.updatedCursor(cursor);
					}
				}
			}
		}
	}
}
