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
}
