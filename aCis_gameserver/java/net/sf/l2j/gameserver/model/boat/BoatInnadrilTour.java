package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.boat.routes.RouteInnadrilTour;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class BoatInnadrilTour extends BoatEngine
{
	private final CreatureSay ARRIVED_AT_INNADRIL = createSay(SystemMessageId.INNADRIL_BOAT_ANCHOR_10_MINUTES);
	private final CreatureSay LEAVE_INNADRIL5 = createSay(SystemMessageId.INNADRIL_BOAT_LEAVE_IN_5_MINUTES);
	private final CreatureSay LEAVE_INNADRIL1 = createSay(SystemMessageId.INNADRIL_BOAT_LEAVE_IN_1_MINUTE);
	private final CreatureSay LEAVE_INNADRIL0 = createSay(SystemMessageId.INNADRIL_BOAT_LEAVE_SOON);
	private final CreatureSay LEAVING_INNADRIL = createSay(SystemMessageId.INNADRIL_BOAT_LEAVING);
	
	private final CreatureSay ARRIVAL20 = createSay(SystemMessageId.INNADRIL_BOAT_ARRIVE_20_MINUTES);
	private final CreatureSay ARRIVAL15 = createSay(SystemMessageId.INNADRIL_BOAT_ARRIVE_15_MINUTES);
	private final CreatureSay ARRIVAL10 = createSay(SystemMessageId.INNADRIL_BOAT_ARRIVE_10_MINUTES);
	private final CreatureSay ARRIVAL5 = createSay(SystemMessageId.INNADRIL_BOAT_ARRIVE_5_MINUTES);
	private final CreatureSay ARRIVAL1 = createSay(SystemMessageId.INNADRIL_BOAT_ARRIVE_1_MINUTE);
	
	private final PlaySound INNADRIL_SOUND;
	private final PlaySound INNADRIL_SOUND_LEAVE_5MIN;
	private final PlaySound INNADRIL_SOUND_LEAVE_1MIN;
	
	public BoatInnadrilTour(Boat boat)
	{
		super(boat);

		INNADRIL_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		INNADRIL_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		INNADRIL_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
	}
	
	@Override
	public void run()
	{
		switch (_cycle)
		{
			case 0:
				broadcastPacket(INNADRIL_DOCK_LOCATION, LEAVE_INNADRIL5);
				_boat.broadcastPacket(INNADRIL_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;
			case 1:
				broadcastPacket(INNADRIL_DOCK_LOCATION, LEAVE_INNADRIL1);
				_boat.broadcastPacket(INNADRIL_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;
			case 2:
				broadcastPacket(INNADRIL_DOCK_LOCATION, LEAVE_INNADRIL0);
				_boat.broadcastPacket(INNADRIL_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;
			case 3:
				broadcastPackets(INNADRIL_DOCK_LOCATION, LEAVING_INNADRIL, INNADRIL_SOUND);
				_boat.payForRide(0, 1, INNADRIL_KICK_LOCATION);
				_boat.getMove().executePath(RouteInnadrilTour.LOCATIONS);
				ThreadPool.schedule(this, RouteInnadrilTour.DURATION - 1200000);
				break;
			case 4:
				broadcastPacket(INNADRIL_DOCK_LOCATION, ARRIVAL20);
				ThreadPool.schedule(this, 300000);
				break;
			case 5:
				broadcastPacket(INNADRIL_DOCK_LOCATION, ARRIVAL15);
				ThreadPool.schedule(this, 300000);
				break;
			case 6:
				broadcastPacket(INNADRIL_DOCK_LOCATION, ARRIVAL10);
				ThreadPool.schedule(this, 300000);
				break;
			case 7:
				broadcastPacket(INNADRIL_DOCK_LOCATION, ARRIVAL5);
				ThreadPool.schedule(this, 240000);
				break;
			case 8:
				broadcastPacket(INNADRIL_DOCK_LOCATION, ARRIVAL1);
				break;
			case 9:
				broadcastPackets(INNADRIL_DOCK_LOCATION, ARRIVED_AT_INNADRIL, INNADRIL_SOUND);
				ThreadPool.schedule(this, 300000);
				break;
		}
		_cycle++;
		if (_cycle > 9)
			_cycle = 0;
	}
	
	public static void load()
	{
		final Boat boat = BoatManager.getInstance().getNewBoat(4, INNADRIL_DOCK_LOCATION, INNADRIL_DOCK_HEADING);
		if (boat != null)
		{
			boat.registerEngine(new BoatInnadrilTour(boat));
			boat.runEngine(180000);
		}
	}
}