package org.czjtu.aiautocode.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.czjtu.aiautocode.model.entity.ChatHistory;
import org.czjtu.aiautocode.service.ChatHistoryService;
import org.czjtu.aiautocode.mapper.ChatHistoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 画外人易朽
* @description 针对表【chat_history(对话历史)】的数据库操作Service实现
* @createDate 2025-10-08 15:50:48
*/
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
    implements ChatHistoryService{

}




