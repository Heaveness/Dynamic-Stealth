package com.fantasticsource.dynamicstealth.config.server.interactions;

import com.fantasticsource.dynamicstealth.DynamicStealth;
import net.minecraftforge.common.config.Config;

public class InteractionConfig
{
    @Config.Name("Attack")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactAttack")
    @Config.Comment("When anything attacks anything else (melee)")
    public NormalAttackConfig attack = new NormalAttackConfig();

    @Config.Name("Ranged Attack")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactRangedAttack")
    @Config.Comment("When anything attacks anything else (ranged)")
    public RangedAttackConfig rangedAttack = new RangedAttackConfig();

    @Config.Name("Stealth Attack")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactStealthAtk")
    @Config.Comment("When something attacks an unaware victim (melee)")
    public StealthAttackConfig stealthAttack = new StealthAttackConfig();

    @Config.Name("Ranged Stealth Attack")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactRangedStealthAtk")
    @Config.Comment("When something attacks an unaware victim (ranged)")
    public RangedStealthAttackConfig rangedStealthAttack = new RangedStealthAttackConfig();

    @Config.Name("Assassination")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactAssassination")
    @Config.Comment("When something kills without being detected (melee)")
    public AssassinationConfig assassination = new AssassinationConfig();

    @Config.Name("Ramged Assassination")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactRangedAssassination")
    @Config.Comment("When something kills without being detected (ranged)")
    public RangedAssassinationConfig rangedAssassination = new RangedAssassinationConfig();

    @Config.Name("Calm Down")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactCalmDown")
    @Config.Comment("When something was fleeing, but ran out of threat")
    public CalmDownConfig calmDown = new CalmDownConfig();

    @Config.Name("Give Up Search")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactGiveUpSearch")
    @Config.Comment("When something was searching for a target, but ran out of threat")
    public GiveUpSearchConfig giveUpSearch = new GiveUpSearchConfig();

    @Config.Name("Rally")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactRally")
    @Config.Comment("When something was fleeing, but regained health")
    public RallyConfig rally = new RallyConfig();

    @Config.Name("Desperation")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactDesperation")
    @Config.Comment("When something is cornered while fleeing")
    public DesperationConfig desperation = new DesperationConfig();

    @Config.Name("Can't Reach")
    @Config.LangKey(DynamicStealth.MODID + ".config.interactCantReach")
    @Config.Comment("When something cannot reach its target")
    public CantReachConfig cantReach = new CantReachConfig();
}
