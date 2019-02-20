package com.fantasticsource.dynamicstealth.common;

import com.fantasticsource.dynamicstealth.Commands;
import com.fantasticsource.dynamicstealth.client.HUD;
import com.fantasticsource.dynamicstealth.client.RenderAlterer;
import com.fantasticsource.dynamicstealth.common.potions.Potions;
import com.fantasticsource.dynamicstealth.compat.Compat;
import com.fantasticsource.dynamicstealth.compat.CompatCNPC;
import com.fantasticsource.dynamicstealth.compat.CompatDissolution;
import com.fantasticsource.dynamicstealth.server.Attributes;
import com.fantasticsource.dynamicstealth.server.CombatTracker;
import com.fantasticsource.dynamicstealth.server.EntityLookHelperEdit;
import com.fantasticsource.dynamicstealth.server.HelperSystem;
import com.fantasticsource.dynamicstealth.server.ai.AIDynamicStealth;
import com.fantasticsource.dynamicstealth.server.ai.edited.*;
import com.fantasticsource.dynamicstealth.server.entitytracker.EntityTrackerEdit;
import com.fantasticsource.dynamicstealth.server.event.attacks.AssassinationEvent;
import com.fantasticsource.dynamicstealth.server.event.attacks.AttackData;
import com.fantasticsource.dynamicstealth.server.event.attacks.StealthAttackEvent;
import com.fantasticsource.dynamicstealth.server.event.attacks.WeaponEntry;
import com.fantasticsource.dynamicstealth.server.senses.EntitySensesEdit;
import com.fantasticsource.dynamicstealth.server.senses.EntityTouchData;
import com.fantasticsource.dynamicstealth.server.senses.hearing.Communication;
import com.fantasticsource.dynamicstealth.server.senses.sight.EntitySightData;
import com.fantasticsource.dynamicstealth.server.senses.sight.Sight;
import com.fantasticsource.dynamicstealth.server.senses.sight.Tracking;
import com.fantasticsource.dynamicstealth.server.threat.EntityThreatData;
import com.fantasticsource.dynamicstealth.server.threat.Threat;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.ServerTickTimer;
import com.fantasticsource.tools.ReflectionTool;
import com.fantasticsource.tools.Tools;
import com.fantasticsource.tools.TrigLookupTable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static com.fantasticsource.dynamicstealth.config.DynamicStealthConfig.serverSettings;

@Mod(modid = DynamicStealth.MODID, name = DynamicStealth.NAME, version = DynamicStealth.VERSION)
public class DynamicStealth
{
    public static final String MODID = "dynamicstealth";
    public static final String NAME = "Dynamic Stealth";
    public static final String VERSION = "1.12.2.054a";

    public static final TrigLookupTable TRIG_TABLE = new TrigLookupTable(1024);

    private static Field sensesField, lookHelperField, abstractSkeletonAIArrowAttackField, abstractSkeletonAIAttackOnCollideField, worldServerEntityTrackerField;
    private static Class aiSlimeFaceRandomClass, aiEvilAttackClass, aiBearMeleeClass, aiSpiderAttackClass, aiSpiderTargetClass, aiBearAttackPlayerClass, aiLlamaDefendTarget,
            aiPigmanHurtByAggressorClass, aiLlamaHurtByTargetClass, aiPigmanTargetAggressorClass, aiVindicatorJohnnyAttackClass, aiBearHurtByTargetClass, aiGuardianAttackClass,
            aiBlazeFireballAttackClass, aiVexChargeAttackClass, aiShulkerAttackClass, aiShulkerAttackNearestClass, aiShulkerDefenseAttackClass;

