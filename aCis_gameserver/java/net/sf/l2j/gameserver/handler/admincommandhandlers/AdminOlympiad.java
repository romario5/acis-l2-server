package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;

public class AdminOlympiad implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_endoly",
		"admin_sethero",
		"admin_setnoble"
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		if (command.startsWith("admin_endoly"))
		{
			Olympiad.getInstance().manualSelectHeroes();
			player.sendMessage("Heroes have been formed.");
		}
		else if (command.startsWith("admin_sethero"))
		{
			Player targetPlayer = null;
			if (player.getTarget() instanceof Player)
				targetPlayer = (Player) player.getTarget();
			else
				targetPlayer = player;
			
			targetPlayer.setHero(!targetPlayer.isHero());
			targetPlayer.broadcastUserInfo();
			player.sendMessage("You have modified " + targetPlayer.getName() + "'s hero status.");
		}
		else if (command.startsWith("admin_setnoble"))
		{
			Player targetPlayer = null;
			if (player.getTarget() instanceof Player)
				targetPlayer = (Player) player.getTarget();
			else
				targetPlayer = player;
			
			targetPlayer.setNoble(!targetPlayer.isNoble(), true);
			player.sendMessage("You have modified " + targetPlayer.getName() + "'s noble status.");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}