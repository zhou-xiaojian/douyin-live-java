package com.example.demo;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.demo.websocket.DyWebSocketClient;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DouyinDanMuTest {

    private static Pattern pattern = Pattern.compile("roomId\\\\\":\\\\\"(\\d+)\\\\\"");


    public static void main(String[] args) throws Exception{

        openRoom("166393852641");
    }

    public static void openRoom(String roomId) throws Exception{

        String url = "https://live.douyin.com/"+roomId;

        Map<String, String> headMap = new HashMap<>();
        headMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1");
        headMap.put("Content-Type", "text/plain;charset=UTF-8");
        headMap.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headMap.put("cookie", "__ac_nonce=0638733a400869171be51");

        HttpResponse response = HttpRequest
                .get(url)
                .headerMap(headMap, true)
                .execute();

        String ttwid = response.getCookieValue("ttwid");
        headMap.put("cookie", "ttwid="+ttwid);

        //获取liveRoomId
        String body = response.body();
        // 在 resText 中查找匹配项
        Matcher matcher = pattern.matcher(body);
        String liveRoomId = "";
        if (matcher.find() && matcher.groupCount() >= 1) {
            // 获取匹配的子字符串
            liveRoomId = matcher.group(1);
            System.out.println("Live room ID: " + liveRoomId);
        } else {
            System.out.println("No match found");
            return;
        }

        //获取wss链接
        String wssUrl = "http://webcast5-ws-web-lq.douyin.com/webcast/im/push/v2/?app_name=douyin_web&version_code=180800&webcast_sdk_version=1.3.0&update_version_code=1.3.0&compress=gzip&internal_ext=internal_src:dim|wss_push_room_id:%s|wss_push_did:%s|dim_log_id:202302171547011A160A7BAA76660E13ED|fetch_time:1676620021641|seq:1|wss_info:0-1676620021641-0-0|wrds_kvs:WebcastRoomStatsMessage-1676620020691146024_WebcastRoomRankMessage-1676619972726895075_AudienceGiftSyncData-1676619980834317696_HighlightContainerSyncData-2&cursor=t-1676620021641_r-1_d-1_u-1_h-1&host=https://live.douyin.com&aid=6383&live_id=1&did_rule=3&debug=false&endpoint=live_pc&support_wrds=1&im_path=/webcast/im/fetch/&user_unique_id=%s&device_platform=web&cookie_enabled=true&screen_width=1440&screen_height=900&browser_language=zh&browser_platform=MacIntel&browser_name=Mozilla&browser_version=5.0%20(Macintosh;%20Intel%20Mac%20OS%20X%2010_15_7)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/110.0.0.0%20Safari/537.36&browser_online=true&tz_name=Asia/Shanghai&identity=audience&room_id=%s&heartbeatDuration=0&signature=00000000";
        String socketUrl = wssUrl.replaceAll("%s", liveRoomId);
        URL socketNewUrl = new URL(socketUrl);
        URI wss = new URI("wss", socketNewUrl.getHost(), socketNewUrl.getPath(), socketNewUrl.getQuery(), null);

        //发起socket请求
        DyWebSocketClient socketClient = new DyWebSocketClient(wss, headMap);
        socketClient.run();
    }

}
