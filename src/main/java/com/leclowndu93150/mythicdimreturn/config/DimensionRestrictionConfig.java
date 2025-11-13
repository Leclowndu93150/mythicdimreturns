package com.leclowndu93150.mythicdimreturn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DimensionRestrictionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "config/mythicdimreturn.json";
    
    public List<DimensionRestriction> restrictions = new ArrayList<>();
    public List<DimensionGravity> dimensionGravity = new ArrayList<>();
    public List<DoubleJumpConfig> doubleJump = new ArrayList<>();
    
    public static class DimensionRestriction {
        public String dimensionId;
        public String requiredPermission;
        public String warningMessage = "&cYou don't have permission to be in this dimension! Teleporting in {countdown} seconds...";
        public int maxWarnings = 3;
        public int countdownSeconds = 3;
        public TeleportLocation teleportLocation;
        
        public static class TeleportLocation {
            public String dimension;
            public double x;
            public double y;
            public double z;
            public float yaw = 0.0f;
            public float pitch = 0.0f;
        }
        
        public ResourceKey<Level> getDimensionKey() {
            return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionId));
        }
        
        public ResourceKey<Level> getTeleportDimensionKey() {
            return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(teleportLocation.dimension));
        }
    }
    
    public static DimensionRestrictionConfig load() {
        File file = new File(CONFIG_FILE);
        
        if (!file.exists()) {
            DimensionRestrictionConfig config = createDefault();
            config.save();
            return config;
        }
        
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, DimensionRestrictionConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return createDefault();
        }
    }
    
    public void save() {
        File file = new File(CONFIG_FILE);
        file.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static DimensionRestrictionConfig createDefault() {
        DimensionRestrictionConfig config = new DimensionRestrictionConfig();
        
        DimensionRestriction example = new DimensionRestriction();
        example.dimensionId = "minecraft:the_nether";
        example.requiredPermission = "mythicdimreturn.dimension.nether";
        example.warningMessage = "&cYou don't have permission to be in the Nether! Teleporting in {countdown} seconds...";
        example.maxWarnings = 3;
        example.countdownSeconds = 3;
        
        DimensionRestriction.TeleportLocation tpLoc = new DimensionRestriction.TeleportLocation();
        tpLoc.dimension = "minecraft:overworld";
        tpLoc.x = 0;
        tpLoc.y = 64;
        tpLoc.z = 0;
        tpLoc.yaw = 0;
        tpLoc.pitch = 0;
        example.teleportLocation = tpLoc;
        
        config.restrictions.add(example);
        
        DimensionGravity gravityExample = new DimensionGravity();
        gravityExample.dimensionId = "minecraft:the_end";
        gravityExample.gravityModifier = -0.5;
        
        config.dimensionGravity.add(gravityExample);
        
        DoubleJumpConfig djPermissionExample = new DoubleJumpConfig();
        djPermissionExample.dimensionId = "minecraft:overworld";
        djPermissionExample.maxDoubleJumps = 2;
        djPermissionExample.cooldownSeconds = 1;
        djPermissionExample.jumpMessage = "&aDouble Jump! &7[{remaining}/{max}]";
        djPermissionExample.refill.secondsPerRefill = 60;
        djPermissionExample.refill.maxAutoRefill = 1;
        
        DoubleJumpConfig.AccessRule permRule = new DoubleJumpConfig.AccessRule();
        permRule.permission = "mythicdimreturn.doublejump.overworld";
        djPermissionExample.accessRules.add(permRule);
        
        config.doubleJump.add(djPermissionExample);
        
        DoubleJumpConfig djArmorExample = new DoubleJumpConfig();
        djArmorExample.dimensionId = "minecraft:the_nether";
        djArmorExample.maxDoubleJumps = 3;
        djArmorExample.cooldownSeconds = 2;
        djArmorExample.jumpMessage = "&6Nether Jump! &c[{remaining}/{max}]";
        djArmorExample.refill.secondsPerRefill = 45;
        djArmorExample.refill.maxAutoRefill = 2;
        
        DoubleJumpConfig.AccessRule armorRule = new DoubleJumpConfig.AccessRule();
        armorRule.armorRequirement = new DoubleJumpConfig.AccessRule.ArmorRequirement();
        armorRule.armorRequirement.helmet = "minecraft:netherite_helmet";
        armorRule.armorRequirement.chestplate = "minecraft:netherite_chestplate";
        armorRule.armorRequirement.leggings = "";
        armorRule.armorRequirement.boots = "";
        djArmorExample.accessRules.add(armorRule);
        
        config.doubleJump.add(djArmorExample);
        
        DoubleJumpConfig djMixedExample = new DoubleJumpConfig();
        djMixedExample.dimensionId = "minecraft:the_end";
        djMixedExample.maxDoubleJumps = 5;
        djMixedExample.cooldownSeconds = 0;
        djMixedExample.jumpMessage = "&dEnd Jump! &5[{remaining}/{max}]";
        djMixedExample.refill.secondsPerRefill = 30;
        djMixedExample.refill.maxAutoRefill = 3;
        
        DoubleJumpConfig.AccessRule mixedPermRule = new DoubleJumpConfig.AccessRule();
        mixedPermRule.permission = "mythicdimreturn.doublejump.end";
        djMixedExample.accessRules.add(mixedPermRule);
        
        DoubleJumpConfig.AccessRule mixedArmorRule = new DoubleJumpConfig.AccessRule();
        mixedArmorRule.armorRequirement = new DoubleJumpConfig.AccessRule.ArmorRequirement();
        mixedArmorRule.armorRequirement.helmet = "";
        mixedArmorRule.armorRequirement.chestplate = "";
        mixedArmorRule.armorRequirement.leggings = "";
        mixedArmorRule.armorRequirement.boots = "minecraft:elytra";
        djMixedExample.accessRules.add(mixedArmorRule);
        
        config.doubleJump.add(djMixedExample);
        
        return config;
    }
    
    public DimensionRestriction getRestriction(ResourceKey<Level> dimension) {
        for (DimensionRestriction restriction : restrictions) {
            if (restriction.getDimensionKey().equals(dimension)) {
                return restriction;
            }
        }
        return null;
    }
    
    public static class DimensionGravity {
        public String dimensionId;
        public double gravityModifier;
        
        public ResourceKey<Level> getDimensionKey() {
            return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionId));
        }
    }
    
    public DimensionGravity getDimensionGravity(ResourceKey<Level> dimension) {
        for (DimensionGravity gravity : dimensionGravity) {
            if (gravity.getDimensionKey().equals(dimension)) {
                return gravity;
            }
        }
        return null;
    }
    
    public static class DoubleJumpConfig {
        public String dimensionId;
        public int maxDoubleJumps = 2;
        public int cooldownSeconds = 1;
        public String jumpMessage = "&aDouble Jump! &7[{remaining}/{max}]";
        public RefillSettings refill = new RefillSettings();
        public List<AccessRule> accessRules = new ArrayList<>();
        
        public static class RefillSettings {
            public int secondsPerRefill = 60;
            public int maxAutoRefill = 1;
        }
        
        public static class AccessRule {
            public String permission = "";
            public ArmorRequirement armorRequirement = null;
            
            public static class ArmorRequirement {
                public String helmet = "";
                public String chestplate = "";
                public String leggings = "";
                public String boots = "";
            }
        }
        
        public ResourceKey<Level> getDimensionKey() {
            return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionId));
        }
    }
    
    public DoubleJumpConfig getDoubleJumpConfig(ResourceKey<Level> dimension) {
        for (DoubleJumpConfig config : doubleJump) {
            if (config.getDimensionKey().equals(dimension)) {
                return config;
            }
        }
        return null;
    }
}
