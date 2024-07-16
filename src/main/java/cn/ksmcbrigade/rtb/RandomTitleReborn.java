package cn.ksmcbrigade.rtb;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.common.Mod;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mod("rtb")
public class RandomTitleReborn {

    public static final Logger LOGGER = LogManager.getLogger("RandomTitle");

    public static File configFile = new File("title.yml");

    public static Map<String, Object> config;
    public static Map<String, Object> defaultConfig;

    public static boolean init = false;

    public RandomTitleReborn() {
        if(!init){
            init();
        }
    }

    public static void init(){
        LOGGER.info("RandomTitleReborn by KSmc_brigade https://github.com/yiyuyan/RandomTitleReborn,this mod is the RandomTitle mod's reborn.");
        LOGGER.info("RandomTitle by PercyDan https://github.com/PercyDan54/RandomTitle");

        defaultConfig = new Yaml().load(RandomTitleReborn.class.getResourceAsStream("/title.yml"));

        if (!configFile.exists()) {
            LOGGER.info("Config file not found, creating default");
            createDefault();
        }

        try {
            config = new Yaml().load(new FileInputStream(configFile));

            init = true;
        } catch (Exception e) {
            LOGGER.error("Failed to load config!", e);
            config = defaultConfig;
        }
    }

    public static void createDefault() {
        InputStream inputStream = RandomTitleReborn.class.getResourceAsStream("/title.yml");

        try {
            if (!configFile.exists()) {
                OutputStream outputStream = new FileOutputStream("title.yml");
                byte[] b = new byte[1024];
                int len;

                while ((len = inputStream.read(b)) != -1) {
                    outputStream.write(b, 0, len);
                }
                outputStream.close();
            }
        } catch (Throwable e) {
            LOGGER.error("Failed to create default config! {}",e.getMessage(), e);
        }
    }

    public static <T> T Get(String key) {
        if(!init) init();
        T value = (T) config.get(key);
        if (value == null) return (T) defaultConfig.get(key);
        return value;
    }

    private static String getTitleFromList() {
        String title = "";

        try {
            List<String> titles = Get("title");
            title = titles.get(new Random().nextInt(titles.size()));
        } catch (Throwable e) {
            LOGGER.error("Failed to get title from config!", e);
        }

        return title;
    }

    private static String getTitleFromHitokoto() {
        LOGGER.info("Getting title from Hitokoto API.");

        String title;
        String response;

        try {
            response = EntityUtils.toString(HttpClients.createDefault().execute(new HttpGet("https://v1.hitokoto.cn/")).getEntity());
            LOGGER.info("Hitokoto Response String: " + response);
            JsonObject json = new JsonParser().parse(response).getAsJsonObject();
            String from = json.get("from").getAsString();
            String sentence = json.get("hitokoto").getAsString();
            String type = json.get("type").getAsString();
            title = sentence + "   —— ";

            switch (type) {
                case "e":
                    title += json.get("creator").getAsString() + " 原创";
                    break;
                case "f":
                    title += "来自网络";
                    break;
                default:
                    title += from;
            }

        } catch (Throwable e) {
            LOGGER.error("Failed to get title from API! {}",e.getMessage(), e);
            return getTitleFromList();
        }
        return title;
    }

    public static String getTitle() {
        int mode = Get("mode");

        switch (mode) {
            case 0:
                return getTitleFromHitokoto();
            case 1:
                return getTitleFromList();
            case 2:
                boolean use = new Random().nextBoolean();
                if (use) {
                    return getTitleFromList();
                } else {
                    return getTitleFromHitokoto();
                }
        }
        return getTitleFromList();
    }
}
