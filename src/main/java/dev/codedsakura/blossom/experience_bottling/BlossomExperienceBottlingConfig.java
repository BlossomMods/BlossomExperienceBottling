package dev.codedsakura.blossom.experience_bottling;


import javax.annotation.Nullable;

public class BlossomExperienceBottlingConfig {
    int langDescriptionLines = 2;
    ItemColors itemColors = new ItemColors();

    public float xpDropOnDeathMultiplier = .5F;

    @Nullable
    Sound bottlingSound = new Sound("minecraft:block.brewing_stand.brew");
    @Nullable
    public Sound usageSound = new Sound("minecraft:entity.experience_orb.pickup");

    static class ItemColors {
        String title = "yellow";
        String description = "gray";
    }

    public static class Sound {
        Sound(String id) {
            identifier = id;
        }

        public String identifier;
        public float volume = 1;
        public float pitch = 1;
    }
}
