package DyDanmaku;

import java.io.*;
import java.util.Map;

public class Main {
    /**
     * 根据roomId和user_unique_id获取Signature，由Singature.exe生成
     * Signature.exe路径：/src/main/java/com/DyDanmaku/Signature.exe,由同目录下index.js打包生成
     * @param roomId 直播间id
     * @param user_unique_id 用户唯一id
     * @return Signature
     */
    public static String sign(String roomId, String user_unique_id) {
        String command = Main.class.getClassLoader().getResource("./Signature.exe").getPath() + " " + roomId + " " + user_unique_id;
        Process process = null;
        String signature = "";
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            signature = br.readLine();
            return signature;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (process!= null) {
                process.destroy();
            }
        }
        return signature;
    }

    public static void main(String[] args){
        //直播间网页id， 即直播间网址https://live.douyin.com/后面的数字部分
        String live_id = "485090914826";

        //从直播间网页相应中获取roomId, user_unique_id, ttwid
        String live_url = "https://live.douyin.com/" + live_id;
        String UserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        Map<String, String> params = myRequest.getParams(live_id);
        if (params != null) {
            String roomId = params.get("roomId");
            String user_unique_id = params.get("user_unique_id");
            String ttwid = params.get("ttwid");

            /*
             * 检查roomId, user_unique_id, ttwid是否正确
             * System.out.println("roomId: " + roomId);
             * System.out.println("user_unique_id: " + user_unique_id);
             * System.out.println("ttwid: " + ttwid);
             */

            /*
             * 根据roomId和user_unique_id获取Signature（目前无需Signature也可连接，暂时停用）
             * String signature = sign(roomId, user_unique_id);
             * System.out.println("Signature: " + signature);
             */


            WebSocketClient listener = new WebSocketClient();
            listener.connect(roomId, UserAgent, ttwid);
//            Scanner scanner = new Scanner(System.in);
//            if (scanner.next().equals("exit")) {
//                listener.close();
//                scanner.close();
//            }
            System.out.println("连接成功，等待10秒后关闭连接");
            try {
                Thread.sleep(10000);
                System.out.println("时间到，关闭连接");
                listener.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("无法获取参数");
        }
    }


}
