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

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.event.skill.XpGainEvent;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

public class AuraSkillsBonusXpListener implements Listener, IBonusXpListener<XpGainEvent, Skill>
{
	private final AuraSkillsApi auraSkills;
	private final IBonusXpCalculator<XpGainEvent, Skill> calculator;
	private final Set<String> blockedSources;
	private final Set<String> blockedSkills;

	public AuraSkillsBonusXpListener(@NotNull MarriageMaster plugin)
	{
		this.auraSkills = AuraSkillsApi.get();
		if(plugin.getConfiguration().isAuraSkillsBonusXPSplitWithAllEnabled())
			calculator = new AllPartnersInRangeBonusXpCalculator<>(plugin, plugin.getConfiguration().getAuraSkillsBonusXpMultiplier(), this);
		else
			calculator = new NearestPartnerBonusXpCalculator<>(plugin, plugin.getConfiguration().getAuraSkillsBonusXpMultiplier(), plugin.getConfiguration().isAuraSkillsBonusXPSplitEnabled(), this);

		blockedSources = plugin.getConfiguration().getAuraSkillsBonusXpBlockedSources();
		blockedSkills = plugin.getConfiguration().getAuraSkillsBonusXpBlockedSkills();

		plugin.getLogger().info(ConsoleColor.GREEN + "AuraSkills hooked" + ConsoleColor.RESET);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onXpGain(@NotNull XpGainEvent event)
	{
		Skill skill = event.getSkill();
		if(isSkillBlocked(skill)) return;

		XpSource source = event.getSource();
		if(source != null && isSourceBlocked(source)) return;

		calculator.process(event, event.getPlayer(), event.getAmount(), skill);
	}

	private boolean isSkillBlocked(@NotNull Skill skill)
	{
		return blockedSkills.contains(skill.toString().toLowerCase(Locale.ENGLISH));
	}

	private boolean isSourceBlocked(@NotNull XpSource source)
	{
		return blockedSources.contains(source.toString().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public void setEventExp(@NotNull XpGainEvent event, double xp, @Nullable Skill skill, @NotNull MarriagePlayer player, @NotNull Marriage marriage)
	{
		event.setAmount(xp);
	}

	@Override
	public void splitWithPartner(@NotNull XpGainEvent event, @NotNull Player partner, double xp, @Nullable Skill skill, @NotNull MarriagePlayer player, @NotNull Marriage marriage)
	{
		auraSkills.getUser(partner.getUniqueId()).addSkillXpRaw(skill, xp);
	}
}
