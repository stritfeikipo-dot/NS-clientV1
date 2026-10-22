package com.nsclient.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nsclient.module.HudModule;
import com.nsclient.module.Module;
import com.nsclient.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Persists every module's enabled state, settings, HUD position/scale and keybind.
 * The active config lives at config/nsclient/config.json; named profiles (saved/loaded
 * from the Configs page) live under config/nsclient/profiles/<name>.json.
 */
public class ConfigManager {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("nsclient");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    private static final Path PROFILES_DIR = CONFIG_DIR.resolve("profiles");

    public static void save(ModuleManager manager) {
        writeToFile(manager, CONFIG_FILE);
    }

    public static void load(ModuleManager manager) {
        readFromFile(manager, CONFIG_FILE);
    }

    public static void saveProfile(ModuleManager manager, String name) {
        writeToFile(manager, profilePath(name));
    }

    public static void loadProfile(ModuleManager manager, String name) {
        readFromFile(manager, profilePath(name));
    }

    public static boolean deleteProfile(String name) {
        try {
            return Files.deleteIfExists(profilePath(name));
        } catch (IOException e) {
            System.err.println("[NS Client] Failed to delete profile: " + e.getMessage());
            return false;
        }
    }

    /** Alphabetically sorted list of saved profile names (without the .json extension). */
    public static List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(PROFILES_DIR)) return names;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(PROFILES_DIR, "*.json")) {
            for (Path p : stream) {
                String fileName = p.getFileName().toString();
                names.add(fileName.substring(0, fileName.length() - 5));
            }
        } catch (IOException e) {
            System.err.println("[NS Client] Failed to list profiles: " + e.getMessage());
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    private static Path profilePath(String name) {
        String safe = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return PROFILES_DIR.resolve(safe + ".json");
    }

    private static void writeToFile(ModuleManager manager, Path file) {
        try {
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            JsonObject modulesObj = new JsonObject();
            for (Module m : manager.getModules()) {
                JsonObject obj = new JsonObject();
                m.writeConfig(obj);
                modulesObj.add(m.getName(), obj);
            }
            root.add("modules", modulesObj);
            root.add("hud_groups", manager.getGroupManager().toJson());

            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
        } catch (IOException e) {
            System.err.println("[NS Client] Failed to save config: " + e.getMessage());
        }
    }

    private static void readFromFile(ModuleManager manager, Path file) {
        if (!Files.exists(file)) return;
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            if (!root.has("modules")) return;
            JsonObject modulesObj = root.getAsJsonObject("modules");
            for (Module m : manager.getModules()) {
                if (modulesObj.has(m.getName())) {
                    m.readConfig(modulesObj.getAsJsonObject(m.getName()));
                }
                if (m instanceof HudModule hm) hm.setGrouped(false); // reset; reapplied below if still grouped
            }

            JsonArray groupsArr = root.has("hud_groups") ? root.getAsJsonArray("hud_groups") : new JsonArray();
            manager.getGroupManager().fromJson(groupsArr, manager);
        } catch (IOException e) {
            System.err.println("[NS Client] Failed to load config: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[NS Client] Failed to parse config: " + e.getMessage());
        }
    }
}
