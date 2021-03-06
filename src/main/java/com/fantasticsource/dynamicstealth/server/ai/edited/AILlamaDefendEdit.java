package com.fantasticsource.dynamicstealth.server.ai.edited;

import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityWolf;

public class AILlamaDefendEdit extends AINearestAttackableTargetEdit<EntityWolf>
{
    public AILlamaDefendEdit(EntityAINearestAttackableTarget<EntityWolf> oldAI)
    {
        super(oldAI);
    }

    @Override
    public boolean shouldExecute()
    {
        return targetEntity != null && !targetEntity.isTamed() && super.shouldExecute();
    }

    @Override
    protected double getFollowDistance()
    {
        return super.getFollowDistance() * 0.25;
    }
}
