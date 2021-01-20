package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.boat.routes.RouteGludinRune;
import net.sf.l2j.gameserver.model.boat.routes.RouteRuneGludin;
import net.sf.l2j.gameserver.model.boat.routes.RouteRunePrimeval;
import net.sf.l2j.gameserver.model.boat.routes.RouteTalkingGludin;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class BoatGludinRune extends BoatEngine
{
	public static final long WAITING_DURATION = 600000;

	private static final BoatLocation[] RUNE_DOCK = { RUNE_DOCK_LOCATION };
	private static final BoatLocation[] GLUDIN_DOCK = { GLUDIN_DOCK_LOCATION };

	private final CreatureSay ARRIVED_AT_GLUDIN   = createSay(SystemMessageId.FERRY_ARRIVED_AT_GLUDIN);
	private final CreatureSay ARRIVED_AT_GLUDIN_2 = createSay(SystemMessageId.DEPARTURE_FOR_RUNE_10_MINUTES);
	private final CreatureSay LEAVE_GLUDIN5       = createSay(SystemMessageId.DEPARTURE_FOR_RUNE_5_MINUTES);
	private final CreatureSay LEAVE_GLUDIN1       = createSay(SystemMessageId.DEPARTURE_FOR_RUNE_1_MINUTE);
	private final CreatureSay LEAVING_GLUDIN      = createSay(SystemMessageId.DEPARTURE_FOR_RUNE_NOW);
	private final CreatureSay ARRIVED_AT_RUNE     = createSay(SystemMessageId.ARRIVED_AT_RUNE);
	private final CreatureSay ARRIVED_AT_RUNE_2   = createSay(SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES);
	private final CreatureSay LEAVE_RUNE5         = createSay(SystemMessageId.DEPARTURE_FOR_GLUDIN_5_MINUTES);
	private final CreatureSay LEAVE_RUNE1         = createSay(SystemMessageId.DEPARTURE_FOR_GLUDIN_1_MINUTE);
	private final CreatureSay LEAVE_RUNE0         = createSay(SystemMessageId.DEPARTURE_FOR_GLUDIN_SHORTLY);
	private final CreatureSay LEAVING_RUNE        = createSay(SystemMessageId.DEPARTURE_FOR_GLUDIN_NOW);
	private final CreatureSay BUSY_GLUDIN         = createSay(SystemMessageId.FERRY_RUNE_GLUDIN_DELAYED);
	private final CreatureSay BUSY_RUNE           = createSay(SystemMessageId.FERRY_GLUDIN_RUNE_DELAYED);
	
	private final CreatureSay ARRIVAL_RUNE15   = createSay(SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES);
	private final CreatureSay ARRIVAL_RUNE10   = createSay(SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES);
	private final CreatureSay ARRIVAL_RUNE5    = createSay(SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES);
	private final CreatureSay ARRIVAL_RUNE1    = createSay(SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE);
	private final CreatureSay ARRIVAL_GLUDIN15 = createSay(SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES);
	private final CreatureSay ARRIVAL_GLUDIN10 = createSay(SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES);
	private final CreatureSay ARRIVAL_GLUDIN5  = createSay(SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES);
	private final CreatureSay ARRIVAL_GLUDIN1  = createSay(SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE);
	
	private final PlaySound GLUDIN_SOUND;
	private final PlaySound RUNE_SOUND;
	private final PlaySound GLUDIN_SOUND_LEAVE_5MIN;
	private final PlaySound GLUDIN_SOUND_LEAVE_1MIN;
	private final PlaySound RUNE_SOUND_LEAVE_5MIN;
	private final PlaySound RUNE_SOUND_LEAVE_1MIN;

	public BoatGludinRune(Boat boat) {
		super(boat);

		GLUDIN_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		RUNE_SOUND   = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		GLUDIN_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		GLUDIN_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
		RUNE_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		RUNE_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
	}


	@Override
	public void run()
	{
		switch (_cycle)
		{
			case 0:
				BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVE_GLUDIN5);
				_boat.broadcastPacket(GLUDIN_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;

			case 1:
				BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVE_GLUDIN1);
				_boat.broadcastPacket(GLUDIN_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;

			case 2:
				BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVE_GLUDIN1);
				_boat.broadcastPacket(GLUDIN_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;

			case 3:
				broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVING_GLUDIN);
				_boat.broadcastPacket(GLUDIN_SOUND);
				_boat.payForRide(7905, 1, GLUDIN_KICK_LOCATION);
				_boat.getMove().executePath(RouteGludinRune.LOCATIONS);
				releaseDock(GLUDIN_HARBOR);
				ThreadPool.schedule(this, RouteGludinRune.DURATION - 900000);

				if (Config.BOATS_SCHEDULING) {
					long estimatedTime = RouteGludinRune.DURATION
							- RouteRunePrimeval.DURATION
							- BoatRunePrimeval.WAITING_DURATION
							- BoatRunePrimeval.WAITING_DURATION;
					ThreadPool.schedule(() -> {
						long busyDuration = WAITING_DURATION
								+ RouteRunePrimeval.DURATION
								+ BoatRunePrimeval.WAITING_DURATION
								+ BoatRunePrimeval.WAITING_DURATION;
						takeDock(RUNE_HARBOR, busyDuration);
					}, estimatedTime);
				}

				break;

			case 4:
				broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE15);
				ThreadPool.schedule(this, 300000);
				break;

			case 5:
				broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE10);
				ThreadPool.schedule(this, 300000);
				break;
			case 6:
				broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE5);
				ThreadPool.schedule(this, 240000);
				break;
			case 7:
				broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE1);
				break;

			case 8:
				if (isDockBusy(RUNE_HARBOR) && !isDockTakenByBoat(RUNE_HARBOR, _boat))
				{
					if (_shoutCount == 0)
						broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], BUSY_RUNE);
					
					_shoutCount++;
					if (_shoutCount > 35)
						_shoutCount = 0;
					
					ThreadPool.schedule(this, 5000);
					return;
				}
				takeDock(RUNE_HARBOR, WAITING_DURATION);
				_boat.getMove().executePath(RUNE_DOCK);
				break;

			case 9:
				if (Config.BOATS_SCHEDULING) {
					// Wait until harbor will be released on arrive.
					long busyDuration = BoatManager.getInstance().getDockBusyDurationLeft(GLUDIN_HARBOR);
					long travelDuration = RouteRuneGludin.DURATION + WAITING_DURATION;
					if (busyDuration > travelDuration) {
						ThreadPool.schedule(this, busyDuration - travelDuration + 1000);
						return;
					}

					// Schedule harbor taking.
					long estimatedTime = RouteRuneGludin.DURATION + WAITING_DURATION
							- RouteTalkingGludin.DURATION
							- BoatTalkingGludin.WAITING_DURATION;

					ThreadPool.schedule(() -> {
						long busyTime = WAITING_DURATION
								+ RouteRunePrimeval.DURATION
								+ BoatRunePrimeval.WAITING_DURATION
								+ BoatRunePrimeval.WAITING_DURATION;
						takeDock(GLUDIN_HARBOR, busyTime);
					}, estimatedTime);
				}

				broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVED_AT_RUNE, ARRIVED_AT_RUNE_2);
				_boat.broadcastPacket(RUNE_SOUND);
				ThreadPool.schedule(this, 300000);
				break;

			case 10:
				broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE5);
				_boat.broadcastPacket(RUNE_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;

			case 11:
				broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE1);
				_boat.broadcastPacket(RUNE_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;

			case 12:
				broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE0);
				_boat.broadcastPacket(RUNE_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;

			case 13:
				releaseDock(RUNE_HARBOR);
				broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVING_RUNE);
				_boat.broadcastPacket(RUNE_SOUND);
				_boat.payForRide(7904, 1, RUNE_KICK_LOCATION);
				_boat.getMove().executePath(RouteRuneGludin.LOCATIONS);
				ThreadPool.schedule(this, RouteRuneGludin.DURATION - 900000);
				break;

			case 14:
				broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN15);
				ThreadPool.schedule(this, 300000);
				break;

			case 15:
				broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN10);
				ThreadPool.schedule(this, 300000);
				break;

			case 16:
				broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN5);
				ThreadPool.schedule(this, 240000);
				break;

			case 17:
				broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN1);
				break;

			case 18:
				if (isDockBusy(GLUDIN_HARBOR) && !isDockTakenByBoat(GLUDIN_HARBOR, _boat))
				{
					if (_shoutCount == 0)
						broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], BUSY_GLUDIN);
					
					_shoutCount++;
					if (_shoutCount > 35)
						_shoutCount = 0;
					
					ThreadPool.schedule(this, 5000);
					return;
				}
				takeDock(GLUDIN_HARBOR, WAITING_DURATION);
				_boat.getMove().executePath(GLUDIN_DOCK);
				break;

			case 19:
				broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVED_AT_GLUDIN, ARRIVED_AT_GLUDIN_2);
				_boat.broadcastPacket(GLUDIN_SOUND);
				ThreadPool.schedule(this, 300000);
				break;
		}
		_shoutCount = 0;
		
		_cycle++;
		if (_cycle > 19)
			_cycle = 0;
	}
	
	public static void load()
	{
		final Boat boat = BoatManager.getInstance().getNewBoat(3, GLUDIN_DOCK_LOCATION, GLUDIN_DOCK_HEADING);
		if (boat != null)
		{
			BoatGludinRune engine = new BoatGludinRune(boat);
			boat.registerEngine(engine);
			engine.takeDock(GLUDIN_HARBOR, 480000);
			boat.runEngine(180000);
		}
	}
}
