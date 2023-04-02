package dev.arubik.realmcraft.EmoteHandler;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.comphenix.net.bytebuddy.build.Plugin;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Api.RealStack;
import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Managers.Depend;
import lombok.Getter;
import xyz.larkyy.aquaticmodelengine.api.AquaticModelEngineAPI;
import xyz.larkyy.aquaticmodelengine.api.event.EmoteAnimationStateChangeEvent;
import xyz.larkyy.aquaticmodelengine.api.event.EmoteEndEvent;
import xyz.larkyy.aquaticmodelengine.api.model.animation.LoopMode;
import xyz.larkyy.aquaticmodelengine.api.model.animation.PlayerAnimationHandlerImpl;
import xyz.larkyy.aquaticmodelengine.api.model.animation.TemplateAnimation;
import xyz.larkyy.aquaticmodelengine.api.model.holder.ModelHolder;
import xyz.larkyy.aquaticmodelengine.api.model.holder.impl.EntityModelHolder;
import xyz.larkyy.aquaticmodelengine.api.model.holder.impl.PlayerModelHolder;
import xyz.larkyy.aquaticmodelengine.api.model.spawned.ModelBone;
import xyz.larkyy.aquaticmodelengine.api.model.spawned.player.PlayerModel;
import xyz.larkyy.aquaticmodelengine.api.model.template.ModelTemplate;
import xyz.larkyy.aquaticmodelengine.api.model.template.ModelTemplateImpl;
import xyz.larkyy.aquaticmodelengine.api.model.template.TemplateBone;
import xyz.larkyy.aquaticmodelengine.api.model.template.TemplateBoneImpl;
import xyz.larkyy.aquaticmodelengine.generator.blockbench.BlockBenchParser;
import xyz.larkyy.aquaticmodelengine.generator.java.JavaBaseItem;
import xyz.larkyy.aquaticmodelengine.model.player.PlayerModelImpl;

public class EmoteMain implements Listener, Depend {

    private static Map<UUID, String> UserLastEmote = new HashMap<UUID, String>();

