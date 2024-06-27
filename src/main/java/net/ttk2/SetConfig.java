package net.ttk2;

import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;

public class SetConfig {
    public static String host, database, username, password,server,discord_webhook_url;
    public static int port;
    public static FileConfiguration config;
    public SetConfig(FileConfiguration config) {
    	if(Objects.nonNull(config)) {
            host = config.getString("host");
            port = config.getInt("port");
            database = config.getString("database");
            username = config.getString("user");
            password = config.getString("pass");
            server = config.getString("server");
            discord_webhook_url = config.getString("discord_webhook_url");
    	}

    }
}
