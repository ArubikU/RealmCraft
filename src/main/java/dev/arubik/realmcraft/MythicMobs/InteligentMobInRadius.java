package dev.arubik.realmcraft.MythicMobs;

import com.google.common.collect.Sets;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitEntityType;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.logging.MythicLogger.DebugLevel;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.targeters.IEntitySelector;
import io.lumine.mythic.core.utils.MythicUtil;
import io.lumine.mythic.core.utils.annotations.MythicTargeter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.bukkit.entity.Item;

@MythicTargeter(author = "Arubik", name = "InteligentMobInRadius", aliases = {
        "IIR" }, description = "Gets all mobs in a radius around the caster in a intelligent way.")
public class InteligentMobInRadius extends IEntitySelector {
    protected PlaceholderDouble xoffset;
    protected PlaceholderDouble yoffset;
    protected PlaceholderDouble zoffset;
    protected PlaceholderDouble forwardOffset;
    protected PlaceholderDouble sideOffset;
    protected PlaceholderDouble rotateX;
    protected PlaceholderDouble rotateY;
    protected PlaceholderDouble rotateZ;
    protected PlaceholderDouble length;
    protected PlaceholderDouble coordinateX;
    protected PlaceholderDouble coordinateY;
    protected PlaceholderDouble coordinateZ;
    protected PlaceholderFloat coordinateYaw;
    protected PlaceholderFloat coordinatePitch;
    protected boolean statics = false;
    protected boolean offsets = false;
    protected boolean advOffset = false;
    protected boolean rotated = false;
    protected boolean centered = false;
    private double radius;
    private HashSet<MythicMob> mmTypes = new HashSet();
    private HashSet<BukkitEntityType> meTypes = new HashSet();

    public InteligentMobInRadius(SkillExecutor manager, MythicLineConfig mlc) {
        super(manager, mlc);
        this.radius = mlc.getDouble(new String[] { "radius", "r" }, 5.0D);
        this.xoffset = mlc.getPlaceholderDouble(new String[] { "xoffset", "xo", "x" }, 0.0D, new String[0]);
        this.yoffset = mlc.getPlaceholderDouble(new String[] { "yoffset", "yo", "y" }, 0.0D, new String[0]);
        this.zoffset = mlc.getPlaceholderDouble(new String[] { "zoffset", "zo", "z" }, 0.0D, new String[0]);
        this.centered = mlc.getBoolean(new String[] { "blockCentered", "centered" }, false);
        this.forwardOffset = mlc.getPlaceholderDouble(new String[] { "forwardoffset", "foffset", "fo" }, 0.0D,
                new String[0]);
        this.sideOffset = mlc.getPlaceholderDouble(new String[] { "sideoffset", "soffset", "so" }, 0.0D, new String[0]);
        this.rotateX = mlc.getPlaceholderDouble(new String[] { "rotatex", "rotx" }, 0.0D, new String[0]);
        this.rotateY = mlc.getPlaceholderDouble(new String[] { "rotatey", "roty" }, 0.0D, new String[0]);
        this.rotateZ = mlc.getPlaceholderDouble(new String[] { "rotatez", "rotz" }, 0.0D, new String[0]);
        this.length = mlc.getPlaceholderDouble(new String[] { "length" }, 0.0D, new String[0]);
        this.coordinateX = mlc.getPlaceholderDouble(new String[] { "coordinatex", "cx" }, 0.0D, new String[0]);
        this.coordinateY = mlc.getPlaceholderDouble(new String[] { "coordinatey", "cy" }, 0.0D, new String[0]);
        this.coordinateZ = mlc.getPlaceholderDouble(new String[] { "coordinatez", "cz" }, 0.0D, new String[0]);
        this.coordinateYaw = mlc.getPlaceholderFloat(new String[] { "coordinateyaw", "cyaw" }, 0.0F, new String[0]);
        this.coordinatePitch = mlc.getPlaceholderFloat(new String[] { "coordinatepitch", "cpitch" }, 0.0F,
                new String[0]);
        String types = mlc.getString(new String[] { "types", "type", "t" }, "", new String[0]);
        String[] ss = types.split(",");
        this.getPlugin().getSkillManager().queueSecondPass(() -> {
            String[] var4 = ss;
            int var5 = ss.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String s = var4[var6];
                MythicMob mm = (MythicMob) MythicBukkit.inst().getMobManager().getMythicMob(s).orElseGet(() -> {
                    return null;
                });
                if (mm != null) {
                    this.mmTypes.add(mm);
                } else {
                    BukkitEntityType me = BukkitEntityType.getMythicEntity(s);
                    if (me != null) {
                        this.meTypes.add(me);
                    } else {
                        MythicLogger.errorTargeterConfig(manager.getTargeter(types), mlc,
                                "The 'type' attribute must be a valid MythicMob or MythicEntity type.");
                    }
                }
            }

            MythicLogger.debug(DebugLevel.MECHANIC,
                    "@MIR targeter loaded targeting " + (this.meTypes.size() + this.mmTypes.size()) + " types",
                    new Object[0]);
        });

        if (!this.xoffset.isStaticallyEqualTo(0.0D) || !this.yoffset.isStaticallyEqualTo(0.0D)
                || !this.zoffset.isStaticallyEqualTo(0.0D)) {
            this.offsets = true;
        }

        if (!this.forwardOffset.isStaticallyEqualTo(0.0D) || !this.sideOffset.isStaticallyEqualTo(0.0D)) {
            this.advOffset = true;
        }

    }

    public Collection<AbstractEntity> getEntities(SkillMetadata data) {
        SkillCaster am = data.getCaster();
        HashSet<AbstractEntity> targets = Sets.newHashSet();
        AbstractLocation location = am.getLocation();

        if (this.centered) {
            double bX;
            double cY;
            bX = (double) location.getBlockX();
            cY = (double) location.getBlockZ();
            location.setX(bX + 0.5D);
            location.setZ(cY + 0.5D);
        }

        if (this.offsets) {
            location = location.clone().add(this.xoffset.get(data), this.yoffset.get(data), this.zoffset.get(data));
        }

        if (this.advOffset) {
            location = MythicUtil.move(location, this.forwardOffset.get(data), 0.0D, this.sideOffset.get(data));
        }

        Iterator var4 = this.getPlugin().getVolatileCodeHandler().getWorldHandler()
                .getEntitiesNearLocation(location, this.radius, (ae) -> {
                    if (this.mmTypes.isEmpty() && this.meTypes.isEmpty()) {
                        return true;
                    } else {
                        if (ae.getBukkitEntity() instanceof Item) {
                            return false;
                        } else {
                            if (MythicBukkit.inst().getMobManager().getActiveMob(ae.getUniqueId()).isPresent()) {
                                return this.mmTypes.contains(MythicBukkit.inst().getMobManager()
                                        .getActiveMob(ae.getUniqueId()).get().getType());
                            } else {
                                return this.meTypes.contains(ae.getBukkitEntity().getType());
                            }
                        }
                    }
                }).iterator();

        while (var4.hasNext()) {
            AbstractEntity p = (AbstractEntity) var4.next();
            // if (this.distanceSquared(am.getEntity().getLocation(), p) <
            // Math.pow(this.radius, 2.0D)) {
            if (p.getLocation().distance(location) < this.radius) {
                targets.add(p);
            }
            // }
        }

        return targets;
    }
}
