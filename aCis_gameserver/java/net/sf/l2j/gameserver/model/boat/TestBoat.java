package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.boat.routes.*;
import net.sf.l2j.gameserver.model.location.BoatLocation;

import java.util.logging.Logger;


public class TestBoat extends BoatEngine
{
    private static final BoatLocation[] GLUDIN_DOCK = { GLUDIN_DOCK_LOCATION };
    private static final BoatLocation[] TALKING_DOCK = { TALKING_DOCK_LOCATION };
    private static final BoatLocation[] RUNE_DOCK = { RUNE_DOCK_LOCATION };
    private static final BoatLocation[] GIRAN_DOCK = { GIRAN_DOCK_LOCATION };

    public TestBoat(Boat boat)
    {
        super(boat);
    }

    @Override
    public void run()
    {
        switch (_cycle)
        {
            case 0:
                _startTime = System.currentTimeMillis();
                _boat.getMove().executePath(RouteTalkingGiran.LOCATIONS);
                break;

            case 1:
                _boat.getMove().executePath(GIRAN_DOCK);
                break;

            case 2:
                Logger.getAnonymousLogger().info( "Talking -> Giran: " + Math.round((System.currentTimeMillis() - _startTime) / 1000));
                ThreadPool.schedule(this, 60000);
                break;
        }

        _cycle++;
        if (_cycle > 2)
            _cycle = 0;
    }

    public static void load()
    {
        final Boat boat = BoatManager.getInstance().getNewBoat(3, TALKING_DOCK_LOCATION, TALKING_DOCK_HEADING);
        if (boat != null)
        {
            TestBoat engine = new TestBoat(boat);
            boat.registerEngine(engine);
            boat.runEngine(60000);
        }
    }
}