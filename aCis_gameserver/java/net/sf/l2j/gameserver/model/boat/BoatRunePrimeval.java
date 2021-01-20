package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.boat.routes.RoutePrimevalRune;
import net.sf.l2j.gameserver.model.boat.routes.RouteRunePrimeval;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class BoatRunePrimeval extends BoatEngine
{
	public static final long WAITING_DURATION = 180000;
	
	private static final BoatLocation[] RUNE_DOCK =
	{
		new BoatLocation(34381, -37680, -3610, 220, 800)
	};
	
	private static final BoatLocation PRIMEVAL_DOCK = RouteRunePrimeval.LOCATIONS[RouteRunePrimeval.LOCATIONS.length - 1];
	
	private final CreatureSay ARRIVED_AT_RUNE       = createSay(SystemMessageId.ARRIVED_AT_RUNE);
	private final CreatureSay ARRIVED_AT_RUNE_2     = createSay(SystemMessageId.FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES);
	private final CreatureSay LEAVING_RUNE          = createSay(SystemMessageId.FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW);
	private final CreatureSay ARRIVED_AT_PRIMEVAL   = createSay(SystemMessageId.FERRY_ARRIVED_AT_PRIMEVAL);
	private final CreatureSay ARRIVED_AT_PRIMEVAL_2 = createSay(SystemMessageId.FERRY_LEAVING_FOR_RUNE_3_MINUTES);
	private final CreatureSay LEAVING_PRIMEVAL      = createSay(SystemMessageId.FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW);
	private final CreatureSay BUSY_RUNE             = createSay(SystemMessageId.FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED);
	
	private final PlaySound RUNE_SOUND;
	private final PlaySound PRIMEVAL_SOUND;
	
	public BoatRunePrimeval(Boat boat)
	{
		super(boat);
		RUNE_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		PRIMEVAL_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
	}
	
	@Override
	public void run()
	{
		switch (_cycle)
		{
			case 0:
				releaseDock(RUNE_HARBOR);
				broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, LEAVING_RUNE, RUNE_SOUND);
				_boat.payForRide(8925, 1, RUNE_KICK_LOCATION);
				_startTime = System.currentTimeMillis();
				_boat.getMove().executePath(RouteRunePrimeval.LOCATIONS);
				break;

			case 1:
				broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], ARRIVED_AT_PRIMEVAL, PRIMEVAL_SOUND);
				_cycle = 2;
				ThreadPool.execute(this);
				return;

			case 2:
				if (Config.BOATS_SCHEDULING) {
					long busyDuration = getDockBusyDurationLeft(RUNE_HARBOR);
					long travelDuration = WAITING_DURATION + RoutePrimevalRune.DURATION;
					if (busyDuration >= travelDuration) {
						ThreadPool.schedule(this, busyDuration - travelDuration + 30000);
						return;
					}
				}

				broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], ARRIVED_AT_PRIMEVAL_2);
				ThreadPool.schedule(this, WAITING_DURATION);
				break;

			case 3:
				broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], LEAVING_PRIMEVAL, PRIMEVAL_SOUND);
				_boat.payForRide(8924, 1, PRIMEVAL_KICK_LOCATION);
				_boat.getMove().executePath(RoutePrimevalRune.LOCATIONS);
				_startTime = System.currentTimeMillis();
				break;

			case 4:
				if (isDockBusy(RUNE_HARBOR))
				{
					if (_shoutCount == 0)
						broadcastPacket(RUNE_DOCK[0], PRIMEVAL_DOCK, BUSY_RUNE);
					
					_shoutCount++;
					if (_shoutCount > 35)
						_shoutCount = 0;
					
					ThreadPool.schedule(this, 5000);
					return;
				}
				takeDock(RUNE_HARBOR, WAITING_DURATION);
				_boat.getMove().executePath(RUNE_DOCK);
				break;

			case 5:
				broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, ARRIVED_AT_RUNE, ARRIVED_AT_RUNE_2, RUNE_SOUND);
				ThreadPool.schedule(this, WAITING_DURATION);
				break;
		}
		_shoutCount = 0;
		
		_cycle++;
		if (_cycle > 5)
			_cycle = 0;
	}
	
	public static void load()
	{
		final Boat boat = BoatManager.getInstance().getNewBoat(5, RUNE_DOCK_LOCATION, RUNE_DOCK_HEADING);
		if (boat != null)
		{
			BoatRunePrimeval engine = new BoatRunePrimeval(boat);
			boat.registerEngine(engine);
			engine.takeDock(RUNE_HARBOR, 480000);
			boat.runEngine(180000);
		}
	}
}