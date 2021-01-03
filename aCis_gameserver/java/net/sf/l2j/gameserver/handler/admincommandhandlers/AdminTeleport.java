package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class AdminTeleport implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_instant_move", // alt+G menu
		"admin_recall", // alt+G menu
		"admin_sendhome", // alt+G menu
		"admin_tele",
		"admin_teleport",
		"admin_teleportto" // alt+G menu
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		if (command.equals("admin_tele"))
		{
			sendFile(player, "teleports.htm");
			return;
		}
		
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_instant_move"))
		{
			if (!st.hasMoreTokens())
				player.setTeleMode(1);
			else
			{
				try
				{
					final int mode = Integer.parseInt(st.nextToken());
					if (mode < 0 || mode > 2)
					{
						player.sendMessage("Usage: //instant_move [0|1|2]");
						return;
					}
					
					player.setTeleMode(mode);
				}
				catch (Exception e)
				{
					player.sendMessage("Usage: //instant_move [0|1|2]");
				}
			}
		}
		else if (command.startsWith("admin_recall"))
		{
			if (!st.hasMoreTokens())
			{
				player.sendPacket(SystemMessageId.INVALID_TARGET);
				return;
			}
			
			final String param = st.nextToken();
			switch (param)
			{
				case "clan":
					if (!st.hasMoreTokens())
					{
						player.sendPacket(SystemMessageId.INVALID_TARGET);
						return;
					}
					
					Player targetPlayer = World.getInstance().getPlayer(st.nextToken());
					if (targetPlayer == null)
					{
						player.sendPacket(SystemMessageId.INVALID_TARGET);
						return;
					}
					
					final Clan clan = targetPlayer.getClan();
					if (clan == null)
						targetPlayer.teleportTo(player.getPosition(), 0);
					else
					{
						for (Player clanMember : clan.getOnlineMembers())
							clanMember.teleportTo(player.getPosition(), 0);
					}
					break;
				
				case "party":
					if (!st.hasMoreTokens())
					{
						player.sendPacket(SystemMessageId.INVALID_TARGET);
						return;
					}
					
					targetPlayer = World.getInstance().getPlayer(st.nextToken());
					if (targetPlayer == null)
					{
						player.sendPacket(SystemMessageId.INVALID_TARGET);
						return;
					}
					
					final Party party = targetPlayer.getParty();
					if (party == null)
						targetPlayer.teleportTo(player.getPosition(), 0);
					else
					{
						for (Player partyMember : party.getMembers())
							partyMember.teleportTo(player.getPosition(), 0);
					}
					break;
				
				default:
					targetPlayer = World.getInstance().getPlayer(param);
					if (targetPlayer == null)
					{
						player.sendPacket(SystemMessageId.INVALID_TARGET);
						return;
					}
					
					targetPlayer.teleportTo(player.getPosition(), 0);
					break;
			}
		}
		else if (command.startsWith("admin_sendhome"))
		{
			Player targetPlayer;
			
			if (st.hasMoreTokens())
			{
				targetPlayer = World.getInstance().getPlayer(st.nextToken());
				if (targetPlayer == null)
				{
					player.sendPacket(SystemMessageId.INVALID_TARGET);
					return;
				}
			}
			else
				targetPlayer = getTargetPlayer(player, true);
			
			targetPlayer.teleportTo(TeleportType.TOWN);
			targetPlayer.setIsIn7sDungeon(false);
		}
		else if (command.startsWith("admin_teleportto"))
		{
			if (!st.hasMoreTokens())
			{
				player.sendPacket(SystemMessageId.INVALID_TARGET);
				return;
			}
			
			final Player targetPlayer = World.getInstance().getPlayer(st.nextToken());
			if (targetPlayer == null)
			{
				player.sendPacket(SystemMessageId.INVALID_TARGET);
				return;
			}
			
			player.teleportTo(targetPlayer.getPosition(), 0);
		}
		else if (command.startsWith("admin_teleport"))
		{
			try
			{
				final int x = Integer.parseInt(st.nextToken());
				final int y = Integer.parseInt(st.nextToken());
				final int z = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : GeoEngine.getInstance().getHeight(x, y, player.getZ());
				
				player.teleportTo(x, y, z, 0);
			}
			catch (Exception e)
			{
				sendFile(player, "teleports.htm");
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}