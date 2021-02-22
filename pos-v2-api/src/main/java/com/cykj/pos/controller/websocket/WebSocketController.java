package com.cykj.pos.controller.websocket;

import com.cykj.pos.domain.BizMessageRecords;
import com.cykj.pos.profit.dto.MessageDTO;
import com.cykj.pos.websocket.server.WebSocketServer;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * WebSocket控制器
 *
 * @author weijianbo
 * @date 2021-02-19
 */
@RestController
@RequestMapping("/websocket")
public class WebSocketController {
    @Resource
    private WebSocketServer webSocketServer;
    @PostMapping("/send/test")
    public void sendInfoToOneTest(@RequestBody BizMessageRecords message){
        webSocketServer.sendInfo(message.getMsgUserId(),message);
    }

    @PostMapping("/send/to/one")
    public void sendInfoToOne(@RequestBody MessageDTO message){
        webSocketServer.sendInfo(message.getUserId(),message.getMessage());
    }
    @PostMapping("/send/to/users")
    public void sendInfoToUsers(@RequestBody MessageDTO message){
        List<Long> userIds = message.getUserIds();
        for(Long userId: userIds){
            webSocketServer.sendInfo(userId,"[BIZ]"+message.getMessage());
        }
    }
    @PostMapping("/send/to/all")
    public void sendInfoToAll(@RequestBody MessageDTO message){
        webSocketServer.onMessage("[SYS]"+message.getMessage());
    }
}
