package dev.arubik.realmcraft.MythicMobs;

import com.google.common.collect.Sets;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.targeters.IEntitySelector;
import io.lumine.mythic.core.utils.MythicUtil;
import io.lumine.mythic.core.utils.annotations.MythicTargeter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.bukkit.entity.Item;

@MythicTargeter(author = "Arubik", name = "InteligentItemInRadius", aliases = {
        "IIR" }, description = "Gets all items in a radius around the caster in a intelligent way.")
public class InteligentItemInRadius extends IEntitySelector {
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

    public InteligentItemInRadius(SkillExecutor manager, MythicLineConfig mlc) {
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
                    return ae.getBukkitEntity() instanceof Item;
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
