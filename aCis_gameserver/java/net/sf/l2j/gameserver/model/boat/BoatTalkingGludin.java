package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.boat.routes.RouteGludinTalking;
import net.sf.l2j.gameserver.model.boat.routes.RouteTalkingGludin;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class BoatTalkingGludin extends BoatEngine
{
	private static final BoatLocation[] GLUDIN_DOCK = { GLUDIN_DOCK_LOCATION };
	private static final BoatLocation[] TALKING_DOCK = { TALKING_DOCK_LOCATION };

	public static final long WAITING_DURATION = 600000;
	
	private final CreatureSay ARRIVED_AT_TALKING = createSay(SystemMessageId.FERRY_ARRIVED_AT_TALKING);
	private final CreatureSay ARRIVED_AT_TALKING_2 = createSay(SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES);
	private final CreatureSay LEAVE_TALKING5 = createSay(SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES);
	private final CreatureSay LEAVE_TALKING1 = createSay(SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE);
	private final CreatureSay LEAVE_TALKING1_2 = createSay(SystemMessageId.MAKE_HASTE_GET_ON_BOAT);
	private final CreatureSay LEAVE_TALKING0 = createSay(SystemMessageId.FERRY_LEAVE_SOON_FOR_GLUDIN);
	private final CreatureSay LEAVING_TALKING = createSay(SystemMessageId.FERRY_LEAVING_FOR_GLUDIN);
	private final CreatureSay ARRIVED_AT_GLUDIN = createSay(SystemMessageId.FERRY_ARRIVED_AT_GLUDIN);
	private final CreatureSay ARRIVED_AT_GLUDIN_2 = createSay(SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES);
	private final CreatureSay LEAVE_GLUDIN5 = createSay(SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES);
	private final CreatureSay LEAVE_GLUDIN1 = createSay(SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE);
	private final CreatureSay LEAVE_GLUDIN0 = createSay(SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING);
	private final CreatureSay LEAVING_GLUDIN = createSay(SystemMessageId.FERRY_LEAVING_FOR_TALKING);
	private final CreatureSay BUSY_TALKING = createSay(SystemMessageId.FERRY_GLUDIN_TALKING_DELAYED);
	private final CreatureSay BUSY_GLUDIN = createSay(SystemMessageId.FERRY_TALKING_GLUDIN_DELAYED);
	
	private final CreatureSay ARRIVAL_GLUDIN10 = createSay(SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES);
	private final CreatureSay ARRIVAL_GLUDIN5 = createSay(SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES);
	private final CreatureSay ARRIVAL_GLUDIN1 = createSay(SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE);
	private final CreatureSay ARRIVAL_TALKING10 = createSay(SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES);
	private final CreatureSay ARRIVAL_TALKING5 = createSay(SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES);
	private final CreatureSay ARRIVAL_TALKING1 = createSay(SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE);
	
	private final PlaySound TALKING_SOUND;
	private final PlaySound GLUDIN_SOUND;
	private final PlaySound TALKING_SOUND_LEAVE_5MIN;
	private final PlaySound TALKING_SOUND_LEAVE_1MIN;
	private final PlaySound GLUDIN_SOUND_LEAVE_5MIN;
	private final PlaySound GLUDIN_SOUND_LEAVE_1MIN;
	
	public BoatTalkingGludin(Boat boat)
	{
		super(boat);
		TALKING_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		GLUDIN_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		TALKING_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		TALKING_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
		GLUDIN_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		GLUDIN_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
	}
	
	@Override
	public void run()
	{
		switch (_cycle)
		{
			case 0:
				broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_TALKING5);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;
			case 1:
				broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_TALKING1, LEAVE_TALKING1_2);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;
			case 2:
				broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_TALKING0);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;
			case 3:
				releaseDock(BoatManager.TALKING_HARBOR);
				broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVING_TALKING);
				_boat.broadcastPacket(TALKING_SOUND);
				_boat.payForRide(1074, 1, TALKING_KICK_LOCATION);
				_boat.getMove().executePath(RouteTalkingGludin.LOCATIONS);

				if (Config.BOATS_SCHEDULING) {
					long dockBusyDuration = getDockBusyDurationLeft(GLUDIN_HARBOR);
					if (dockBusyDuration > 0) {
						ThreadPool.schedule(() -> {
							takeDock(GLUDIN_HARBOR, RouteTalkingGludin.DURATION + WAITING_DURATION);
						}, dockBusyDuration + 1000);
					} else {
						takeDock(GLUDIN_HARBOR, RouteTalkingGludin.DURATION + WAITING_DURATION);
					}
				}

				ThreadPool.schedule(this, RouteTalkingGludin.DURATION - 600000);
				break;
			case 4:
				broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVAL_GLUDIN10);
				ThreadPool.schedule(this, 300000);
				break;
			case 5:
				broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVAL_GLUDIN5);
				ThreadPool.schedule(this, 240000);
				break;
			case 6:
				broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVAL_GLUDIN1);
				break;
			case 7:
				if (isDockBusy(GLUDIN_HARBOR) && !isDockTakenByBoat(GLUDIN_HARBOR, _boat))
				{
					if (_shoutCount == 0)
						BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], BUSY_GLUDIN);
					
					_shoutCount++;
					if (_shoutCount > 35)
						_shoutCount = 0;
					
					ThreadPool.schedule(this, 5000);
					return;
				}
				takeDock(BoatManager.GLUDIN_HARBOR, WAITING_DURATION);
				_boat.getMove().executePath(GLUDIN_DOCK);
				break;
			case 8:
				broadcastPackets(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVED_AT_GLUDIN);
				_boat.broadcastPacket(GLUDIN_SOUND);
				_cycle = 9;
				ThreadPool.execute(this);
				return;

			case 9:
				if (Config.BOATS_SCHEDULING) {
					long dockBusyDuration = getDockBusyDurationLeft(TALKING_HARBOR);
					long travelDuration = RouteGludinTalking.DURATION + WAITING_DURATION;

					if (dockBusyDuration > travelDuration) {
						takeDock(GLUDIN_HARBOR, dockBusyDuration - travelDuration + WAITING_DURATION + 20000);
						ThreadPool.schedule(this, dockBusyDuration - travelDuration + 20000);
						return;
					}
				}

				takeDock(GLUDIN_HARBOR, WAITING_DURATION);
				broadcastPackets(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVED_AT_GLUDIN_2);
				ThreadPool.schedule(this, 300000);
				break;
			case 10:
				broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_GLUDIN5);
				_boat.broadcastPacket(GLUDIN_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;
			case 11:
				broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_GLUDIN1, LEAVE_TALKING1_2);
				_boat.broadcastPacket(GLUDIN_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;
			case 12:
				broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_GLUDIN0);
				_boat.broadcastPacket(GLUDIN_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;
			case 13:
				releaseDock(BoatManager.GLUDIN_HARBOR);
				broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVING_GLUDIN);
				_boat.broadcastPacket(GLUDIN_SOUND);
				_boat.payForRide(1075, 1, GLUDIN_KICK_LOCATION);
				_boat.getMove().executePath(RouteGludinTalking.LOCATIONS);

				if (Config.BOATS_SCHEDULING) {
					long dockBusyDuration = getDockBusyDurationLeft(TALKING_HARBOR);
					if (dockBusyDuration > 0) {
						ThreadPool.schedule(() -> {
							takeDock(TALKING_HARBOR, RouteGludinTalking.DURATION + WAITING_DURATION);
						}, dockBusyDuration + 1000);
					} else {
						takeDock(TALKING_HARBOR, RouteGludinTalking.DURATION + WAITING_DURATION);
					}
				}

				ThreadPool.schedule(this, RouteGludinTalking.DURATION - 600000);
				break;
			case 14:
				broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_TALKING10);
				ThreadPool.schedule(this, 300000);
				break;
			case 15:
				broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_TALKING5);
				ThreadPool.schedule(this, 240000);
				break;
			case 16:
				broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_TALKING1);
				break;
			case 17:
				if (isDockBusy(TALKING_HARBOR) && !isDockTakenByBoat(TALKING_HARBOR, _boat))
				{
					if (_shoutCount == 0)
						broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], BUSY_TALKING);
					
					_shoutCount++;
					if (_shoutCount > 35)
						_shoutCount = 0;
					
					ThreadPool.schedule(this, 5000);
					return;
				}
				_boat.getMove().executePath(TALKING_DOCK);
				break;
			case 18:
				broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVED_AT_TALKING);
				_boat.broadcastPacket(TALKING_SOUND);
				_cycle = 18;
				ThreadPool.execute(this);
				return;

			case 19:
				if (Config.BOATS_SCHEDULING) {
					long dockBusyDuration = getDockBusyDurationLeft(GLUDIN_HARBOR);
					long travelDuration = RouteTalkingGludin.DURATION + WAITING_DURATION;
					if (dockBusyDuration > travelDuration) {
						takeDock(TALKING_HARBOR, dockBusyDuration - travelDuration + 20000);
						ThreadPool.schedule(this, dockBusyDuration - travelDuration + 20000);
						return;
					}
				}

				broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVED_AT_TALKING_2);
				takeDock(TALKING_HARBOR, WAITING_DURATION);
				ThreadPool.schedule(this, 300000);
		}
		_shoutCount = 0;
		
		_cycle++;
		if (_cycle > 19)
			_cycle = 0;
	}
	
	public static void load()
	{
		final Boat boat = BoatManager.getInstance().getNewBoat(1, TALKING_DOCK_LOCATION, TALKING_DOCK_HEADING);
		if (boat != null)
		{
			BoatTalkingGludin engine = new BoatTalkingGludin(boat);
			boat.registerEngine(engine);
			engine.takeDock(BoatManager.TALKING_HARBOR, 480000);
			boat.runEngine(180000);
		}
	}
}