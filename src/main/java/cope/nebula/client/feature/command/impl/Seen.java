package cope.nebula.client.feature.command.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import cope.nebula.client.feature.command.Command;
import cope.nebula.util.internal.net.NetworkUtil;

public class Seen extends Command {
    public Seen() {
        super("seen", LiteralArgumentBuilder.literal("seen")
                .then(RequiredArgumentBuilder.argument("username", StringArgumentType.string())
                        .executes((ctx) -> {
                            String username = StringArgumentType.getString(ctx, "username");
                            if (username == null || username.isEmpty()) {
                                username = "";
                            }

                            String text = NetworkUtil.makeRequest("https://api.2b2t.dev/seen?username=" + username);
                            if (text == null || text.isEmpty() || text.equals("[]")) {
                                send("An exception occurred when retrieving the date, or this player has not been seen.");
                                return 0;
                            }

                            JsonArray array = new Gson().fromJson(text, JsonArray.class);
                            if (array.size() == 0) {
                                send("This player has not been seen.");
                                return 0;
                            }

                            JsonObject data = array.get(0).getAsJsonObject();
                            send(username + " has been last seen at " + data.get("seen").getAsString());

                            return 0;
                        }))
                .executes((ctx) -> {
                    send("Please provide a username");
                    return 0;
                }));
    }
}
