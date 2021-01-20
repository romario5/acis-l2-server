package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.boat.routes.RouteGiranTalking;
import net.sf.l2j.gameserver.model.boat.routes.RouteTalkingGiran;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class BoatGiranTalking extends BoatEngine
{
	public static final long WAITING_DURATION = 600000;
	private static final BoatLocation[] TALKING_DOCK = { TALKING_DOCK_LOCATION };

	private final CreatureSay ARRIVED_AT_GIRAN = createSay(SystemMessageId.FERRY_ARRIVED_AT_GIRAN);
	private final CreatureSay ARRIVED_AT_GIRAN_2 = createSay(SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES);
	private final CreatureSay LEAVE_GIRAN5 = createSay(SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES);
	private final CreatureSay LEAVE_GIRAN1 = createSay(SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE);
	private final CreatureSay LEAVE_GIRAN0 = createSay(SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING);
	private final CreatureSay LEAVING_GIRAN = createSay(SystemMessageId.FERRY_LEAVING_FOR_TALKING);
	private final CreatureSay ARRIVED_AT_TALKING = createSay(SystemMessageId.FERRY_ARRIVED_AT_TALKING);
	private final CreatureSay ARRIVED_AT_TALKING_2 = createSay(SystemMessageId.FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES);
	private final CreatureSay LEAVE_TALKING5 = createSay(SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES);
	private final CreatureSay LEAVE_TALKING1 = createSay(SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE);
	private final CreatureSay LEAVE_TALKING0 = createSay(SystemMessageId.FERRY_LEAVE_SOON_FOR_GIRAN);
	private final CreatureSay LEAVING_TALKING = createSay(SystemMessageId.FERRY_LEAVING_FOR_GIRAN);
	private final CreatureSay BUSY_TALKING = createSay(SystemMessageId.FERRY_GIRAN_TALKING_DELAYED);
	
	private final CreatureSay ARRIVAL_TALKING15 = createSay(SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES);
	private final CreatureSay ARRIVAL_TALKING10 = createSay(SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES);
	private final CreatureSay ARRIVAL_TALKING5 = createSay(SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES);
	private final CreatureSay ARRIVAL_TALKING1 = createSay(SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE);
	private final CreatureSay ARRIVAL_GIRAN20 = createSay(SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES);
	private final CreatureSay ARRIVAL_GIRAN15 = createSay(SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES);
	private final CreatureSay ARRIVAL_GIRAN10 = createSay(SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES);
	private final CreatureSay ARRIVAL_GIRAN5 = createSay(SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES);
	private final CreatureSay ARRIVAL_GIRAN1 = createSay(SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE);
	
	private final PlaySound GIRAN_SOUND;
	private final PlaySound TALKING_SOUND;
	private final PlaySound GIRAN_SOUND_LEAVE_5MIN;
	private final PlaySound GIRAN_SOUND_LEAVE_1MIN;
	private final PlaySound TALKING_SOUND_LEAVE_5MIN;
	private final PlaySound TALKING_SOUND_LEAVE_1MIN;
	
	public BoatGiranTalking(Boat boat)
	{
		super(boat);
		
		GIRAN_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", boat);
		TALKING_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", boat);
		GIRAN_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", boat);
		GIRAN_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", boat);
		TALKING_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", boat);
		TALKING_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", boat);
	}
	
	@Override
	public void run()
	{
		switch (_cycle)
		{
			case 0:
				broadcastPacket(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], LEAVE_GIRAN5);
				_boat.broadcastPacket(GIRAN_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;

			case 1:
				broadcastPacket(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], LEAVE_GIRAN1);
				_boat.broadcastPacket(GIRAN_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;

			case 2:
				broadcastPacket(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], LEAVE_GIRAN0);
				_boat.broadcastPacket(GIRAN_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;

			case 3:
				broadcastPackets(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], LEAVING_GIRAN, ARRIVAL_TALKING15);
				_boat.broadcastPacket(GIRAN_SOUND);
				_boat.payForRide(3946, 1, GIRAN_KICK_LOCATION);
				_boat.getMove().executePath(RouteGiranTalking.LOCATIONS);
				
				if (Config.BOATS_SCHEDULING) {
					long dockBusyDuration = getDockBusyDurationLeft(GLUDIN_HARBOR);
					if (dockBusyDuration > 0) {
						ThreadPool.schedule(() -> {
							takeDock(GLUDIN_HARBOR, RouteGiranTalking.DURATION + WAITING_DURATION);
						}, dockBusyDuration + 1000);
					} else {
						takeDock(GLUDIN_HARBOR, RouteGiranTalking.DURATION + WAITING_DURATION);
					}
				}

				ThreadPool.schedule(this, RouteGiranTalking.DURATION - 600000);
				break;

			case 4:
				broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, ARRIVAL_TALKING10);
				ThreadPool.schedule(this, 300000);
				break;

			case 5:
				broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, ARRIVAL_TALKING5);
				ThreadPool.schedule(this, 240000);
				break;

			case 6:
				broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, ARRIVAL_TALKING1);
				break;

			case 7:
				if (isDockBusy(BoatManager.TALKING_HARBOR))
				{
					if (_shoutCount == 0)
						broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, BUSY_TALKING);
					
					_shoutCount++;
					if (_shoutCount > 35)
						_shoutCount = 0;
					
					ThreadPool.schedule(this, 5000);
					return;
				}

				takeDock(BoatManager.TALKING_HARBOR, 300000);
				_boat.getMove().executePath(TALKING_DOCK);
				break;

			case 8:
				broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, ARRIVED_AT_TALKING, ARRIVED_AT_TALKING_2);
				_boat.broadcastPacket(TALKING_SOUND);
				ThreadPool.schedule(this, 300000);
				break;

			case 9:
				broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, LEAVE_TALKING5);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;

			case 10:
				broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, LEAVE_TALKING1);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;

			case 11:
				broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, LEAVE_TALKING0);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;

			case 12:
				releaseDock(BoatManager.TALKING_HARBOR);
				broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK_LOCATION, LEAVING_TALKING);
				_boat.broadcastPacket(TALKING_SOUND);
				_boat.payForRide(3945, 1, TALKING_KICK_LOCATION);
				_boat.getMove().executePath(RouteTalkingGiran.LOCATIONS);
				ThreadPool.schedule(this, RouteTalkingGiran.DURATION - 1200000);
				break;

			case 13:
				broadcastPacket(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], ARRIVAL_GIRAN20);
				ThreadPool.schedule(this, 300000);
				break;

			case 14:
				broadcastPacket(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], ARRIVAL_GIRAN15);
				ThreadPool.schedule(this, 300000);
				break;

			case 15:
				broadcastPacket(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], ARRIVAL_GIRAN10);
				ThreadPool.schedule(this, 300000);
				break;

			case 16:
				broadcastPacket(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], ARRIVAL_GIRAN5);
				ThreadPool.schedule(this, 240000);
				break;

			case 17:
				broadcastPacket(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], ARRIVAL_GIRAN1);
				break;


			case 18:
				broadcastPackets(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], ARRIVED_AT_GIRAN);
				_boat.broadcastPacket(GIRAN_SOUND);
				
				if (Config.BOATS_SCHEDULING) {
					long dockBusyDuration = getDockBusyDurationLeft(GLUDIN_HARBOR);
					long travelDuration = RouteGiranTalking.DURATION + WAITING_DURATION;
					if (dockBusyDuration > travelDuration) {
						ThreadPool.schedule(this, dockBusyDuration - travelDuration + 20000);
						return;
					}
				}

				_cycle = 19;
				ThreadPool.execute(this);
				return;
				
			case 19:
				broadcastPackets(GIRAN_DOCK_LOCATION, TALKING_DOCK[0], ARRIVED_AT_GIRAN_2);
				ThreadPool.schedule(this, 300000);
				
		}
		_shoutCount = 0;
		
		_cycle++;
		if (_cycle > 19)
			_cycle = 0;
	}
	
	public static void load()
	{
		final Boat boat = BoatManager.getInstance().getNewBoat(2,  GIRAN_DOCK_LOCATION, GIRAN_DOCK_HEADING);
		if (boat != null)
		{
			BoatGiranTalking engine = new BoatGiranTalking(boat);
			boat.registerEngine(engine);
			boat.runEngine(180000);
		}
	}
}
