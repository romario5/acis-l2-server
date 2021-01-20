package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public abstract class BoatEngine implements Runnable
{
    public static final Location TALKING_KICK_LOCATION = new Location(-96799,  261259, -3616);
    public static final Location GLUDIN_KICK_LOCATION = new Location(-95254, 150551, -3624);
    public static final Location RUNE_KICK_LOCATION = new Location(34558, -38016, -3610);
    public static final Location INNADRIL_KICK_LOCATION = new Location(111553, 225748, -3600);
    public static final Location GIRAN_KICK_LOCATION = new Location(46763, 187041, -3451);
    public static final Location PRIMEVAL_KICK_LOCATION = new Location(10456, -26935, -3624);

    public static final int RUNE_DOCK_HEADING = 40785;
    public static final int GLUDIN_DOCK_HEADING = 16723;
    public static final int TALKING_DOCK_HEADING = 32768;
    public static final int PRIMEVAL_DOCK_HEADING = 40785;
    public static final int INNADRIL_DOCK_HEADING = 32768;
    public static final int GIRAN_DOCK_HEADING = 60800;

    public static final BoatLocation GLUDIN_DOCK_LOCATION = new BoatLocation(-95686, 150514, -3610, 200, 800);
    public static final BoatLocation TALKING_DOCK_LOCATION = new BoatLocation(-96622, 261660, -3610, 200, 1800);
    public static final BoatLocation RUNE_DOCK_LOCATION = new BoatLocation(34381, -37680, -3610, 200, 800);
    public static final BoatLocation GIRAN_DOCK_LOCATION = new BoatLocation(48950, 190613, -3610, 200, 800);
    public static final BoatLocation INNADRIL_DOCK_LOCATION = new BoatLocation(111264, 226240, -3610, 200, 800);
    public static final BoatLocation PRIMEVAL_DOCK_LOCATION = new BoatLocation(10342, -27279, -3610, 200, 800);

    public static final int GLUDIN_HARBOR = BoatManager.GLUDIN_HARBOR;
    public static final int RUNE_HARBOR = BoatManager.RUNE_HARBOR;
    public static final int TALKING_HARBOR = BoatManager.TALKING_HARBOR;

    protected final Boat _boat;
    protected int _cycle = 0;
    protected int _shoutCount = 0;
    protected double _startTime = 0;

    public static final int BOAT_BROADCAST_RADIUS = 20000;

    public BoatEngine(Boat boat) {
        _boat = boat;
    }


    public boolean takeDock(int dockId, long duration)
    {
        if (_boat == null) return false;
        BoatManager.getInstance().takeDock(_boat.getObjectId(), dockId, duration);
        return true;
    }


    public boolean releaseDock(int dockId)
    {
        return BoatManager.getInstance().releaseDock(_boat.getObjectId(), dockId);
    }

    /**
     * Check if the dock is busy.
     * @param dockId : The dock id.
     * @return true if the dock is locked, false otherwise.
     */
    public boolean isDockBusy(int dockId)
    {
        return BoatManager.getInstance().isDockBusy(dockId);
    }


    public boolean isDockTakenByBoat(int dockId, Boat boat)
    {
        return BoatManager.getInstance().isDockTakenByBoat(dockId, boat.getObjectId());
    }


    /**
     * Broadcast one packet in both path points.
     * @param point1 : The first location to broadcast the packet.
     * @param point2 : The second location to broadcast the packet.
     * @param packet : The packet to broadcast.
     */
    public void broadcastPacket(BoatLocation point1, BoatLocation point2, L2GameServerPacket packet)
    {
        BoatManager.getInstance().broadcastPacket(point1, point2, packet);
    }

    /**
     * Broadcast one packet in both path points.
     * @param point : The location to broadcast the packet.
     * @param packet : The packet to broadcast.
     */
    public void broadcastPacket(BoatLocation point, L2GameServerPacket packet)
    {
        BoatManager.getInstance().broadcastPacket(point, packet);
    }

    /**
     * Broadcast several packets in both path points.
     * @param point1 : The first location to broadcast the packet.
     * @param point2 : The second location to broadcast the packet.
     * @param packets : The packets to broadcast.
     */
    public void broadcastPackets(BoatLocation point1, BoatLocation point2, L2GameServerPacket... packets)
    {
        BoatManager.getInstance().broadcastPackets(point1, point2, packets);
    }

    /**
     * Broadcast several packets in both path points.
     * @param point : The location to broadcast the packet.
     * @param packets : The packets to broadcast.
     */
    public void broadcastPackets(BoatLocation point, L2GameServerPacket... packets)
    {
        BoatManager.getInstance().broadcastPackets(point, packets);
    }

    /**
     *
     * @param dockId : The dock ID.
     * @return busy duration left in milliseconds
     */
    public long getDockBusyDurationLeft(int dockId)
    {
        return BoatManager.getInstance().getDockBusyDurationLeft(dockId);
    }

    protected static CreatureSay createSay(SystemMessageId msgId)
    {
        return new CreatureSay(SayType.BOAT, 801, msgId);
    }
}
