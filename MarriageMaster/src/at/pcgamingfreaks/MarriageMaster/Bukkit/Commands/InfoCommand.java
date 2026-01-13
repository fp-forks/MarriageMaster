/*
 *   Copyright (C) 2025 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;
import at.pcgamingfreaks.Message.Placeholder.Placeholder;
import at.pcgamingfreaks.Message.Placeholder.Processors.SimpleDatePlaceholderProcessor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfoCommand extends MarryCommand
{
	private final Message messageHeadline, messageFooter, messagePlayer1, messagePlayer2, messageSurname, messageWeddingDate;
	private final Message messagePriest, messageNoPriest, messageHasHome, messagePvP, messageHeart, messageNotMarried;

	public InfoCommand(final @NotNull MarriageMaster plugin)
	{
		super(plugin, "info", plugin.getLanguage().getTranslated("Commands.Description.Info"), Permissions.INFO, plugin.getLanguage().getCommandAliases("Info"));

		Placeholder datePlaceholder = new Placeholder("WeddingDate", new SimpleDatePlaceholderProcessor(plugin.getLanguage().getLangE().getString("Language.Ingame.Info.DateFormat", "yyyy-MM-dd")));

		messageHeadline    = plugin.getLanguage().getMessage("Ingame.Info.Headline");
		messageFooter      = plugin.getLanguage().getMessage("Ingame.Info.Footer");
		messagePlayer1     = plugin.getLanguage().getMessage("Ingame.Info.Player1").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME).placeholder("Surname").placeholder("MagicHeart");
		messagePlayer2     = plugin.getLanguage().getMessage("Ingame.Info.Player2").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME).placeholder("Surname").placeholder("MagicHeart");
		messageSurname     = plugin.getLanguage().getMessage("Ingame.Info.Surname").placeholder("Surname");
		messageWeddingDate = plugin.getLanguage().getMessage("Ingame.Info.WeddingDate").placeholders(datePlaceholder);
		messagePriest      = plugin.getLanguage().getMessage("Ingame.Info.Priest").placeholders(Placeholders.PRIEST_NAME);
		messageNoPriest    = plugin.getLanguage().getMessage("Ingame.Info.NoPriest");
		messageHasHome     = plugin.getLanguage().getMessage("Ingame.Info.HasHome").placeholder("HasHome");
		messagePvP         = plugin.getLanguage().getMessage("Ingame.Info.PvP").placeholder("PvPEnabled");
		messageHeart       = plugin.getLanguage().getMessage("Ingame.Info.Heart").placeholder("MagicHeart");
		messageNotMarried  = plugin.getLanguage().getMessage("Ingame.Info.NotMarried").placeholders(Placeholders.PLAYER_NAME);
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		MarriagePlayer player;
		Marriage marriage = null;

		if(args.length >= 1 && sender.hasPermission(Permissions.INFO_OTHERS))
		{
			MarriagePlayer target = getMarriagePlugin().getPlayerData(args[0]);
			if(target.isMarried())
			{
				if(getMarriagePlugin().areMultiplePartnersAllowed() && args.length >= 2)
				{
					MarriagePlayer partner = target.getPartner(args[1]);
					if(partner != null)
					{
						marriage = target.getMarriageData(partner);
					}
				}
				else
				{
					marriage = target.getMarriageData();
				}
			}
			if(marriage == null)
			{
				if(!target.isMarried())
				{
					messageNotMarried.send(sender, target);
				}
				else
				{
					CommonMessages.getMessageTargetPartnerNotFound().send(sender);
				}
				return;
			}
		}
		else if(sender instanceof Player)
		{
			player = getMarriagePlugin().getPlayerData((Player) sender);
			if(!player.isMarried())
			{
				CommonMessages.getMessageNotMarried().send(sender);
				return;
			}
			if(getMarriagePlugin().areMultiplePartnersAllowed() && args.length >= 1)
			{
				MarriagePlayer partner = player.getPartner(args[0]);
				if(partner == null)
				{
					CommonMessages.getMessageTargetPartnerNotFound().send(sender);
					return;
				}
				marriage = player.getMarriageData(partner);
			}
			else
			{
				marriage = player.getMarriageData();
			}
		}
		else
		{
			showHelp(sender, mainCommandAlias);
			return;
		}

		if(marriage == null)
		{
			CommonMessages.getMessageTargetPartnerNotFound().send(sender);
			return;
		}

		sendInfo(sender, marriage);
	}

	private void sendInfo(final @NotNull CommandSender sender, final @NotNull Marriage marriage)
	{
		MarriagePlayer p1 = marriage.getPartner1();
		MarriagePlayer p2 = marriage.getPartner2();

		messageHeadline.send(sender);
		messagePlayer1.send(sender, p1, p2, marriage.getSurnameString(), marriage.getMagicHeart());
		messagePlayer2.send(sender, p1, p2, marriage.getSurnameString(), marriage.getMagicHeart());
		messageSurname.send(sender, marriage.getSurnameString());
		messageWeddingDate.send(sender, marriage.getWeddingDate());
		if(marriage.getPriest() != null)
		{
			messagePriest.send(sender, marriage.getPriest());
		}
		else
		{
			messageNoPriest.send(sender);
		}
		messageHasHome.send(sender, marriage.isHomeSet() ? "Yes" : "No");
		messagePvP.send(sender, marriage.isPVPEnabled() ? "On" : "Off");
		messageHeart.send(sender, marriage.getMagicHeart());
		messageFooter.send(sender);
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(sender.hasPermission(Permissions.INFO_OTHERS) && args.length == 1)
		{
			return Utils.getPlayerNamesStartingWithVisibleOnly(args[0], sender, Permissions.BYPASS_VANISH);
		}
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}
}
