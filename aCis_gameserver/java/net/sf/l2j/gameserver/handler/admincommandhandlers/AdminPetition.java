package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.manager.PetitionManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminPetition implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_view_petitions",
		"admin_view_petition",
		"admin_accept_petition",
		"admin_reject_petition",
		"admin_reset_petitions"
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		int petitionId = -1;
		
		try
		{
			petitionId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
		}
		
		if (command.equals("admin_view_petitions"))
			PetitionManager.getInstance().sendPendingPetitionList(player);
		else if (command.startsWith("admin_view_petition"))
			PetitionManager.getInstance().viewPetition(player, petitionId);
		else if (command.startsWith("admin_accept_petition"))
		{
			if (PetitionManager.getInstance().isPlayerInConsultation(player))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME));
				return;
			}
			
			if (PetitionManager.getInstance().isPetitionInProcess(petitionId))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
				return;
			}
			
			if (!PetitionManager.getInstance().acceptPetition(player, petitionId))
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION));
		}
		else if (command.startsWith("admin_reject_petition"))
		{
			if (!PetitionManager.getInstance().rejectPetition(player, petitionId))
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER));
		}
		else if (command.equals("admin_reset_petitions"))
		{
			if (PetitionManager.getInstance().isPetitionInProcess())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
				return;
			}
			PetitionManager.getInstance().getPendingPetitions().clear();
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}