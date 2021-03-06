package com.newfly.dao;


import com.newfly.pojo.Combat;
import com.newfly.pojo.Player;
import com.newfly.pojo.Task;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;


@Repository
public class PlayerRedis
{
    private final JedisPool jedisPool;

    public PlayerRedis(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }


    // 设置玩家信息, 只能保存从mysql中读取的
    public void savePlayer(Player player) {
        Jedis jedis = jedisPool.getResource();
        String strID = "player:" + player.getId();
        jedis.hset(strID, "id", player.getStrId());
        jedis.hsetnx(strID, "name", player.getName());
        jedis.hsetnx(strID, "profession", String.valueOf(player.getProfession()));
        jedis.hsetnx(strID, "grade", String.valueOf(player.getGrade()));
        jedis.hsetnx(strID, "scene", String.valueOf(player.getScene()));
        jedis.hsetnx(strID, "exp", String.valueOf(player.getExp()));
        jedis.hsetnx(strID, "money", String.valueOf(player.getMoney()));
        jedis.hsetnx(strID, "x", String.valueOf(player.getX()));
        jedis.hsetnx(strID, "y", String.valueOf(player.getY()));
        jedis.close();
    }

    // 移除玩家信息
    public void removePlayer(String playerId) {
        Jedis jedis = jedisPool.getResource();
        jedis.del("player:" + playerId);
        jedis.close();
    }

    // 获取玩家信息 Set集合形式
    public Map<String, String> getInfo(String playerId) {
        Jedis jedis = jedisPool.getResource();
        Map<String, String> playerInfo = jedis.hgetAll("player:" + playerId);
        jedis.close();
        if (playerInfo.isEmpty())
            return null;
        return playerInfo;
    }

    // 获取玩家信息, Player对象形式
    public Player getPlayer(String playerId) {
        Jedis jedis = jedisPool.getResource();
        String strId = "player:" + playerId;
        if (!jedis.exists(strId)) {
            jedis.close();
            return null;
        }
        Player player = new Player();
        player.setId(Integer.parseInt(jedis.hget(strId, "id")));
        player.setName(jedis.hget(strId, "name"));
        player.setProfession(Integer.parseInt(jedis.hget(strId, "profession")));
        player.setGrade(Integer.parseInt(jedis.hget(strId, "grade")));
        player.setExp(Integer.parseInt(jedis.hget(strId, "exp")));
        player.setMoney(Integer.parseInt(jedis.hget(strId, "money")));
        player.setScene(Integer.parseInt(jedis.hget(strId, "scene")));
        player.setX(Integer.parseInt(jedis.hget(strId, "x")));
        player.setY(Integer.parseInt(jedis.hget(strId, "y")));
        player.setCreateTime(jedis.hget(strId, "create_time"));
        jedis.close();
        return player;
    }

    // 获取玩家信息 字符串形式
    public String playerInfo(String playerId) {
        Jedis jedis = jedisPool.getResource();
        Map<String, String> player = jedis.hgetAll("player:" + playerId);
        if (player.isEmpty())
            return null;
        String string = player.get("id") + ":" + player.get("name") + ":" + player.get("profession") + ":" + player.get("grade") + ":" + player.get("exp") + ":" + player.get("money") + ":" + player.get("scene") + ":" + player.get("x") + ":" + player.get("y");
        jedis.close();
        return string;
    }

    // 是否存在
    public boolean exist(String playerId) {
        Jedis jedis = jedisPool.getResource();
        boolean exist = jedis.exists("player:" + playerId);
        jedis.close();
        return exist;
    }

    // 获取玩家name
    public String getName(String playerId) {
        Jedis jedis = jedisPool.getResource();
        String name = jedis.hget("player:" + playerId, "name");
        jedis.close();
        return name;
    }


    // 保存玩家主线任务进度
    public void saveMainTask(Task task) {
        Jedis jedis = jedisPool.getResource();
        jedis.hset("player:" + task.getId(), "task", String.valueOf(task.getTask()));
        jedis.hset("player:" + task.getId(), "sub_task", String.valueOf(task.getSubTask()));
        jedis.close();
    }

