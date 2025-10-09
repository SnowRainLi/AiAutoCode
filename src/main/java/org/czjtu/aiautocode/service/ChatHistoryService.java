package org.czjtu.aiautocode.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.czjtu.aiautocode.model.dto.chathistory.ChatHistoryQueryRequest;
import org.czjtu.aiautocode.model.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import org.czjtu.aiautocode.model.entity.User;

import java.time.LocalDateTime;

/**
* @author 画外人易朽
* @description 针对表【chat_history(对话历史)】的数据库操作Service
* @createDate 2025-10-08 15:50:48
*/
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息
     * @param appId appId
     * @param message 消息
     * @param messageType 消息类型
     * @param userId 用户id
     * @return 是否添加成功
     */
    boolean addChatMessage(Long appId, String message,String messageType,Long userId);

    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装类
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页查询某app的对话历史
     * @param appId appId
     * @param pageSize 页大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param loginUser 登录用户
     * @return 对话历史列表
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 加载对话历史到内存
     * @param appId appId
     * @param chatMemory 对话记忆
     * @param maxCount 最大数量
     * @return 加载数量
     */
    int lodeChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
