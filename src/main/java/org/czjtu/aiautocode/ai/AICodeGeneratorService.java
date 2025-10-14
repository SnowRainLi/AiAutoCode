package org.czjtu.aiautocode.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import org.czjtu.aiautocode.ai.model.HtmlCodeResult;
import org.czjtu.aiautocode.ai.model.MultiFileCodeResult;
import reactor.core.publisher.Flux;

public interface AICodeGeneratorService {
    /**
     * 生成原生HTML代码
     * @param userMessage 用户提示
     * @return AI回复
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-proompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户提示
     * @return AI回复
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-proompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    /**
     * 生成原生HTML代码
     * @param userMessage 用户提示
     * @return AI回复
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-proompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户提示
     * @return AI回复
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-proompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成VUE项目代码（流式）
     * @param userMessage 用户提示
     * @return AI回复
     */
    @SystemMessage(fromResource = "prompt/code-vue-progect-prompt")
    TokenStream generateVueCodeStream(@MemoryId long appId, @UserMessage String userMessage);
}
