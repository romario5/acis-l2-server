package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.commons.data.Pagination;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.AdminCommand;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminAdmin implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_gmlist",
		"admin_gmoff",
		"admin_help",
		"admin_link",
		"admin_msg",
		"admin_show"
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		if (command.startsWith("admin_admin"))
			showMainPage(player, command);
		else if (command.startsWith("admin_gmlist"))
			player.sendMessage((AdminData.getInstance().showOrHideGm(player)) ? "Removed from GMList." : "Registered into GMList.");
		else
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			if (command.startsWith("admin_gmoff"))
			{
				int duration = 1;
				if (st.hasMoreTokens())
				{
					try
					{
						duration = Integer.parseInt(st.nextToken());
					}
					catch (Exception e)
					{
						player.sendMessage("Invalid timer set for //gm ; default time is used.");
					}
				}
				
				// We keep the previous level to rehabilitate it later.
				final int previousAccessLevel = player.getAccessLevel().getLevel();
				
				player.setAccessLevel(0);
				player.sendMessage("You no longer have GM status, but will be rehabilitated after " + duration + " minutes.");
				
				ThreadPool.schedule(() ->
				{
					if (!player.isOnline())
						return;
					
					player.setAccessLevel(previousAccessLevel);
					player.sendMessage("Your previous access level has been rehabilitated.");
				}, duration * 60000);
			}
			else if (command.startsWith("admin_help"))
			{
				try
				{
					final int page = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 1;
					
					sendHelp(player, page);
				}
				catch (Exception e)
				{
					sendHelp(player, 1);
				}
			}
			else if (command.startsWith("admin_link"))
			{
				try
				{
					sendFile(player, st.nextToken());
				}
				catch (Exception e)
				{
					sendFile(player, "main_menu.htm");
				}
			}
			else if (command.startsWith("admin_msg"))
			{
				try
				{
					player.sendPacket(SystemMessage.getSystemMessage(Integer.parseInt(st.nextToken())));
				}
				catch (Exception e)
				{
					player.sendMessage("Usage: //msg sysMsgId");
				}
			}
			else if (command.startsWith("admin_show"))
			{
				final Creature targetCreature = getTargetCreature(player, true);
				
				try
				{
					switch (st.nextToken().toLowerCase())
					{
						case "clear":
							if (targetCreature instanceof Player)
								((Player) targetCreature).clearDebugPackets();
							break;
						
						case "html":
							NpcHtmlMessage.SHOW_FILE = !NpcHtmlMessage.SHOW_FILE;
							break;
						
						case "move":
							// Toggle debug move.
							boolean move = !targetCreature.getMove().isDebugMove();
							targetCreature.getMove().setDebugMove(move);
							
							if (move)
							{
								// Send info messages.
								player.sendMessage("Debug move enabled on " + targetCreature.getName());
								if (player != targetCreature)
									targetCreature.sendMessage("Debug move was enabled.");
							}
							else
							{
								// Send info messages.
								player.sendMessage("Debug move disabled on " + targetCreature.getName());
								if (player != targetCreature)
									targetCreature.sendMessage("Debug move was disabled.");
								
								// Clear debug move packet to all GMs.
								World.getInstance().getPlayers().stream().filter(Player::isGM).forEach(p ->
								{
									final ExServerPrimitive debug = p.getDebugPacket("MOVE" + targetCreature.getObjectId());
									debug.reset();
									debug.sendTo(p);
								});
								
								// Clear debug move packet to self.
								if (targetCreature instanceof Player)
								{
									final ExServerPrimitive debug = ((Player) targetCreature).getDebugPacket("MOVE" + targetCreature.getObjectId());
									debug.reset();
									debug.sendTo((Player) targetCreature);
								}
							}
							break;
						
						case "path":
							// Toggle debug move.
							boolean path = !targetCreature.getMove().isDebugPath();
							targetCreature.getMove().setDebugPath(path);
							
							if (path)
							{
								// Send info messages.
								player.sendMessage("Debug path enabled on " + targetCreature.getName());
								if (player != targetCreature)
									targetCreature.sendMessage("Debug path was enabled.");
							}
							else
							{
								// Send info messages.
								player.sendMessage("Debug path disabled on " + targetCreature.getName());
								if (player != targetCreature)
									targetCreature.sendMessage("Debug path was disabled.");
								
								// Clear debug move packet to all GMs.
								World.getInstance().getPlayers().stream().filter(Player::isGM).forEach(p ->
								{
									final ExServerPrimitive debug = p.getDebugPacket("PATH" + targetCreature.getObjectId());
									debug.reset();
									debug.sendTo(p);
								});
								
								// Clear debug move packet to self.
								if (targetCreature instanceof Player)
								{
									final ExServerPrimitive debug = ((Player) targetCreature).getDebugPacket("PATH" + targetCreature.getObjectId());
									debug.reset();
									debug.sendTo((Player) targetCreature);
								}
							}
							break;
						
						default:
							player.sendMessage("Usage : //show <clear|html|move|path>");
							break;
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Usage : //show <clear|html|move|path>");
				}
			}
		}
	}
	
	/**
	 * Send to the {@link Player} all {@link AdminCommand}s informations.
	 * @param player : The Player used as reference.
	 * @param page : The current page we are checking.
	 */
	private static void sendHelp(Player player, int page)
	{
		final StringBuilder sb = new StringBuilder(2000);
		sb.append("<html><body>");
		
		final Pagination<AdminCommand> list = new Pagination<>(AdminData.getInstance().getAdminCommands().stream(), page, PAGE_LIMIT_8);
		for (AdminCommand command : list)
		{
			sb.append(((list.indexOf(command) % 2) == 0 ? "<table width=280 height=40 bgcolor=000000><tr>" : "<table width=280 height=40><tr>"));
			
			// Write the admin command in gold color, with "//".
			StringUtil.append(sb, "<td width=280 height=34><font color=\"LEVEL\">//", command.getName().substring(6), "</font>");
			
			// If params exist, write them in blue in the same line than command.
			if (!command.getParams().isBlank())
				StringUtil.append(sb, " <font color=\"33cccc\">", command.getParams(), "</font>");
			
			// Pass a line, then write the description.
			StringUtil.append(sb, "<br1>", command.getDesc(), "</td>");
			
			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
		}
		list.generateSpace(sb, "<img height=34>");
		list.generatePages(sb, "bypass admin_help %page%");
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	private void showMainPage(Player player, String command)
	{
		String filename = "main";
		
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (st.hasMoreTokens())
		{
			final String param = st.nextToken();
			if (StringUtil.isDigit(param))
			{
				final int mode = Integer.parseInt(param);
				if (mode == 2)
					filename = "game";
				else if (mode == 3)
					filename = "effects";
				else if (mode == 4)
					filename = "server";
			}
			else if (HtmCache.getInstance().isLoadable("data/html/admin/" + param + "_menu.htm"))
				filename = param;
		}
		
		sendFile(player, filename + "_menu.htm");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}