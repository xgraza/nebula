package cope.nebula.client.configs.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cope.nebula.client.configs.AbstractConfig;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.manager.ModuleManager;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.fs.FileUtil;

public class ModuleConfig extends AbstractConfig {
    public ModuleConfig() {
        super("modules.json");
    }

    @Override
    public void load(String data) {
        try {
            JsonObject json = new Gson().fromJson(data, JsonObject.class);
            ModuleManager moduleManager = getNebula().getModuleManager();

            json.entrySet().forEach((entry) -> {
                Module module = moduleManager.getModule(entry.getKey());
                if (module == null) {
                    return;
                }

                JsonObject obj = entry.getValue().getAsJsonObject();

                if (obj.has("state")) {
                    module.setState(obj.get("state").getAsBoolean());
                }

                if (obj.has("settings")) {
                    JsonObject settings = obj.getAsJsonObject("settings");
                    settings.entrySet().forEach((e) -> {
                        Value value = module.getValue(e.getKey());
                        if (value == null) {
                            System.out.println("ssss");
                            return;
                        }

                        JsonElement element = e.getValue();
                        if (value.getValue() instanceof String) {
                            value.setValue(element.getAsString());
                        } else if (value.getValue() instanceof Enum) {
                            value.setValue(Enum.valueOf(((Enum) value.getValue()).getClass(), element.getAsString()));
                        } else if (value.getValue() instanceof Boolean) {
                            value.setValue(element.getAsBoolean());
                        } else if (value.getValue() instanceof Integer) {
                            value.setValue(element.getAsInt());
                        } else if (value.getValue() instanceof Double) {
                            value.setValue(element.getAsDouble());
                        } else if (value.getValue() instanceof Float) {
                            value.setValue(element.getAsFloat());
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        JsonObject data = new JsonObject();

        getNebula().getModuleManager().getModules().forEach((module) -> {
            JsonObject modData = new JsonObject();
            modData.addProperty("state", module.isOn());

            JsonObject settings = new JsonObject();
            module.getValues().forEach((value) -> {
                if (value.getValue() instanceof String) {
                    settings.addProperty(value.getName(), (String) value.getValue());
                } else if (value.getValue() instanceof Enum) {
                    settings.addProperty(value.getName(), ((Enum) value.getValue()).name());
                } else if (value.getValue() instanceof Boolean) {
                    settings.addProperty(value.getName(), (boolean) value.getValue());
                } else if (value.getValue() instanceof Integer) {
                    settings.addProperty(value.getName(), (int) value.getValue());
                } else if (value.getValue() instanceof Double) {
                    settings.addProperty(value.getName(), (double) value.getValue());
                } else if (value.getValue() instanceof Float) {
                    settings.addProperty(value.getName(), (float) value.getValue());
                }
            });

            modData.add("settings", settings);
            data.add(module.getName(), modData);
        });

        FileUtil.touch(getFilePath(), data.toString());
    }
}