    // 查询玩家主线任务进度, id字段是多用的,这里表示玩家ID
    public Task getMainTask(String playerId) {
        Jedis jedis = jedisPool.getResource();
        Task task = new Task();
        task.setId(Integer.parseInt(playerId));
        task.setTask(Integer.parseInt(jedis.hget("player:" + playerId, "task")));
        task.setSubTask(Integer.parseInt(jedis.hget("player:" + playerId, "sub_task")));
        jedis.close();
        return task;
    }

    // 更新玩家主线任务进度
    public void setMainTask(String playerId, String task, String subTask) {
        Jedis jedis = jedisPool.getResource();
        jedis.hset("player:" + playerId, "task", task);
        jedis.hset("player:" + playerId, "sub_task", subTask);
        jedis.close();
    }


    // 获取玩家所在的场景
    public String getScene(String playerId) {
        Jedis jedis = jedisPool.getResource();
        String sceneId = jedis.hget("player:" + playerId, "scene");
        jedis.close();
        return sceneId;
    }

    // 场景切换, 返回原来的场景
    public String switchScene(String playerId, String sceneId) {
        Jedis jedis = jedisPool.getResource();
        String oldScene = jedis.hget("player:" + playerId, "scene");
        jedis.hset("player:" + playerId, "scene", sceneId);
        jedis.close();
        return oldScene;
    }

    // 玩家移动
    public void moveto(String playerId, String x, String y) {
        Jedis jedis = jedisPool.getResource();
        jedis.hset("player:" + playerId, "x", x);
        jedis.hset("player:" + playerId, "y", y);
        jedis.close();
    }


    // 玩家加入队伍, 返回队长id
    public String joinTeam(String playerId, String teamId) {
        Jedis jedis = jedisPool.getResource();
        jedis.hsetnx("player:" + playerId, "team", teamId);
        String captainId = jedis.hget("team:" + teamId, "captainId");
        jedis.close();
        return captainId;
    }

    // 玩家离开队伍
    public void leaveTeam(String playerId) {
        Jedis jedis = jedisPool.getResource();
        jedis.hdel("player:" + playerId, "team");
        jedis.close();
    }

    // 获取玩家所在队伍
    public String getTeam(String playerId) {
        Jedis jedis = jedisPool.getResource();
        String teamId = jedis.hget("player:" + playerId, "team");
        jedis.close();
        return teamId;
    }

    // 某个玩家是否有队伍
    public boolean existTeam(String playerId) {
        Jedis jedis = jedisPool.getResource();
        boolean result = jedis.hexists("player:" + playerId, "team");
        jedis.close();
        return result;
    }

    // 是否是队长
    public boolean isCaptain(String playerId) {
        Jedis jedis = jedisPool.getResource();
        // 是否有队伍
        String teamId = jedis.hget("player:" + playerId, "team");
        if (teamId == null)
            return false;

        // 获取该队伍的队长id并判断
        String captainId = jedis.hget("team:" + teamId, "captainId");
        return captainId.equals(playerId);
    }


    // 获取玩家信息
    public Combat getCombat(String playerId) {
        Jedis jedis = jedisPool.getResource();
        Combat combat = new Combat();
        combat.setId(Integer.parseInt(playerId));
        combat.setHp(Integer.parseInt(jedis.hget("player:" + playerId, "hp")));
        combat.setAttack(Integer.parseInt(jedis.hget("player:" + playerId, "attack")));
        combat.setDefense(Integer.parseInt(jedis.hget("player:" + playerId, "defense")));
        jedis.close();
        return combat;
    }

    // 设置玩家信息
    public void setCombat(Combat combat) {
        Jedis jedis = jedisPool.getResource();
        String playerId = String.valueOf(combat.getId());
        jedis.hset("player:" + playerId, "hp", String.valueOf(combat.getHp()));
        jedis.hset("player:" + playerId, "attack", String.valueOf(combat.getAttack()));
        jedis.hset("player:" + playerId, "defense", String.valueOf(combat.getDefense()));
        jedis.close();
    }

    // 设置玩家金钱
    public void setMoney(String playerId, int money) {
        Jedis jedis = jedisPool.getResource();
        jedis.hset("player:" + playerId, "money", String.valueOf(money));
        jedis.close();
    }

}// end
