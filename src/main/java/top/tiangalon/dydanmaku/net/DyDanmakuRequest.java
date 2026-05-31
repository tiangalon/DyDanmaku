package top.tiangalon.dydanmaku.net;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.util.EntityUtils;
import top.tiangalon.dydanmaku.client.DyDanmakuClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static top.tiangalon.dydanmaku.client.DyDanmakuClient.ConfigDirPath;
import static top.tiangalon.dydanmaku.client.DyDanmakuClient.LOGGER;


public class DyDanmakuRequest {

    RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();

    public static String User_Agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    /**
     *  获取抖音ttwid
     * @param live_id
     * @return
     */
    public static Map<String, String> getParams(String live_id) {

        String url = "https://live.douyin.com/" + live_id;
        String ttwid = null;
        String roomId = null;
        String user_unique_id = null;
        String live_status = null;
        String live_title = null;
        String nickname = null;
        String avatar = null;
        Map<String, String> params = new HashMap<String, String>();
        CloseableHttpClient httpClient = HttpClients.createDefault();



        try {
            HttpGet httpGet = new HttpGet(url);
            RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
            httpGet.setHeader("User-Agent", User_Agent);
            httpGet.setHeader("cookie", "__ac_nonce=0" + GenerateToken(20)+ ";/=" +  "live.douyin.com");
            httpGet.setConfig(defaultConfig);
            CloseableHttpResponse response = httpClient.execute(httpGet);



            if(response != null){
                HttpEntity entity = response.getEntity();   // 获取网页内容
                String result = EntityUtils.toString(entity, "UTF-8");

                roomId = result.substring(result.lastIndexOf("roomId\\\":\\\"")+11, result.lastIndexOf("roomId\\\":\\\"") + 30);
                user_unique_id = result.substring(result.indexOf("\\\"user_unique_id\\\":\\\"")+21, result.indexOf("\\\"user_unique_id\\\":\\\"") + 40);
                live_status = result.substring(result.indexOf("\\\"status_str\\\":")+17, result.indexOf("\\\"status_str\\\":") + 18);
                String temp = result.substring(result.indexOf("\\\"status_str\\\":")+21);
                live_title = temp.substring(temp.indexOf("\\\"title\\\":\\\"")+12, temp.indexOf("\\\"title\\\":\\\"") + 100);
                live_title = live_title.substring(0, live_title.indexOf("\\"));
                nickname = temp.substring(temp.indexOf("\\\"nickname\\\":\\\"")+15, temp.indexOf("\\\"nickname\\\":\\\"") + 100);
                nickname = nickname.substring(0, nickname.indexOf("\\"));
                avatar = temp.substring(temp.indexOf("\\\"avatar_thumb\\\":{\\\"url_list\\\":[\\\"")+34, temp.indexOf("\\\"avatar_thumb\\\":{\\\"url_list\\\":[\\\"") + 250);
                avatar = avatar.substring(0, avatar.indexOf("\\"));
                params.put("live_id", live_id);
                params.put("roomId", roomId);
                params.put("user_unique_id", user_unique_id);
                params.put("live_status", live_status);
                params.put("live_title", live_title);
                params.put("nickname", nickname);
                params.put("avatar", avatar);



                Header responseHeader = response.getFirstHeader("Set-Cookie");
                HeaderElement[] responseHeaderElements = responseHeader.getElements();
                for (int i=0; i<responseHeaderElements.length; i++){
                    if ("ttwid".equals(responseHeaderElements[i].getName())){
                        ttwid = responseHeaderElements[i].getValue();
                    }
                }
                params.put("ttwid", ttwid);

            }
            return params;
        }catch (Exception e) {
            LOGGER.info("[DyDanmaku]getParams error:", e);
            return null;
        }
    }

    public static String GenerateToken(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789=_";
        int base_length = base.length();
        StringBuffer token = new StringBuffer();
        for (int i = 0; i < length; i++) {
            token.append(base.charAt((int) (Math.random() * base_length)));
        }
        return token.toString();
    }

    public static void DownloadAvatar(String url, String path) {
        File ConfigDir = new File(ConfigDirPath);
        if  (!ConfigDir.exists()  && !ConfigDir.isDirectory()) {
            LOGGER.info("[DyDanmaku]/config/DyDanmaku不存在,创建目录");
            ConfigDir.mkdirs();
        } else {
            LOGGER.info("[DyDanmaku]/config/DyDanmaku目录存在");
        }
        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP请求失败，响应码: " + responseCode);
            }

            // 生成PNG格式的文件路径
            File originalFile = new File(path);
            String parent = originalFile.getParent();
            String filename = originalFile.getName();
            int dotIndex = filename.lastIndexOf('.');
            String newName = (dotIndex == -1) ?
                    filename + ".png" :
                    filename.substring(0, dotIndex) + ".png";
            File pngFile = new File(parent, newName);

            // 确保目标目录存在
            File parentDir = pngFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 读取图像并转换格式
            try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    throw new IOException("无法解析图像数据");
                }
                // 缩放为50x50图像
                BufferedImage scaledImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = scaledImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(image, 0, 0, 50, 50, null);
                g.dispose();
                if (!ImageIO.write(scaledImage, "PNG", pngFile)) {
                    throw new IOException("不支持的PNG格式转换");
                }
                LOGGER.info("[DyDanmaku]DownloadAvatar成功，路径: " + pngFile.getPath());
                DyDanmakuClient.gui.avatar_register(pngFile.getPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
