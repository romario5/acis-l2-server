package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.manager.MovieMakerManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminMovieMaker implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_addseq",
		"admin_playseqq",
		"admin_delsequence",
		"admin_editsequence",
		"admin_addsequence",
		"admin_playsequence",
		"admin_movie",
		"admin_updatesequence",
		"admin_broadcast",
		"admin_playmovie",
		"admin_broadmovie"
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		if (command.equals("admin_movie"))
		{
			MovieMakerManager.getInstance().mainHtm(player);
		}
		else if (command.startsWith("admin_playseqq"))
		{
			try
			{
				MovieMakerManager.getInstance().playSequence(Integer.parseInt(command.substring(15)), player);
			}
			catch (Exception e)
			{
				player.sendMessage("You entered an invalid sequence id.");
				MovieMakerManager.getInstance().mainHtm(player);
				return;
			}
		}
		else if (command.equals("admin_addseq"))
		{
			MovieMakerManager.getInstance().addSequence(player);
		}
		else if (command.startsWith("admin_delsequence"))
		{
			try
			{
				MovieMakerManager.getInstance().deleteSequence(Integer.parseInt(command.substring(18)), player);
			}
			catch (Exception e)
			{
				player.sendMessage("You entered an invalid sequence id.");
				MovieMakerManager.getInstance().mainHtm(player);
				return;
			}
		}
		else if (command.startsWith("admin_broadcast"))
		{
			try
			{
				MovieMakerManager.getInstance().broadcastSequence(Integer.parseInt(command.substring(16)), player);
			}
			catch (Exception e)
			{
				player.sendMessage("You entered an invalid sequence id.");
				MovieMakerManager.getInstance().mainHtm(player);
				return;
			}
		}
		else if (command.equals("admin_playmovie"))
		{
			MovieMakerManager.getInstance().playMovie(0, player);
		}
		else if (command.equals("admin_broadmovie"))
		{
			MovieMakerManager.getInstance().playMovie(1, player);
		}
		else if (command.startsWith("admin_editsequence"))
		{
			try
			{
				MovieMakerManager.getInstance().editSequence(Integer.parseInt(command.substring(19)), player);
			}
			catch (Exception e)
			{
				player.sendMessage("You entered an invalid sequence id.");
				MovieMakerManager.getInstance().mainHtm(player);
				return;
			}
		}
		else
		{
			String[] args = command.split(" ");
			if (args.length < 10)
			{
				player.sendMessage("Some arguments are missing.");
				return;
			}
			
			final int objectId = (player.getTarget() != null) ? player.getTarget().getObjectId() : player.getObjectId();
			
			if (command.startsWith("admin_addsequence"))
			{
				MovieMakerManager.getInstance().addSequence(player, Integer.parseInt(args[1]), objectId, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9]));
			}
			else if (command.startsWith("admin_playsequence"))
			{
				MovieMakerManager.getInstance().playSequence(player, objectId, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]));
			}
			else if (command.startsWith("admin_updatesequence"))
			{
				MovieMakerManager.getInstance().updateSequence(player, Integer.parseInt(args[1]), objectId, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9]));
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}