package com.newfly.service;

import com.newfly.common.Constant;
import com.newfly.common.SocketChannelMap;
import com.newfly.dao.PlayerRedis;
import com.newfly.dao.SceneRedis;
import com.newfly.pojo.ResultMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class SceneService
{
    @Autowired
    PlayerRedis playerRedis;

    @Autowired
    SceneRedis sceneRedis;


    // 保存世界场景信息
    public void addPlayer(String playerId, String sceneId) {
        sceneRedis.add(playerId, sceneId);
    }

    // 移除
    public void removePlayer(String playerId) {
        String sceneId = playerRedis.getScene(playerId);
        sceneRedis.remove(playerId, sceneId);
    }


    // 玩家移动
    public ResultMessage movetoPlayer(ResultMessage msg) {
        String[] strings = msg.getBody().split(":");
        String playerId = strings[0];
        String sceneId = strings[1];
        String moveX = strings[2];
        String moveY = strings[3];

        if (!playerRedis.exist(playerId))
            return null;

        // 控制玩家移动
        playerRedis.moveto(playerId, moveX, moveY);

        Set<String> players = new HashSet<>();
        // 场景切换
        String oldScene = playerRedis.switchScene(playerId, sceneId);
        if (!oldScene.equals(sceneId)) {
            players.addAll(sceneRedis.sceneMember(oldScene));
            sceneRedis.switchScene(playerId, oldScene, sceneId); // 测试有没有错误
        }
        // 添加新场景的所有玩家
        players.addAll(sceneRedis.sceneMember(sceneId));

        // 移除自己
        players.remove(playerId);

        String content = msg.getBody();
        SocketChannelMap.sendAll(players, Constant.MESSAGE_MAP_PLAYER_MOVE_RETURN, content);
        return null;
    }

    // 场景切换
    //注意修改场景保存的队伍列表

}