    @EventHandler
    public void onEmoteEndEvent(EmoteEndEvent event) {
        if (event.getPlayerModel().getModelHolder() instanceof PlayerModelHolder) {
            PlayerModelHolder holder = (PlayerModelHolder) event.getPlayerModel().getModelHolder();
            holder.getPlayer().setInvisible(false);
            // realmcraft.getEntityHider().removeForceInvisible((Entity)
            // holder.getPlayer());
        } else {
            ModelHolder holder = event.getPlayerModel().getModelHolder();
            Object a = event.getPlayerModel().getAnimationHandler()
                    .getAnimationState();
            if (a.toString().contains("PLAY") || a.toString().contains("PR")
                    || event.getPlayerModel().getAnimationHandler().isPlaying()) {
                Class<?> clazz = holder.getClass();
                Field field;
                try {
                    field = clazz.getField("boundEntity");
                    Entity entity = (Entity) field.get(holder);
                    realmcraft.getEntityHider().removeForceInvisible(entity);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                        | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setInvisible(Player p) {
        p.setInvisible(true);
    }

    public void setInvisible(Entity e) {
        realmcraft.getEntityHider().addForceInvisible(e);
    }

    public void setVisible(Player p) {
        p.setInvisible(false);
    }

    public void setVisible(Entity e) {
        realmcraft.getEntityHider().removeForceInvisible(e);
    }

    @EventHandler
    public void onEmoteAnimationStateChangeEvent(EmoteAnimationStateChangeEvent event) {
        if (event.getPlayerModel().getModelHolder() instanceof PlayerModelHolder) {
            PlayerModelHolder holder = (PlayerModelHolder) event.getPlayerModel().getModelHolder();
            Object a = event.getPlayerModel().getAnimationHandler()
                    .getAnimationState();
            if (a.toString().contains("PLAY") || a.toString().contains("PR")
                    || event.getPlayerModel().getAnimationHandler().isPlaying()) {
                setInvisible(holder.getPlayer());
                // realmcraft.getEntityHider().addForceInvisible(holder.getPlayer());
            }
        } else {
            ModelHolder holder = event.getPlayerModel().getModelHolder();
            Object a = event.getPlayerModel().getAnimationHandler()
                    .getAnimationState();
            if (a.toString().contains("PLAY") || a.toString().contains("PR")
                    || event.getPlayerModel().getAnimationHandler().isPlaying()) {
                Class<?> clazz = holder.getClass();
                Field field;
                try {
                    field = clazz.getField("boundEntity");
                    Entity entity = (Entity) field.get(holder);
                    realmcraft.getEntityHider().addForceInvisible(entity);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                        | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (AquaticModelEngineAPI.getModelHandler().getModelHolder(player) instanceof PlayerModelHolder holder) {
            if (holder.getEmote() != null) {
                if (holder.getEmote().getAnimationHandler().isPlaying()) {
                    setInvisible(holder.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (AquaticModelEngineAPI.getModelHandler().getModelHolder(player) instanceof PlayerModelHolder holder) {
            if (holder.getEmote() != null) {
                if (holder.getEmote().getAnimationHandler().isPlaying()) {
                    holder.getPlayer().setInvisible(false);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (AquaticModelEngineAPI.getModelHandler().getModelHolder(player) instanceof PlayerModelHolder holder) {
            if (holder.getEmote() != null) {
                if (holder.getEmote().getAnimationHandler().isPlaying()) {
                    setInvisible(holder.getPlayer());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (AquaticModelEngineAPI.getModelHandler().getModelHolder(player) instanceof PlayerModelHolder holder) {
            if (holder.getEmote() != null) {
                if (UserLastEmote.containsKey(player.getUniqueId())) {
                    String emote = UserLastEmote.get(player.getUniqueId());
                    String canmove = realmcraft.getInteractiveConfig()
                            .getString("emoteconfig." + emote + ".cancelmove", "true");
                    String canheadmove = realmcraft.getInteractiveConfig()
                            .getString("emoteconfig." + emote + ".canheadmove", "true");
                    if (canmove.equalsIgnoreCase("true")) {
                        if (holder.getEmote().getAnimationHandler().isPlaying()) {
                            event.setCancelled(true);
                        }
                    }
                    if (canheadmove.equalsIgnoreCase("true")) {
                        if (holder.getEmote().getAnimationHandler().isPlaying()) {
                            if (event.getFrom().getX() == event.getTo().getX()
                                    && event.getFrom().getZ() == event.getTo().getZ()
                                    && event.getFrom().getY() == event.getTo().getY()) {
                                event.setCancelled(false);
                            }
                        }
                    }
                }
            }
        }
    }

    // if cancel on shift
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (AquaticModelEngineAPI.getModelHandler().getModelHolder(player) instanceof PlayerModelHolder holder) {
            if (holder.getEmote() != null) {
                if (UserLastEmote.containsKey(player.getUniqueId())) {
                    String emote = UserLastEmote.get(player.getUniqueId());
                    String cansneak = realmcraft.getInteractiveConfig()
                            .getString("emoteconfig." + emote + ".cancelsneak", "true");
                    if (cansneak.equalsIgnoreCase("true")) {
                        if (holder.getEmote().getAnimationHandler().isPlaying()) {
                            event.setCancelled(true);
                            EmoteMain.cancelEmote(player);
                        }
                    }
                }
            }
        }
    }

    public static void Register() {
        EmoteMain emoteMain = new EmoteMain();
        if (Depend.isPluginEnabled(emoteMain)) {
            Bukkit.getPluginManager().registerEvents(emoteMain, realmcraft.getInstance());
            LoadModelTemplates();
        } else {
            RealMessage.nonFound("AquaticModelEngine not found");
        }
    }

    private static realmcraft plugin = realmcraft.getInstance();

    private static Map<String, ModelTemplateImpl> modelTemplates = new HashMap<>();
    @Getter
    private static Set<String> emotes = Set.of("stop");

    public static void LoadModelTemplates() {
        modelTemplates.clear();
        for (File file : new File(plugin.getDataFolder() + "/emotes/").listFiles()) {
            if (file.getName().endsWith(".bbmodel")) {
                ModelTemplateImpl modelTemplate = new BlockBenchParser().generateEmote(file, new JavaBaseItem());
                modelTemplates.put(file.getName().replace(".bbmodel", ""), modelTemplate);
            }
        }
        genAnimations();
    }

    public static PlayerModel playAnimation(Player player, String animation, Boolean head) {
        ModelHolder modelHolder = EmoteMain.cancelEmote(player);
        ModelTemplateImpl modelTemplate = modelTemplates.get("elite");
        for (ModelTemplateImpl modelTemplate2 : modelTemplates.values()) {
            if (modelTemplate2.getAnimations().keySet().contains(animation)) {
                modelTemplate = modelTemplate2;
                break;
            }
        }
        PlayerModel model = AquaticModelEngineAPI.getModelHandler().spawnEmote(modelHolder, player, modelTemplate, null,
                animation, null, head);
        UserLastEmote.put(player.getUniqueId(), animation);
        return model;
    }

    public static PlayerModel playAnimationURL(Player player, String animation, Boolean head, String url) {
        ModelHolder modelHolder = EmoteMain.cancelEmote(player);
        ModelTemplateImpl modelTemplate = modelTemplates.get("elite");
        for (ModelTemplateImpl modelTemplate2 : modelTemplates.values()) {
            if (modelTemplate2.getAnimations().keySet().contains(animation)) {
                modelTemplate = modelTemplate2;
                break;
            }
        }
        PlayerModel model = AquaticModelEngineAPI.getModelHandler().spawnEmote(modelHolder, url, false, modelTemplate,
                null, animation, null, false);
        UserLastEmote.put(player.getUniqueId(), animation);
        return model;
    }

    public static PlayerModel playAnimationMOB(Entity entity, String animation, Boolean head, String url, Boolean slim,
            LoopMode loop) {
        ModelHolder modelHolder = EmoteMain.cancelEmote(entity);
        ModelTemplateImpl modelTemplate = modelTemplates.get("elite");
        for (ModelTemplateImpl modelTemplate2 : modelTemplates.values()) {
            if (modelTemplate2.getAnimations().keySet().contains(animation)) {
                modelTemplate = modelTemplate2;
                break;
            }
        }
        TemplateAnimation anim = modelTemplate.getAnimation(animation);
        anim.setLoopMode(loop);
        anim.setOverride(true);
        PlayerModel model = new PlayerModelImpl(modelTemplate, modelHolder, url, slim, null, anim, null, head);
        modelHolder.addModel(model);
        model.show();
        return model;
    }

    public static PlayerModel playAnimationMOBEndIdle(Entity entity, String animation, Boolean head, String url,
            Boolean slim, LoopMode loop) {
        ModelHolder modelHolder = EmoteMain.cancelEmote(entity);
        ModelTemplateImpl modelTemplate = modelTemplates.get("elite");
        for (ModelTemplateImpl modelTemplate2 : modelTemplates.values()) {
            if (modelTemplate2.getAnimations().keySet().contains(animation)) {
                modelTemplate = modelTemplate2;
                break;
            }
        }
        TemplateAnimation anim = modelTemplate.getAnimation(animation);
        anim.setLoopMode(loop);
        anim.setOverride(true);
        if (modelTemplate.getAnimations().keySet().contains("idle")) {

            TemplateAnimation idle = modelTemplate.getAnimation("idle");
            idle.setLoopMode(LoopMode.LOOP);
            idle.setOverride(true);
            PlayerModel model = new PlayerModelImpl(modelTemplate, modelHolder, url, slim, null, anim, idle, head);
            modelHolder.addModel(model);
            model.show();
            return model;
        } else {

            PlayerModel model = new PlayerModelImpl(modelTemplate, modelHolder, url, slim, null, anim, null, head);
            modelHolder.addModel(model);
            model.show();
            return model;
        }
    }

    public static void AttachModel(PlayerModel model, String bone, RealStack stack) {
        ModelBone Mbone = model.getBone(bone);

        ModelTemplateImpl modelTemplate = new ModelTemplateImpl(bone);
        try {
            Class<?> clazz = modelTemplate.getClass();
            // debug all the fields
            for (Field field : clazz.getSuperclass().getFields()) {
                RealMessage.sendConsoleMessage(field.getName());
            }
            Field bones = clazz.getField("bones");
            bones.setAccessible(true);
            List<TemplateBone> bonesList = new ArrayList<>();
            TemplateBone bone1 = new TemplateBoneImpl("templ", new Vector(), new EulerAngle(0, 0, 0));
            bone1.setModelId(stack.getModelid());
            Class<?> clazz1 = bone1.getClass().getSuperclass();
            Field item = clazz1.getField("material");
            item.setAccessible(true);
            item.set(bone1, stack.getMaterial());
            bonesList.add(bone1);
            bones.set(modelTemplate, bonesList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AquaticModelEngineAPI.getModelHandler().attachModel(Mbone, modelTemplate);
    }

    public static Set<String> genAnimations() {
        Set<String> emotesb = new HashSet<>();
        emotesb.add("stop");
        for (ModelTemplateImpl modelTemplate : modelTemplates.values()) {
            emotesb.addAll(modelTemplate.getAnimations().keySet());
        }

        emotes = emotesb;
        return emotesb;
    }

    public static PlayerModelHolder cancelEmote(Player player) {
        PlayerModelHolder modelHolder = (PlayerModelHolder) AquaticModelEngineAPI.getModelHandler()
                .getModelHolder(player);
        if (modelHolder.getEmote() != null) {
            modelHolder.getEmote().getAnimationHandler().stopAnimation();
            return (PlayerModelHolder) AquaticModelEngineAPI.getModelHandler().getModelHolder(player);
        }

        return modelHolder;
    }

    public static ModelHolder cancelEmote(Entity entity) {
        ModelHolder modelHolder = AquaticModelEngineAPI.getModelHandler()
                .getModelHolder(entity);
        if (modelHolder.getEmote() != null) {
            modelHolder.getEmote().getAnimationHandler().stopAnimation();
            return AquaticModelEngineAPI.getModelHandler().getModelHolder(entity);
        }

        return modelHolder;
    }

    @Override
    public String[] getDependatsPlugins() {
        return new String[] { "AquaticModelEngine" };
    }
}
