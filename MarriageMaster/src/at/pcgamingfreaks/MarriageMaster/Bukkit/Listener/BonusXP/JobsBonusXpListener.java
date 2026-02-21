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
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobsPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

public class JobsBonusXpListener implements Listener, IBonusXpListener<JobsExpGainEvent, Job>
{
	private final Set<String> blockedJobs;
	private final IBonusXpCalculator<JobsExpGainEvent, Job> calculator;

	public JobsBonusXpListener(@NotNull MarriageMaster plugin)
	{
		blockedJobs = plugin.getConfiguration().getJobsBonusXpBlockedJobs();

		if(plugin.getConfiguration().isAuraSkillsBonusXPSplitWithAllEnabled())
			calculator = new AllPartnersInRangeBonusXpCalculator<>(plugin, plugin.getConfiguration().getJobsBonusXpMultiplier(), this);
		else
			calculator = new NearestPartnerBonusXpCalculator<>(plugin, plugin.getConfiguration().getJobsBonusXpMultiplier(), plugin.getConfiguration().isJobsBonusXPSplitEnabled(), this);

		plugin.getLogger().info(ConsoleColor.GREEN + "Jobs hooked" + ConsoleColor.RESET);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onJobsExpGain(@NotNull JobsExpGainEvent event)
	{
		final Job job = event.getJob();
		if(isJobBlocked(job)) return;
		final Player player = event.getPlayer().getPlayer();
		if(player == null) return;

		calculator.process(event, player, event.getExp() ,job);
	}

	@Override
	public void setEventExp(@NotNull JobsExpGainEvent event, double xp, @Nullable Job job, @NotNull MarriagePlayer player, @NotNull Marriage marriage)
	{
		event.setExp(xp);
	}

	@Override
	public void splitWithPartner(@NotNull JobsExpGainEvent event, @NotNull Player partner, double xp, @Nullable Job job, @NotNull MarriagePlayer player, @NotNull Marriage marriage)
	{
		JobsPlayer jobsPartner = Jobs.getPlayerManager().getJobsPlayer(partner);
		if(jobsPartner != null)
		{
			Jobs.getPlayerManager().addExperience(jobsPartner, job, xp);
		}
	}

	private boolean isJobBlocked(@NotNull Job job)
	{
		return blockedJobs.contains(job.getName().toLowerCase(Locale.ENGLISH));
	}
}
