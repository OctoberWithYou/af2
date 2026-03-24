package org.ljc.server.registry;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.ljc.common.model.AgentInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent注册表
 * 管理已连接的Agent信息
 */
public class AgentRegistry {
    /**
     * Agent信息映射: agentId -> AgentInfo
     */
    private final Map<String, AgentInfo> agentInfoMap = new ConcurrentHashMap<>();

    /**
     * Channel映射: agentId -> Channel
     */
    private final Map<String, Channel> agentChannelMap = new ConcurrentHashMap<>();

    /**
     * Channel反向映射: ChannelId -> agentId
     */
    private final Map<ChannelId, String> channelAgentMap = new ConcurrentHashMap<>();

    /**
     * 注册Agent
     *
     * @param agentId Agent标识
     * @param channel Netty Channel
     * @param agentInfo Agent信息
     */
    public void register(String agentId, Channel channel, AgentInfo agentInfo) {
        agentInfoMap.put(agentId, agentInfo);
        agentChannelMap.put(agentId, channel);
        channelAgentMap.put(channel.id(), agentId);
    }

    /**
     * 注销Agent
     *
     * @param agentId Agent标识
     */
    public void unregister(String agentId) {
        Channel channel = agentChannelMap.remove(agentId);
        if (channel != null) {
            channelAgentMap.remove(channel.id());
        }
        agentInfoMap.remove(agentId);
    }

    /**
     * 根据Channel获取Agent ID
     *
     * @param channel Netty Channel
     * @return Agent标识
     */
    public String getAgentIdByChannel(Channel channel) {
        return channelAgentMap.get(channel.id());
    }

    /**
     * 根据Agent ID获取Channel
     *
     * @param agentId Agent标识
     * @return Netty Channel
     */
    public Channel getChannelByAgentId(String agentId) {
        return agentChannelMap.get(agentId);
    }

    /**
     * 根据Agent ID获取Agent信息
     *
     * @param agentId Agent标识
     * @return Agent信息
     */
    public AgentInfo getAgentInfo(String agentId) {
        return agentInfoMap.get(agentId);
    }

    /**
     * 检查Agent是否已注册
     *
     * @param agentId Agent标识
     * @return 是否已注册
     */
    public boolean isRegistered(String agentId) {
        return agentInfoMap.containsKey(agentId);
    }

    /**
     * 获取所有已注册的Agent数量
     *
     * @return Agent数量
     */
    public int getAgentCount() {
        return agentInfoMap.size();
    }

    /**
     * 获取所有已注册的Agent信息
     *
     * @return Agent信息映射
     */
    public Map<String, AgentInfo> getAllAgents() {
        return new ConcurrentHashMap<>(agentInfoMap);
    }

    /**
     * 更新Agent心跳时间
     *
     * @param agentId Agent标识
     */
    public void updateHeartbeat(String agentId) {
        AgentInfo info = agentInfoMap.get(agentId);
        if (info != null) {
            info.setLastHeartbeat(java.time.LocalDateTime.now());
        }
    }
}