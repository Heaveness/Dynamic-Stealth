package com.fantasticsource.dynamicstealth.config.server.threat;

import net.minecraftforge.common.config.Config;

public class ThreatConfig
{
    @Config.Name("'Attacked By Same' Multiplier")
    @Config.Comment({"When an in-combat entity is attacked by its current target, its threat is increased by the damage taken times this, divided by its max HP"})
    @Config.RangeDouble(min = 1)
    public double attackedThreatMultiplierTarget = 6000;

    @Config.Name("'Attacked By Other' Multiplier")
    @Config.Comment({"When an in-combat entity is attacked by something that is *not* its current target, its threat is decreased by damage taken times this, divided by its max HP"})
    @Config.RangeDouble(min = 1)
    public double attackedThreatMultiplierOther = 6000;

    @Config.Name("Bypass Threat System (Global)")
    @Config.Comment({"If enabled, all entities should bypass the threat system"})
    public boolean bypassThreatSystem = false;

    @Config.Name("Initial Attack Multiplier")
    @Config.Comment({"When an out-of-combat entity is attacked, its threat is set to the damage taken times this, divided by its max HP"})
    @Config.RangeDouble(min = 1)
    public double attackedThreatMultiplierInitial = 6000;

    @Config.Name("Initial 'Target Spotted' Threat")
    @Config.Comment({"When an out-of-combat entity spots a valid target, its threat is set to this"})
    @Config.RangeInt(min = 1)
    public int targetSpottedThreat = 300;

    @Config.Name("'Ally Killed' Threat")
    @Config.Comment({"When an out-of-combat entity spots a valid target, its threat is set to this"})
    @Config.RangeInt(min = 1)
    public int allyKilledThreat = 1000;

    @Config.Name("'Warned' Threat")
    @Config.Comment("When an entity heeds a warning from another entity, its threat is set to this (if less than this)")
    public int warnedThreat = 300;

    @Config.Name("Maximum Threat")
    @Config.Comment({"The maximum threat level an entity can reach"})
    @Config.RangeInt(min = 1)
    public int maxThreat = 1000;

    @Config.Name("Recognize Passives Automatically")
    @Config.Comment({"If enabled, clients' threat HUDs will display green for passive mobs"})
    public boolean recognizePassive = true;

    @Config.Name("Unseen Target Minimum Threat Level")
    @Config.Comment({"If an entity's threat level falls below this and they don't see their target, they go out-of-combat / stop searching"})
    @Config.RangeInt(min = 0)
    public int unseenMinimumThreat = 0;

    @Config.Name("Unseen Target Degredation Rate")
    @Config.Comment({"Every time an entity updates and their target's position is unknown, this is subtracted from their threat"})
    @Config.RangeInt(min = 1)
    public int unseenTargetDegredationRate = 1;

    @Config.Name("Entity-Specific Settings (Advanced)")
    public SpecificThreatConfig y_entityOverrides = new SpecificThreatConfig();
}
