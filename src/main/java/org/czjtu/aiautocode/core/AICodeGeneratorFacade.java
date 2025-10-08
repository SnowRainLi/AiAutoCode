package org.czjtu.aiautocode.core;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.czjtu.aiautocode.ai.AICodeGeneratorService;
import org.czjtu.aiautocode.ai.model.HtmlCodeResult;
import org.czjtu.aiautocode.ai.model.MultiFileCodeResult;
import org.czjtu.aiautocode.core.parser.CodeParserExecutor;
import org.czjtu.aiautocode.core.saver.CodeFileSaverExecutor;
import org.czjtu.aiautocode.exception.BusinessException;
import org.czjtu.aiautocode.exception.ErrorCode;
import org.czjtu.aiautocode.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI代码生成门面类，封装了AI代码生成器的调用逻辑
 */
@Service
@Slf4j
public class AICodeGeneratorFacade {
    @Resource
    private AICodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口方法，根据传入的代码生成模式，调用对应的代码生成方法
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId){
        if (codeGenTypeEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码生成模式");
        }
        return switch (codeGenTypeEnum){
            case HTML-> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(htmlCodeResult,CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE-> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(multiFileCodeResult,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default->{
                String errorMessage = "不支持的代码生成模式"+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }
    /**
     * 统一入口方法，根据传入的代码生成模式，调用对应的代码生成方法(流式)
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId){
        if (codeGenTypeEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码生成模式");
        }
        return switch (codeGenTypeEnum){
            case HTML->{
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield  ProcessCodeStream(result,CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE-> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield  ProcessCodeStream(result,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default->{
                String errorMessage = "不支持的代码生成模式"+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成多文件代码(流式)
     *
     * @param codeStream 代码流
     * @param codeGenType 代码生成模式
     * @return 流式响应
     */
    private Flux<String> ProcessCodeStream(Flux<String> codeStream,CodeGenTypeEnum codeGenType,Long appId) {
        //定义一个字符串拼接器
        StringBuilder codeBuilder=new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            //实时拼接字符串
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                //流式返回后，保存代码
                String completeCode = codeBuilder.toString();
                //调用执行器解析代码为对象
                Object parseResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                //使用执行器保存代码
                File saveDir = CodeFileSaverExecutor.executeSaver(parseResult, codeGenType, appId);
                log.info("保存的代码文件路径：{}", saveDir.getAbsolutePath());
            }catch (Exception e){
                log.error("代码保存失败：{}",e.getMessage());
            }
        });
    }


}