    public DynamicStealth()
    {
        Attributes.init();

        MinecraftForge.EVENT_BUS.register(ServerTickTimer.class);
        MinecraftForge.EVENT_BUS.register(CombatTracker.class);
        MinecraftForge.EVENT_BUS.register(EntitySightData.class);
        MinecraftForge.EVENT_BUS.register(DynamicStealth.class);
        MinecraftForge.EVENT_BUS.register(Network.class);
        MinecraftForge.EVENT_BUS.register(Threat.class);
        MinecraftForge.EVENT_BUS.register(Sight.class);
        MinecraftForge.EVENT_BUS.register(Communication.class);
        MinecraftForge.EVENT_BUS.register(Potions.class);
        MinecraftForge.EVENT_BUS.register(Tracking.class);

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            MinecraftForge.EVENT_BUS.register(HUD.class);
            MinecraftForge.EVENT_BUS.register(ClientData.class);
            MinecraftForge.EVENT_BUS.register(RenderAlterer.class);
        }
    }

    @SubscribeEvent
    public static void saveConfig(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID)) ConfigManager.sync(MODID, Config.Type.INSTANCE);
    }

    @SubscribeEvent
    public static void despawn(LivingSpawnEvent.AllowDespawn event)
    {
        EntityLivingBase livingBase = event.getEntityLiving();
        Event.Result result = event.getResult();
        if (livingBase instanceof EntityLiving && result != Event.Result.DENY && result != Event.Result.DEFAULT)
        {
            Threat.remove(livingBase);
        }
    }

    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event)
    {
        Chunk chunk = event.getChunk();
        Set<Entity>[] sets = chunk.getEntityLists();
        for (Set<Entity> set : sets)
        {
            for (Entity entity : set)
            {
                if (entity instanceof EntityLiving) Threat.remove((EntityLiving) entity);
            }
        }
    }

    @SubscribeEvent
    public static void worldUnload(WorldEvent.Unload event)
    {
        for (Entity entity : event.getWorld().loadedEntityList)
        {
            if (entity instanceof EntityLiving) Threat.remove((EntityLiving) entity);
        }
    }


    @SubscribeEvent
    public static void worldLoad(WorldEvent.Load event) throws IllegalAccessException
    {
        World world = event.getWorld();
        if (world instanceof WorldServer && serverSettings.senses.usePlayerSenses)
        {
            worldServerEntityTrackerField.set(world, new EntityTrackerEdit((WorldServer) world));
        }
    }


    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) throws InvocationTargetException, IllegalAccessException
    {
        World world = event.world;
        if (!MCTools.isClient(world) && event.phase == TickEvent.Phase.START)
        {
            if (serverSettings.senses.touch.touchEnabled)
            {
                for (Entity feeler : world.loadedEntityList)
                {
                    if (feeler instanceof EntityLivingBase && feeler.isEntityAlive() && !(feeler instanceof EntityArmorStand || feeler instanceof EntityBat || feeler instanceof FakePlayer) && EntityTouchData.canFeel(feeler))
                    {
                        for (Entity felt : world.getEntitiesWithinAABBExcludingEntity(feeler, feeler.getEntityBoundingBox()))
                        {
                            if (felt.isEntityAlive() && (felt instanceof EntityPlayer || (felt instanceof EntityLiving && !(felt instanceof EntityBat))))
                            {
                                if (feeler instanceof EntityPlayerMP)
                                {
                                    //TODO add indicator for players
                                }
                                else if (feeler instanceof EntityLiving)
                                {
                                    EntityLiving feelerLiving = (EntityLiving) feeler;
                                    makeLivingLookTowardEntity(feelerLiving, felt);
                                    feelerLiving.getNavigator().clearPath();
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Entity entity : world.loadedEntityList)
        {
            if (entity instanceof EntityLiving && entity.isEntityAlive())
            {
                CombatTracker.pathReachesThreatTarget((EntityLiving) entity);
            }
        }
    }


    @SubscribeEvent
    public static void kamikazeDeath(ExplosionEvent event)
    {
        //Because creepers don't trigger the LivingDeathEvent when they explode
        //So I'm making sure they setDead() before the damage happens to prevent threat changes
        //Trying to remove them from threat system right here doesn't work due to the timing
        Explosion explosion = event.getExplosion();
        EntityLivingBase exploder = explosion.getExplosivePlacedBy();
        if (exploder instanceof EntityCreeper) exploder.setDead();
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entityDeath(LivingDeathEvent event)
    {
        EntityLivingBase victim = event.getEntityLiving();
        Threat.set(victim, null, 0);

        Entity source = event.getSource().getTrueSource();
        if (source == null) source = event.getSource().getImmediateSource();

        if (source instanceof EntityLivingBase)
        {
            EntityLivingBase killer = (EntityLivingBase) source;

            boolean wasSeen = false;

            EntityLivingBase witness;
            for (Entity entity : victim.world.loadedEntityList)
            {
                if (entity instanceof EntityLivingBase)
                {
                    witness = (EntityLivingBase) entity;

                    if (Threat.getTarget(witness) == victim)
                    {
                        if (Sight.canSee(witness, victim))
                        {
                            if (MCTools.isOwned(witness)) Threat.set(witness, null, 0);
                            else Threat.clearTarget(witness);
                            Communication.notifyDead(witness, victim);
                        }
                    }
                    else if (HelperSystem.isAlly(witness, victim))
                    {
                        if (Sight.canSee(witness, victim))
                        {
                            //Witness saw victim die
                            if (Sight.canSee(witness, source))
                            {
                                //Witness saw everything
                                wasSeen = true;

                                Threat.set(witness, killer, serverSettings.threat.allyKilledThreat);
                                Communication.warn(witness, killer, killer.getPosition(), true);
                            }
                            else
                            {
                                //Witness saw ally die without seeing killer
                                Threat.set(witness, null, serverSettings.threat.allyKilledThreat);
                                Communication.warn(witness, killer, victim.getPosition(), false);
                            }

                            if (witness instanceof EntityLiving)
                            {
                                AIDynamicStealth stealthAI = AIDynamicStealth.getStealthAI((EntityLiving) witness);
                                if (stealthAI != null)
                                {
                                    stealthAI.fleeIfYouShould(0);

                                    if (stealthAI.isFleeing()) stealthAI.lastKnownPosition = killer.getPosition();
                                }
                            }
                        }
                    }
                }
            }

            if (!wasSeen && !EntityThreatData.isPassive(victim))
            {
                //Target's friends didn't see
                if (!Sight.canSee(victim, source))
                {
                    //Target cannot see us
                    if (Threat.getTarget(victim) != source)
                    {
                        //Target is not searching for *us*
                        if (!(killer instanceof FakePlayer) && !MinecraftForge.EVENT_BUS.post(new AssassinationEvent(killer, victim)))
                        {
                            //Assassinations
                            ItemStack itemStack = killer.getHeldItemMainhand();
                            WeaponEntry weaponEntry = WeaponEntry.get(itemStack, WeaponEntry.TYPE_ASSASSINATION);

                            for (PotionEffect potionEffect : weaponEntry.attackerEffects)
                            {
                                killer.addPotionEffect(new PotionEffect(potionEffect));
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entityAttackedPre(LivingHurtEvent event)
    {
        EntityLivingBase victim = event.getEntityLiving();

        DamageSource dmgSource = event.getSource();
        Entity source = dmgSource.getTrueSource();
        if (source == null) source = dmgSource.getImmediateSource();

        if (source instanceof EntityLivingBase)
        {
            EntityLivingBase attacker = (EntityLivingBase) source;

            //Remove invisibility and blindness if set to do so
            if (serverSettings.interactions.attack.removeInvisibilityOnHit)
            {
                if (!(attacker instanceof FakePlayer)) attacker.removePotionEffect(MobEffects.INVISIBILITY);
                victim.removePotionEffect(MobEffects.INVISIBILITY);
            }
            if (serverSettings.interactions.attack.removeBlindnessOnHit)
            {
                if (!(attacker instanceof FakePlayer)) attacker.removePotionEffect(MobEffects.BLINDNESS);
                victim.removePotionEffect(MobEffects.BLINDNESS);
            }

            if (attacker.isEntityAlive() && !(attacker instanceof FakePlayer))
            {
                //Normal attacks
                ItemStack itemStack = attacker.getHeldItemMainhand();
                WeaponEntry weaponEntry = WeaponEntry.get(itemStack, WeaponEntry.TYPE_NORMAL);

                if (weaponEntry.armorPenetration) dmgSource.setDamageBypassesArmor();
                event.setAmount((float) (event.getAmount() * weaponEntry.damageMultiplier));

                for (PotionEffect potionEffect : weaponEntry.attackerEffects)
                {
                    attacker.addPotionEffect(new PotionEffect(potionEffect));
                }
                for (PotionEffect potionEffect : weaponEntry.victimEffects)
                {
                    victim.addPotionEffect(new PotionEffect(potionEffect));
                }

                if (weaponEntry.consumeItem && !(attacker instanceof EntityPlayer && ((EntityPlayer) attacker).capabilities.isCreativeMode) && !itemStack.getItem().equals(Items.AIR)) itemStack.grow(-1);


                //Stealth attacks
                if (!Sight.canSee(victim, attacker))
                {
                    if (!MinecraftForge.EVENT_BUS.post(new StealthAttackEvent(victim, dmgSource, event.getAmount())))
                    {
                        itemStack = attacker.getHeldItemMainhand();
                        weaponEntry = WeaponEntry.get(itemStack, WeaponEntry.TYPE_STEALTH);

                        if (weaponEntry.armorPenetration) dmgSource.setDamageBypassesArmor();
                        event.setAmount((float) (event.getAmount() * weaponEntry.damageMultiplier));

                        for (PotionEffect potionEffect : weaponEntry.attackerEffects)
                        {
                            attacker.addPotionEffect(new PotionEffect(potionEffect));
                        }
                        for (PotionEffect potionEffect : weaponEntry.victimEffects)
                        {
                            victim.addPotionEffect(new PotionEffect(potionEffect));
                        }

                        if (weaponEntry.consumeItem && !(attacker instanceof EntityPlayer && ((EntityPlayer) attacker).capabilities.isCreativeMode) && !itemStack.getItem().equals(Items.AIR)) itemStack.grow(-1);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void entityAttackedPost(LivingHurtEvent event) throws InvocationTargetException, IllegalAccessException
    {
        if (event.isCanceled() && event.getResult() == Event.Result.DENY) return;

        EntityLivingBase targetBase = event.getEntityLiving();

        Entity source = event.getSource().getTrueSource();
        if (source == null) source = event.getSource().getImmediateSource();

        if (targetBase instanceof EntityLiving)
        {
            EntityLiving target = (EntityLiving) targetBase;

            if (source instanceof EntityLivingBase)
            {
                EntityLivingBase attacker = (EntityLivingBase) source;


                //Look toward damage, check sight, and set perceived position
                makeLivingLookTowardEntity(target, attacker);
                boolean canSee = Sight.canSee(target, attacker, false, true);
                BlockPos perceivedPos = attacker.getPosition();
                if (!canSee)
                {
                    int distance = (int) target.getDistance(attacker);
                    MCTools.randomPos(perceivedPos, Tools.min(distance >> 1, 7), Tools.min(distance >> 2, 4));
                }


                //Warn others (for both attacker and target)
                Communication.warn(target, attacker, perceivedPos, canSee);
                Communication.warn(attacker, target, target.getPosition(), true);


                //Threat, AI, and vanilla attack target
                AIDynamicStealth stealthAI = AIDynamicStealth.getStealthAI(target);
                boolean hasAI = stealthAI != null;
                boolean fleeing = hasAI && stealthAI.isFleeing();

                Threat.ThreatData threatData = Threat.get(target);
                EntityLivingBase threatTarget = threatData.target;
                int threat = threatData.threatLevel;

                if (fleeing)
                {
                    //Be brave, Sir Robin
                    if (serverSettings.ai.flee.increaseOnDamage) Threat.set(target, canSee ? attacker : threatTarget, threat + (int) (event.getAmount() * serverSettings.threat.attackedThreatMultiplierTarget / target.getMaxHealth()));
                    target.setAttackTarget(null);
                    stealthAI.restart(perceivedPos);
                }
                else if (threat == 0)
                {
                    Threat.set(target, canSee ? attacker : null, threat + (int) (Tools.max(event.getAmount(), 1) * serverSettings.threat.attackedThreatMultiplierInitial / target.getMaxHealth()));
                    target.setAttackTarget(canSee ? attacker : null);
                    if (hasAI) stealthAI.restart(perceivedPos);
                }
                else if (threatTarget == attacker || threatTarget == null)
                {
                    //In combat (not fleeing), and hit by threat target or what is presumed to be threat target (if null)
                    Threat.set(target, canSee ? attacker : null, threat + (int) (event.getAmount() * serverSettings.threat.attackedThreatMultiplierTarget / target.getMaxHealth()));
                    target.setAttackTarget(canSee ? attacker : threatTarget);
                    if (hasAI) stealthAI.restart(perceivedPos);
                }
                else
                {
                    //In combat (not fleeing), and hit by an entity besides our threat target
                    double changeFactor = event.getAmount() / target.getMaxHealth();
                    threat -= changeFactor * serverSettings.threat.attackedThreatMultiplierOther;
                    if (threat <= 0)
                    {
                        //Switching targets
                        Threat.set(target, canSee ? attacker : null, (int) (changeFactor * serverSettings.threat.attackedThreatMultiplierInitial));
                        target.setAttackTarget(canSee ? attacker : null);
                        if (hasAI) stealthAI.restart(perceivedPos);
                    }
                    else
                    {
                        //Just reducing threat toward current target
                        Threat.setThreat(target, threat);
                    }
                }


                //Flee if you should
                if (hasAI)
                {
                    if (event.isCanceled()) stealthAI.fleeIfYouShould(0);
                    else stealthAI.fleeIfYouShould(-event.getAmount());
                }
            }
        }

        //Threat for attacker
        if (source instanceof EntityLiving)
        {
            EntityLiving livingSource = (EntityLiving) source;
            Threat.ThreatData data = Threat.get(livingSource);
            if (data.target == targetBase) Threat.setThreat(livingSource, data.threatLevel + (int) (event.getAmount() * serverSettings.threat.damageDealtThreatMultiplier / targetBase.getMaxHealth()));
        }
    }


    public static void makeLivingLookTowardEntity(EntityLiving living, Entity target) throws InvocationTargetException, IllegalAccessException
    {
        makeLivingLookDirection(living, MCTools.getYaw(living, target, TRIG_TABLE), MCTools.getPitch(living, target, TRIG_TABLE));
    }

    public static void makeLivingLookDirection(EntityLiving living, double yawDegrees, double pitchDegrees) throws InvocationTargetException, IllegalAccessException
    {
        float fYaw = (float) yawDegrees;
        living.rotationYaw = fYaw;
        living.rotationYawHead = fYaw;

        living.rotationPitch = (float) pitchDegrees;

        if (living instanceof EntitySlime)
        {
            //Look toward damage (slime)
            for (EntityAITasks.EntityAITaskEntry task : ((EntitySlime) living).tasks.taskEntries)
            {
                if (task.action instanceof AISlimeFaceRandomEdit)
                {
                    ((AISlimeFaceRandomEdit) task.action).setDirection(fYaw, true);
                    break;
                }
            }
        }
    }


    @SubscribeEvent
    public static void entityConstructing(EntityEvent.EntityConstructing event)
    {
        Entity entity = event.getEntity();
        if (entity instanceof EntityLivingBase)
        {
            //Add new stealth-related attributes
            Attributes.addAttributes((EntityLivingBase) entity);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityJoin(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if (entity instanceof EntityLiving)
        {
            EntityLiving living = (EntityLiving) entity;
            if (!Compat.customnpcs || !(NpcAPI.Instance().getIEntity(living) instanceof ICustomNpc)) livingJoinWorld(living);
        }
    }

    public static void livingJoinWorld(EntityLiving living)
    {
        //Set the new senses handler for all living entities (not including players)
        try
        {
            lookHelperField.set(living, new EntityLookHelperEdit(living));

            if (!MCTools.isClient(living.world))
            {
                sensesField.set(living, new EntitySensesEdit(living));

                if (living instanceof AbstractSkeleton)
                {
                    abstractSkeletonAIArrowAttackField.set(living, new AIAttackRangedBowEdit<AbstractSkeleton>((EntityAIAttackRangedBow) abstractSkeletonAIArrowAttackField.get(living)));
                    abstractSkeletonAIAttackOnCollideField.set(living, new AIAttackMeleeEdit((EntityAIAttackMelee) abstractSkeletonAIAttackOnCollideField.get(living)));
                }

                //Entity AI task replacements
                replaceTasks(living.tasks, living);
                replaceTasks(living.targetTasks, living);

                //Entity AI task additions
                if (!EntityThreatData.bypassesThreat(living)) addTasks(living.targetTasks, living.tasks, living);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            FMLCommonHandler.instance().exitJava(137, false);
        }
    }

    private static void replaceTasks(EntityAITasks tasks, EntityLiving living) throws Exception
    {
        Compat.replaceNPEAttackTargetTasks(living);

        Set<EntityAITasks.EntityAITaskEntry> taskSet = tasks.taskEntries;
        EntityAITasks.EntityAITaskEntry[] taskArray = new EntityAITasks.EntityAITaskEntry[taskSet.size()];
        taskSet.toArray(taskArray);
        for (EntityAITasks.EntityAITaskEntry task : taskArray)
        {
            Class actionClass = task.action.getClass();

            if (actionClass == EntityAILookIdle.class) tasks.removeTask(task.action);

                //All done (excluded EntityAIVillagerInteract)
                //EntityAIVillagerInteract is fine as-is, because "they can talk to each other to find each other when not visible"
            else if (actionClass == EntityAIWatchClosest.class) replaceTask(tasks, task, new AIWatchClosestEdit((EntityAIWatchClosest) task.action));
            else if (actionClass == EntityAIWatchClosest2.class) replaceTask(tasks, task, new AIWatchClosestEdit((EntityAIWatchClosest) task.action, true));

                //All done
            else if (actionClass == EntityAIAttackMelee.class) replaceTask(tasks, task, new AIAttackMeleeEdit((EntityAIAttackMelee) task.action));
            else if (actionClass.getName().equals("net.minecraft.entity.monster.AbstractSkeleton$1")) replaceTask(tasks, task, new AIAttackMeleeEdit((EntityAIAttackMelee) task.action));
            else if (actionClass == aiEvilAttackClass) replaceTask(tasks, task, new AIAttackMeleeEdit((EntityAIAttackMelee) task.action));
            else if (actionClass == aiBearMeleeClass) replaceTask(tasks, task, new AIBearAttackEdit((EntityAIAttackMelee) task.action));
            else if (actionClass == aiSpiderAttackClass) replaceTask(tasks, task, new AISpiderAttackEdit((EntityAIAttackMelee) task.action));
            else if (actionClass == EntityAIZombieAttack.class) replaceTask(tasks, task, new AIZombieAttackEdit((EntityAIZombieAttack) task.action));

                //All done (except enderman stuff)
            else if (actionClass == EntityAINearestAttackableTarget.class) replaceTask(tasks, task, new AINearestAttackableTargetEdit((EntityAINearestAttackableTarget) task.action));
            else if (actionClass == aiBearAttackPlayerClass) replaceTask(tasks, task, new AIBearAttackPlayerEdit((EntityAINearestAttackableTarget) task.action));
            else if (actionClass == aiSpiderTargetClass) replaceTask(tasks, task, new AISpiderTargetEdit((EntityAINearestAttackableTarget) task.action));
            else if (actionClass == aiLlamaDefendTarget) replaceTask(tasks, task, new AILlamaDefendEdit((EntityAINearestAttackableTarget) task.action));
            else if (actionClass == aiVindicatorJohnnyAttackClass) replaceTask(tasks, task, new AIJohnnyAttackEdit((EntityAINearestAttackableTarget) task.action));
            else if (actionClass == aiPigmanTargetAggressorClass) replaceTask(tasks, task, new AIPigmanTargetAggressorEdit((EntityAINearestAttackableTarget) task.action));
            else if (actionClass == aiShulkerAttackNearestClass) replaceTask(tasks, task, new AIShulkerAttackNearestEdit((EntityAINearestAttackableTarget) task.action));
            else if (actionClass == aiShulkerDefenseAttackClass) replaceTask(tasks, task, new AIShulkerDefenseAttackEdit((EntityAINearestAttackableTarget) task.action));

                //All done
            else if (actionClass == EntityAIHurtByTarget.class) replaceTask(tasks, task, new AIHurtByTargetEdit((EntityAIHurtByTarget) task.action));
            else if (actionClass == aiPigmanHurtByAggressorClass) replaceTask(tasks, task, new AIPigmanHurtByAggressorEdit((EntityAIHurtByTarget) task.action));
            else if (actionClass == aiLlamaHurtByTargetClass) replaceTask(tasks, task, new AILlamaHurtByTargetEdit((EntityAIHurtByTarget) task.action));
            else if (actionClass == aiBearHurtByTargetClass) replaceTask(tasks, task, new AIBearHurtByTargetEdit((EntityAIHurtByTarget) task.action));

                //Random section
            else if (actionClass == EntityAIAttackRanged.class) replaceTask(tasks, task, new AIAttackRangedEdit((EntityAIAttackRanged) task.action));
            else if (actionClass == EntityAIAttackRangedBow.class) replaceTask(tasks, task, new AIAttackRangedBowEdit<>((EntityAIAttackRangedBow) task.action));
            else if (actionClass == EntityAIFindEntityNearestPlayer.class) replaceTask(tasks, task, new AIFindEntityNearestPlayerEdit((EntityAIFindEntityNearestPlayer) task.action));
            else if (actionClass == EntityAIFindEntityNearest.class) replaceTask(tasks, task, new AIFindEntityNearestEdit((EntityAIFindEntityNearest) task.action));
            else if (actionClass == EntityAITargetNonTamed.class) replaceTask(tasks, task, new AITargetNonTamedEdit((EntityAITargetNonTamed) task.action));
            else if (actionClass == EntityAIFollow.class) replaceTask(tasks, task, new AIParrotFollowEdit((EntityAIFollow) task.action));
            else if (actionClass == EntityAIDefendVillage.class) replaceTask(tasks, task, new AIDefendVillageEdit((EntityAIDefendVillage) task.action));
            else if (actionClass == EntityAIOwnerHurtByTarget.class) replaceTask(tasks, task, new AIOwnerHurtByTargetEdit((EntityAIOwnerHurtByTarget) task.action));
            else if (actionClass == EntityAIOwnerHurtTarget.class) replaceTask(tasks, task, new AIOwnerHurtTargetEdit((EntityAIOwnerHurtTarget) task.action));
            else if (actionClass == EntityAICreeperSwell.class) replaceTask(tasks, task, new AICreeperSwellEdit((EntityCreeper) living));
            else if (actionClass == aiSlimeFaceRandomClass) replaceTask(tasks, task, new AISlimeFaceRandomEdit((EntitySlime) living));
            else if (actionClass == EntityAIOcelotAttack.class) replaceTask(tasks, task, new AIOcelotAttackEdit(living));
            else if (actionClass == aiGuardianAttackClass) replaceTask(tasks, task, new AIGuardianAttackEdit((EntityGuardian) living));
            else if (actionClass == aiBlazeFireballAttackClass) replaceTask(tasks, task, new AIFireballAttackEdit((EntityBlaze) living));
            else if (actionClass == aiVexChargeAttackClass) replaceTask(tasks, task, new AIVexChargeAttackEdit((EntityVex) living));
            else if (actionClass == aiShulkerAttackClass) replaceTask(tasks, task, new AIShulkerAttackEdit((EntityShulker) living));

                //Pet teleport prevention (depending on config)
            else if (actionClass == EntityAIFollowOwner.class) replaceTask(tasks, task, new AIFollowOwnerEdit((EntityTameable) living, (EntityAIFollowOwner) task.action));
            else if (actionClass == EntityAIFollowOwnerFlying.class) replaceTask(tasks, task, new AIFollowOwnerFlyingEdit((EntityTameable) living, (EntityAIFollowOwner) task.action));
        }
    }

    private static void replaceTask(EntityAITasks tasks, EntityAITasks.EntityAITaskEntry oldTask, EntityAIBase newTask)
    {
        tasks.addTask(oldTask.priority, newTask);
        tasks.removeTask(oldTask.action);
    }

    private static void addTasks(EntityAITasks targetTasks, EntityAITasks tasks, EntityLiving living)
    {
        if (!EntityThreatData.bypassesThreat(living))
        {
            tasks.addTask(-7777777, new AIDynamicStealth(living, 1));
        }
    }

    @EventHandler
    public static void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new Commands());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws NoSuchFieldException, IllegalAccessException
    {
        Network.init();

        sensesField = ReflectionTool.getField(EntityLiving.class, "field_70723_bA", "senses");
        lookHelperField = ReflectionTool.getField(EntityLiving.class, "field_70749_g", "lookHelper");
        worldServerEntityTrackerField = ReflectionTool.getField(WorldServer.class, "field_73062_L", "entityTracker");

        abstractSkeletonAIArrowAttackField = ReflectionTool.getField(AbstractSkeleton.class, "field_85037_d", "aiArrowAttack");
        abstractSkeletonAIAttackOnCollideField = ReflectionTool.getField(AbstractSkeleton.class, "field_85038_e", "aiAttackOnCollide");

        aiSlimeFaceRandomClass = ReflectionTool.getInternalClass(EntitySlime.class, "AISlimeFaceRandom");
        aiEvilAttackClass = ReflectionTool.getInternalClass(EntityRabbit.class, "AIEvilAttack");
        aiBearMeleeClass = ReflectionTool.getInternalClass(EntityPolarBear.class, "AIMeleeAttack");
        aiSpiderAttackClass = ReflectionTool.getInternalClass(EntitySpider.class, "AISpiderAttack");
        aiSpiderTargetClass = ReflectionTool.getInternalClass(EntitySpider.class, "AISpiderTarget");
        aiBearAttackPlayerClass = ReflectionTool.getInternalClass(EntityPolarBear.class, "AIAttackPlayer");
        aiLlamaDefendTarget = ReflectionTool.getInternalClass(EntityLlama.class, "AIDefendTarget");
        aiPigmanHurtByAggressorClass = ReflectionTool.getInternalClass(EntityPigZombie.class, "AIHurtByAggressor");
        aiLlamaHurtByTargetClass = ReflectionTool.getInternalClass(EntityLlama.class, "AIHurtByTarget");
        aiPigmanTargetAggressorClass = ReflectionTool.getInternalClass(EntityPigZombie.class, "AITargetAggressor");
        aiVindicatorJohnnyAttackClass = ReflectionTool.getInternalClass(EntityVindicator.class, "AIJohnnyAttack");
        aiBearHurtByTargetClass = ReflectionTool.getInternalClass(EntityPolarBear.class, "AIHurtByTarget");
        aiGuardianAttackClass = ReflectionTool.getInternalClass(EntityGuardian.class, "AIGuardianAttack");
        aiBlazeFireballAttackClass = ReflectionTool.getInternalClass(EntityBlaze.class, "AIFireballAttack");
        aiVexChargeAttackClass = ReflectionTool.getInternalClass(EntityVex.class, "AIChargeAttack");
        aiShulkerAttackClass = ReflectionTool.getInternalClass(EntityShulker.class, "AIAttack");
        aiShulkerAttackNearestClass = ReflectionTool.getInternalClass(EntityShulker.class, "AIAttackNearest");
        aiShulkerDefenseAttackClass = ReflectionTool.getInternalClass(EntityShulker.class, "AIDefenseAttack");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        //Compat init
        if (Loader.isModLoaded("lycanitesmobs")) Compat.lycanites = true;
        if (Loader.isModLoaded("thermalfoundation")) Compat.thermalfoundation = true;
        if (Loader.isModLoaded("ancientwarfare")) Compat.ancientwarfare = true;
        if (Loader.isModLoaded("neat")) Compat.neat = true;
        if (Loader.isModLoaded("statues")) Compat.statues = true;
        if (Loader.isModLoaded("dissolution"))
        {
            Compat.dissolution = true;
            MinecraftForge.EVENT_BUS.register(CompatDissolution.class);
        }
        if (Loader.isModLoaded("customnpcs"))
        {
            Compat.customnpcs = true;
            MinecraftForge.EVENT_BUS.register(CompatCNPC.class);
        }

        AttackData.init();
    }
}
