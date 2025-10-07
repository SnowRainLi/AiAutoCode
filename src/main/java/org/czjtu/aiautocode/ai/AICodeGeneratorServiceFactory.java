package org.czjtu.aiautocode.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *AI服务创建工厂
 */
@Configuration
public class AICodeGeneratorServiceFactory {
    @Resource
    private ChatModel chatModel;
    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建AI服务
     * @return
     */
    @Bean
    public AICodeGeneratorService aiCodeGeneratorService(){
        return AiServices.builder(AICodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }

}
