package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.manager.BuyListManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;

public class AdminShop implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_buy",
		"admin_gmshop"
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		if (command.startsWith("admin_buy"))
		{
			try
			{
				final int val = Integer.parseInt(command.substring(10));
				
				final NpcBuyList list = BuyListManager.getInstance().getBuyList(val);
				if (list == null)
					player.sendMessage("Invalid buylist id.");
				else
					player.sendPacket(new BuyList(list, player.getAdena(), 0));
			}
			catch (Exception e)
			{
				player.sendMessage("Invalid buylist id.");
			}
		}
		else if (command.equals("admin_gmshop"))
			sendFile(player, "gmshops.htm");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}