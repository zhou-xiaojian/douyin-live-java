package com.example.demo.websocket;

import com.example.demo.protobuf.Dy;
import com.example.demo.util.GzipUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DyWebSocketClient extends WebSocketClient {

    public DyWebSocketClient(URI uri, Map<String, String> httpHeaders) throws URISyntaxException {
        super(uri, new Draft_6455(), httpHeaders);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        // TODO Auto-generated method stub
        System.out.println("握手...");
        for(Iterator<String> it=shake.iterateHttpFields();it.hasNext();) {
            String key = it.next();
            System.out.println(key+":"+shake.getFieldValue(key));
        }
        this.send();
    }

    @Override
    public void onMessage(String s) {
    }


    @Override
    public void onMessage(ByteBuffer bytes) {
        byte[] data = bytes.array();
        this.read(data);
    }

    @Override
    public void onClose(int paramInt, String paramString, boolean paramBoolean) {
        // TODO Auto-generated method stub
        System.out.println("paramInt:"+paramInt+" paramString:"+paramString+" paramBoolean:"+paramBoolean);
        System.out.println("关闭...");
    }

    @Override
    public void onError(Exception e) {
        // TODO Auto-generated method stub
        System.out.println("异常"+e);

    }

    public void read(byte[] payload) {
        // 处理二进制消息
        try {
            // 解析收到的数据
            Dy.PushFrame msgPack = Dy.PushFrame.parseFrom(payload);
            // 解压缩数据
            byte[] decompressed = GzipUtil.degzip(msgPack.getPayload().toByteArray());
            Dy.Response payloadPackage = Dy.Response.parseFrom(decompressed);
            // 如果需要回应，则发送回应消息
            if (payloadPackage.getNeedAck()) {
                sendAck(msgPack.getLogId(), payloadPackage.getInternalExt());
            }
            // 处理消息列表
            for (Dy.Message msg : payloadPackage.getMessagesListList()) {
                switch (msg.getMethod()) {
                    case "WebcastChatMessage":
                        parseChatMsg(msg.getPayload().toByteArray());
                        break;
                    case "WebcastGiftMessage":
                        parseGiftMsg(msg.getPayload().toByteArray());
                        break;
                    case "WebcastLikeMessage":
                        parseLikeMsg(msg.getPayload().toByteArray());
                        break;
                    case "WebcastMemberMessage":
                        parseEnterMsg(msg.getPayload().toByteArray());
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseChatMsg(byte[] payload) {
        // 解析聊天消息
        try {
            Dy.ChatMessage chatMsg = Dy.ChatMessage.parseFrom(payload);
            // 打印聊天消息的发送者和内容
            System.out.println("[弹幕] " + chatMsg.getUser().getNickName() + " : " + chatMsg.getContent());
        } catch (Exception e) {
            System.out.println("解析聊天消息异常");
        }
    }

    private static void parseGiftMsg(byte[] payload) {
        // 解析礼物消息
        try {
            Dy.GiftMessage giftMsg = Dy.GiftMessage.parseFrom(payload);
            // 打印礼物消息的发送者、礼物名称和连击数
            System.out.println("[礼物] " + giftMsg.getUser().getNickName() + " : " + giftMsg.getGift().getName() + " * " + giftMsg.getComboCount());
        } catch (Exception e) {
            System.out.println("解析礼物消息异常");
        }
    }

    private static void parseLikeMsg(byte[] payload) {
        // 解析点赞消息
        try {
            Dy.LikeMessage likeMsg = Dy.LikeMessage.parseFrom(payload);
            // 打印点赞消息的发送者和点赞数
            System.out.println("[点赞] " + likeMsg.getUser().getNickName() + " 点赞 * " + likeMsg.getCount());
        } catch (Exception e) {
            System.out.println("解析点赞消息异常");
        }
    }

    private static void parseEnterMsg(byte[] payload) {
        // 解析进入直播间消息
        try {
            Dy.MemberMessage enterMsg = Dy.MemberMessage.parseFrom(payload);
            // 打印进入直播间消息的发送者
            System.out.println("[入场] " + enterMsg.getUser().getNickName() + " 进入直播间");
        } catch (Exception e) {
            System.out.println("解析进入直播间消息异常");
        }
    }

    public void send() {
        CompletableFuture.runAsync(() -> {
            while (true) {
                Dy.PushFrame pingPack = Dy.PushFrame.newBuilder()
                        .setPayloadType("bh")
                        .build();
                byte[] data = pingPack.toByteArray();
                this.send(data);
                System.out.println("发送心跳");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // Handle error
                }
            }
        });
    }

    public void sendAck(long logId, String iExt) {
        Dy.PushFrame ackPack = Dy.PushFrame.newBuilder()
                .setLogId(logId)
                .setPayloadType(iExt)
                .build();
        byte[] data = ackPack.toByteArray();
        this.send(ByteBuffer.wrap(data));
        System.out.println("发送回应消息");
    }


}

