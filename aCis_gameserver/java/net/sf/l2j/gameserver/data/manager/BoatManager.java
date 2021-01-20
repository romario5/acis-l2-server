package net.sf.l2j.gameserver.data.manager;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.StatSet;

import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.CreatureTemplate;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public class BoatManager
{
	public static final int TALKING_HARBOR = 0;
	public static final int GLUDIN_HARBOR = 1;
	public static final int RUNE_HARBOR = 2;

	public static final int BOAT_BROADCAST_RADIUS = 20000;
	
	private final Map<Integer, Boat> _boats = new HashMap<>();
	private final boolean[] _docksBusy = new boolean[3];
	private final int[] _docksBoats = new int[3];
	private final long[] _docksBusyDurations = new long[3];
	private final long[] _docksBusyStartTime = new long[3];

	protected BoatManager()
	{
	}
	
	/**
	 * Generate a new {@link Boat}, using a fresh {@link CreatureTemplate}.
	 * @param boatId : The boat id to use.
	 * @param loc: spawn location
	 * @param heading : The heading to use.
	 * @return the new boat instance.
	 */
	public Boat getNewBoat(int boatId, Location loc, int heading)
	{
		final StatSet set = new StatSet();
		set.set("id", boatId);
		set.set("level", 0);
		
		set.set("str", 0);
		set.set("con", 0);
		set.set("dex", 0);
		set.set("int", 0);
		set.set("wit", 0);
		set.set("men", 0);
		
		set.set("hp", 50000);
		set.set("mp", 0);
		
		set.set("hpRegen", 3.e-3f);
		set.set("mpRegen", 3.e-3f);
		
		set.set("radius", 0);
		set.set("height", 0);
		set.set("type", "");
		
		set.set("exp", 0);
		set.set("sp", 0);
		
		set.set("pAtk", 0);
		set.set("mAtk", 0);
		set.set("pDef", 100);
		set.set("mDef", 100);
		
		set.set("rHand", 0);
		set.set("lHand", 0);
		
		set.set("walkSpd", 0);
		set.set("runSpd", 0);
		
		final Boat boat = new Boat(IdFactory.getInstance().getNextId(), new CreatureTemplate(set));
		boat.spawnMe(loc, heading);
		boat.renewBoatEntrances();
		
		_boats.put(boat.getObjectId(), boat);
		
		return boat;
	}
	
	public Boat getBoat(int boatId)
	{
		return _boats.get(boatId);
	}
	
	/**
	 * Lock/unlock dock so only one boat can be docked.
	 * @param dockId : The dock id.
	 * @param value : True if the dock is locked.
	 */
	public void dockBoat(int dockId, boolean value)
	{
		_docksBusy[dockId] = value;
	}

	/**
	 * Locks dock with given ID. Ship can't take dock until it will be released.
	 * @param boatId : The boat ID.
	 * @param dockId : The dock ID.
	 * @param duration : Time the dock will be busy.
	 */
	public void takeDock(int boatId, int dockId, long duration) {
		_docksBoats[dockId] = boatId;
		_docksBusy[dockId] = true;
		_docksBusyStartTime[dockId] = System.currentTimeMillis();
		_docksBusyDurations[dockId] = duration;
	}

	/**
	 * @param dockId : The dock ID.
	 * @return busy duration left in milliseconds
	 */
	public long getDockBusyDurationLeft(int dockId)
	{
		return _docksBusy[dockId]
				? Math.max(0, _docksBusyDurations[dockId] - (System.currentTimeMillis() - _docksBusyStartTime[dockId]))
				: 0;
	}

	/**
	 * Releases dock so another ship can take it.
	 * @param boatId : The boat ID.
	 * @param dockId : The dock ID.
	 */
	public boolean releaseDock(int boatId, int dockId) {
		if (_docksBoats[dockId] == 0 || _docksBoats[dockId] == boatId) {
			_docksBusy[dockId] = false;
			_docksBoats[dockId] = 0;
			_docksBusyStartTime[dockId] = 0;
			return true;
		}
		return false;
	}

	public boolean isDockTakenByBoat(int dockId, int boatId)
	{
		return _docksBoats[dockId] == boatId;
	}

	/**
	 * Check if the dock is busy.
	 * @param dockId : The dock id.
	 * @return true if the dock is locked, false otherwise.
	 */
	public boolean isDockBusy(int dockId)
	{
		return _docksBusy[dockId];
	}
	
	/**
	 * Broadcast one packet in both path points.
	 * @param point1 : The first location to broadcast the packet.
	 * @param point2 : The second location to broadcast the packet.
	 * @param packet : The packet to broadcast.
	 */
	public void broadcastPacket(BoatLocation point1, BoatLocation point2, L2GameServerPacket packet)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isIn2DRadius(point1, BOAT_BROADCAST_RADIUS) || player.isIn2DRadius(point2, BOAT_BROADCAST_RADIUS))
				player.sendPacket(packet);
		}
	}
	/**
	 * Broadcast one packet in both path points.
	 * @param point : The first location to broadcast the packet.
	 * @param packet : The packet to broadcast.
	 */
	public void broadcastPacket(BoatLocation point, L2GameServerPacket packet)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isIn2DRadius(point, BOAT_BROADCAST_RADIUS))
				player.sendPacket(packet);
		}
	}
	
	/**
	 * Broadcast several packets in both path points.
	 * @param point1 : The first location to broadcast the packet.
	 * @param point2 : The second location to broadcast the packet.
	 * @param packets : The packets to broadcast.
	 */
	public void broadcastPackets(BoatLocation point1, BoatLocation point2, L2GameServerPacket... packets)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isIn2DRadius(point1, BOAT_BROADCAST_RADIUS) || player.isIn2DRadius(point2, BOAT_BROADCAST_RADIUS))
			{
				for (L2GameServerPacket packet : packets)
					player.sendPacket(packet);
			}
		}
	}

	/**
	 * Broadcast several packets in both path points.
	 * @param point : The location to broadcast the packet.
	 * @param packets : The packets to broadcast.
	 */
	public void broadcastPackets(BoatLocation point, L2GameServerPacket... packets)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isIn2DRadius(point, BOAT_BROADCAST_RADIUS))
			{
				for (L2GameServerPacket packet : packets)
					player.sendPacket(packet);
			}
		}
	}
	
	public static final BoatManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BoatManager INSTANCE = new BoatManager();
	}
}