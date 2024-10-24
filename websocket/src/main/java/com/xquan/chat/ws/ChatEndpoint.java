package com.xquan.chat.ws;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/chat/{username}")
@Component
@Slf4j
public class ChatEndpoint {

    public static final Map<String, Session> onLineUsers = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        onLineUsers.put(username, session);
        log.info("{}加入了，在线总人数为{}",username, onLineUsers.size());
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.set("user", array);
        for (Object key : onLineUsers.keySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("username", key);
            array.add(jsonObject);
        }

        sendAllMessage(JSONUtil.toJsonStr(result));
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("username") String username) {
        log.info("客户端收到用户{}的消息：{}",username, message);
        JSONObject obj = JSONUtil.parseObj(message);
        String toUsername = obj.getStr("to");
        String text = obj.getStr("text");
        Session toSession = onLineUsers.get(toUsername);
        if (toSession != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("from", username);
            jsonObject.set("text", text);
            this.sendMessage(jsonObject.toString(), toSession);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        onLineUsers.remove(username);
        log.info("移除用户{}，剩余{}",username, onLineUsers.size());
    }

    private void sendMessage(String message, Session toSession) {
        try{
            log.info("服务端给客户端[{}]发送消息{},",toSession.getId(),message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("异常", e);
        }
    }

    private void sendAllMessage(String message) {
        try{
            for (Session session : onLineUsers.values()) {
                log.info("服务端给客户端[{}]发送消息{},",session.getId(),message);
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error("异常", e);
        }
    }
}
