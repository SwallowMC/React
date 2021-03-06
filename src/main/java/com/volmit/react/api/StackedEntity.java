package com.volmit.react.api;

import com.volmit.react.Config;
import com.volmit.react.util.F;
import com.volmit.react.util.M;
import com.volmit.react.util.PE;
import com.volmit.react.util.ParticleEffect;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.Colorable;
import org.bukkit.util.Vector;

public class StackedEntity {
    private final LivingEntity entity;
    private final double rmx;
    private int count;
    private Entity damager;

    public StackedEntity(LivingEntity entity, int count) {
        this.rmx = getMaxHealthFor(entity.getType());
        damager = null;

        if (count > getAbsoluteMaxCount()) {
            count = getAbsoluteMaxCount();
        }

        this.entity = entity;
        this.count = count;
    }

    @SuppressWarnings("deprecation")
    public static double getMaxHealth(LivingEntity e) {
        if (!Capability.ENTITY_ATTRIBUTES.isCapable()) {
            return e.getMaxHealth();
        }

        return e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    public static int getMaxCount(LivingEntity exf) {
        return (int) Math.ceil(((double) 2000 / getMaxHealth(exf)));
    }

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @SuppressWarnings("deprecation")
    public static void setMaxHealth(LivingEntity e, double v) {
        if (!Capability.ENTITY_ATTRIBUTES.isCapable()) {
            e.setMaxHealth(v + 0.1);
        } else {
            e.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(v);
        }
    }

    private double getMaxHealthFor(EntityType type) {
        switch (type) {
            case BAT:
            case PARROT:
                return 6;
            case BLAZE:
            case ZOMBIE_VILLAGER:
            case ZOMBIE:
            case WITHER_SKELETON:
            case VILLAGER:
            case STRAY:
            case SKELETON:
            case PIG_ZOMBIE:
            case HUSK:
            case CREEPER:
                return 20;
            case CAVE_SPIDER:
                return 12;
            case CHICKEN:
            case SNOWMAN:
                return 4;
            case COW:
            case SQUID:
            case PIG:
            case OCELOT:
            case MUSHROOM_COW:
            case GHAST:
                return 10;
            case DONKEY:
                return 30;
            case ELDER_GUARDIAN:
                return 80;
            case ENDERMAN:
                return 40;
            case ENDERMITE:
                return 8;
            case ENDER_DRAGON:
                return 200;
            case EVOKER:
                return 24;
            case GIANT:
                return 100;
            case GUARDIAN:
                return 30;
            case HORSE:
                return 30;
            case ILLUSIONER:
                return 32;
            case IRON_GOLEM:
                return 100;
            case LLAMA:
                return 30;
            case MAGMA_CUBE:
                return 16;
            case MULE:
                return 30;
            case POLAR_BEAR:
                return 30;
            case RABBIT:
                return 3;
            case SHEEP:
                return 8;
            case SHULKER:
                return 30;
            case SILVERFISH:
                return 8;
            case SKELETON_HORSE:
                return 30;
            case SLIME:
                return 16;
            case SPIDER:
                return 16;
            case VEX:
                return 14;
            case VINDICATOR:
                return 24;
            case WITCH:
                return 26;
            case WITHER:
                return 300;
            case WOLF:
                return 8;
            case ZOMBIE_HORSE:
                return 15;
            default:
                break;
        }

        return 10;
    }

    public int getAbsoluteMaxCount() {
        return getEffectiveCount(Config.ENTITYSTACK_MAXIMUM_HEALTH);
    }

    public void update() {
        updateHealth();
        signalSize();
    }

    @SuppressWarnings("deprecation")
    public void destroy() {
        if (Config.ENTITY_STACKER_SHOW_NAME_TAG && Capability.ENTITY_NAMES.isCapable()) {
            entity.setCustomNameVisible(false);
            entity.setCustomName("");
            entity.resetMaxHealth();
        }
        this.remove();
    }

    public void remove() {
        this.entity.remove();
    }

    private void signalSize() {
        double rat = (double) getCount() / (double) getAbsoluteMaxCount();

        if (M.r(rat)) {
            if (Config.ENTITY_STACKER_SHOW_PARTICLES) {
                if (getCount() > 1) {
                    ParticleEffect.VILLAGER_HAPPY.display(0.2f * getCount(), 1, entity.getLocation().clone().add(0, 0.5, 0).clone().add(Vector.getRandom().subtract(Vector.getRandom())), 16);
                }
            }
        }

        if (Config.ENTITY_STACKER_SHOW_NAME_TAG && Capability.ENTITY_NAMES.isCapable()) {
            if (getCount() < 2) {
                destroy();
            } else {
                String tag = Config.ENTITY_STACKER_NAME_TAG_FORMAT.replace("%size%", getCount() + "").replace("%type%", F.capitalizeWords(entity.getType().toString().toLowerCase().replaceAll("_", " "))).replace("%hp%", F.f(entity.getHealth() / 2.0, 1) + " \u2665");
                entity.setCustomName(color(tag));
            }
        }
    }

    public void setDamager(Entity e) {
        damager = e;
    }

    private void updateHealth() {
        if (count > getEffectiveCount()) {
            int grow = count - getEffectiveCount();
            double heal = getEffectiveMaxHealth(grow);
            setMaxHealth(getEffectiveMaxHealth(count));
            heal(heal);
        }

        if (getEffectiveCount() > getEffectiveCount(getHealth()) && getCount() > 1) {
            int oc = count;
            count = getEffectiveCount(getHealth());
            int diff = oc - count;

            if (diff > 1) {
                diff--;
            }

            for (int i = 0; i < diff; i++) {
                LivingEntity ex = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());

                if (Capability.ENTITY_AI.isCapable()) {
                    ex.setAI(false);
                } else {
                    PE.SLOW.a(10).d(100).apply(ex);
                }

                ex.setFireTicks(entity.getFireTicks());

                if (entity instanceof Ageable) {
                    ((Ageable) ex).setAge(((Ageable) entity).getAge());
                }

                if (entity instanceof Colorable) {
                    ((Colorable) ex).setColor(((Colorable) entity).getColor());
                }

                if (damager == null) {
                    ex.damage(ex.getHealth() + 1000);
                } else {
                    ex.damage(ex.getHealth() + 1000, damager);
                }

                ex.remove();
            }

            setMaxHealth(getEffectiveMaxHealth(count));
        }

        if (getCount() > Config.ENTITY_STACK_MAX_COUNT) {
            count = Config.ENTITY_STACK_MAX_COUNT;
        }

        if (getMaxHealth() > getEffectiveMaxHealth(getCount())) {
            if (getHealth() > getEffectiveMaxHealth(getCount())) {
                setHealth(getEffectiveMaxHealth(getCount()));
            }

            setMaxHealth(getEffectiveMaxHealth(getCount()));
        }
    }

    public void heal(double amt) {
        setHealth(getHealth() + amt);
    }

    public int getEffectiveCount() {
        return getEffectiveCount(getMaxHealth());
    }

    public int getEffectiveCount(double hp) {
        return (int) Math.ceil((hp / getRealMaxHealth()));
    }

    public double getEffectiveMaxHealth(int count) {
        return getRealMaxHealth() * count;
    }

    public double getRealMaxHealth() {
        return rmx;
    }

    public double getHealth() {
        return entity.getHealth();
    }

    public void setHealth(double hp) {
        hp = M.clip(hp, 1, 2000);
        entity.setHealth(Math.min(getMaxHealth(), hp));
    }

    public double getMaxHealth() {
        return getMaxHealth(entity);
    }

    public void setMaxHealth(double max) {
        if (getHealth() > max) {
            setHealth(max);
        }

        setMaxHealth(entity, max);
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public int getCount() {
        return count;
    }

    public boolean isValid() {
        return this.entity != null && this.entity.isValid();
    }

    public boolean isEntity(Entity e) {
        return e.equals(this.entity);
    }

}
