/*
 *   Copyright (C) 2026 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.BonusXP;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.api.JobsPaymentEvent;
import com.gamingmesh.jobs.container.CurrencyType;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobsPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class JobsBonusXpListener implements Listener
{
	private final MarriageMaster plugin;
	private final Set<String> blockedJobs = new HashSet<>();

	public JobsBonusXpListener(@NotNull MarriageMaster plugin)
	{
		this.plugin = plugin;

		for(String job : plugin.getConfiguration().getJobsBonusXpBlockedJobs())
		{
			blockedJobs.add(job.toLowerCase(Locale.ENGLISH));
		}

		plugin.getLogger().info(ConsoleColor.GREEN + "Jobs hooked" + ConsoleColor.RESET);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onJobsExpGain(@NotNull JobsExpGainEvent event)
	{
		Job job = event.getJob();
		if(isJobBlocked(job)) return;

		Player player = event.getPlayer().getPlayer();
		if(player == null) return;

		MarriagePlayer marriagePlayer = plugin.getPlayerData(player);
		Marriage marriage = marriagePlayer.getNearestPartnerMarriageData();
		if(marriage == null) return;

		MarriagePlayer partner = marriage.getPartner(marriagePlayer);
		if(partner == null || !partner.isOnline() || !marriage.inRangeSquared(plugin.getConfiguration().getRangeSquared(at.pcgamingfreaks.MarriageMaster.Bukkit.Range.BonusXP))) return;

		double multiplier = plugin.getConfiguration().getJobsBonusXpMultiplier();
		if(plugin.getConfiguration().isJobsBonusXPSplitEnabled())
		{
			multiplier *= 0.5f;
		}

		double xp = event.getExp() * multiplier;
		event.setExp(xp);

		if(plugin.getConfiguration().isJobsBonusXPSplitEnabled())
		{
			JobsPlayer jobsPartner = Jobs.getPlayerManager().getJobsPlayer(partner.getPlayerOnline());
			if(jobsPartner != null)
			{
				Jobs.getPlayerManager().addExperience(jobsPartner, job, xp);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onJobsPayment(@NotNull JobsPaymentEvent event)
	{
		Player player = event.getPlayer().getPlayer();
		if(player == null) return;

		MarriagePlayer marriagePlayer = plugin.getPlayerData(player);
		Marriage marriage = marriagePlayer.getNearestPartnerMarriageData();
		if(marriage == null) return;

		MarriagePlayer partner = marriage.getPartner(marriagePlayer);
		if(partner == null || !partner.isOnline() || !marriage.inRangeSquared(plugin.getConfiguration().getRangeSquared(at.pcgamingfreaks.MarriageMaster.Bukkit.Range.BonusXP))) return;

		double multiplier = plugin.getConfiguration().getJobsPaymentMultiplier();
		if(plugin.getConfiguration().isJobsPaymentSplitEnabled())
		{
			multiplier *= 0.5f;
		}

		double money = event.get(CurrencyType.MONEY);
		if(money > 0)
		{
			double boostedMoney = money * multiplier;
			event.set(CurrencyType.MONEY, boostedMoney);

			if(plugin.getConfiguration().isJobsPaymentSplitEnabled())
			{
				Jobs.getEconomy().getEconomy().depositPlayer(partner.getPlayerOnline(), boostedMoney);
			}
		}

		double points = event.get(CurrencyType.POINTS);
		if(points > 0)
		{
			double boostedPoints = points * multiplier;
			event.set(CurrencyType.POINTS, boostedPoints);

			if(plugin.getConfiguration().isJobsPaymentSplitEnabled())
			{
				JobsPlayer jobsPartner = Jobs.getPlayerManager().getJobsPlayer(partner.getPlayerOnline());
				if(jobsPartner != null)
				{
					jobsPartner.addPoints(boostedPoints);
				}
			}
		}
	}

	private boolean isJobBlocked(@NotNull Job job)
	{
		return blockedJobs.contains(job.getName().toLowerCase(Locale.ENGLISH));
	}
}
