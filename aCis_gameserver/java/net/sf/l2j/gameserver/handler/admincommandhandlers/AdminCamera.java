package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CameraMode;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import net.sf.l2j.gameserver.network.serverpackets.NormalCamera;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;

public class AdminCamera implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_camera",
		"admin_cameramode"
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		if (command.startsWith("admin_camera "))
		{
			try
			{
				final Creature creature = (Creature) player.getTarget();
				final String[] com = command.split(" ");
				
				creature.broadcastPacket(new SpecialCamera(creature.getObjectId(), Integer.parseInt(com[1]), Integer.parseInt(com[2]), Integer.parseInt(com[3]), Integer.parseInt(com[4]), Integer.parseInt(com[5]), Integer.parseInt(com[6]), Integer.parseInt(com[7]), Integer.parseInt(com[8]), Integer.parseInt(com[9])));
			}
			catch (Exception e)
			{
				player.sendMessage("Usage: //camera dist yaw pitch time duration turn rise widescreen unknown");
			}
		}
		else if (command.equals("admin_cameramode"))
		{
			// lolcheck. But basically, chance to be invisible AND rooted is kinda null, except with this command
			if (player.getAppearance().isVisible() && !player.isImmobilized())
			{
				player.setTarget(null);
				player.setIsImmobilized(true);
				player.sendPacket(new CameraMode(1));
				
				// Make the character disappears (from world too)
				player.getAppearance().setVisible(false);
				player.broadcastUserInfo();
				player.decayMe();
				player.spawnMe();
				
				player.sendPacket(new ExShowScreenMessage(1, 0, SMPOS.TOP_CENTER, false, 1, 0, 0, false, 5000, true, "To remove this text, press ALT+H. To exit, press ALT+H and type //cameramode"));
			}
			else
			{
				player.setIsImmobilized(false);
				player.sendPacket(new CameraMode(0));
				player.sendPacket(NormalCamera.STATIC_PACKET);
				
				// Make the character appears (to world too)
				player.getAppearance().setVisible(true);
				player.broadcastUserInfo();
				
				// Teleport back the player to beginning point
				player.teleportTo(player.getPosition(), 0);
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}