package org.czjtu.aiautocode.core;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.czjtu.aiautocode.ai.AICodeGeneratorService;
import org.czjtu.aiautocode.ai.model.HtmlCodeResult;
import org.czjtu.aiautocode.ai.model.MultiFileCodeResult;
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
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum){
        if (codeGenTypeEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码生成模式");
        }
        return switch (codeGenTypeEnum){
            case HTML-> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE-> generateAndSaveMultiFileCode(userMessage);
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

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum){
        if (codeGenTypeEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码生成模式");
        }
        return switch (codeGenTypeEnum){
            case HTML-> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE-> generateAndSaveMultiFileCodeStream(userMessage);
            default->{
                String errorMessage = "不支持的代码生成模式"+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成原生HTML代码(流式)
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        //定义一个字符串拼接器
        StringBuilder codeBuilder=new StringBuilder();
        return result.doOnNext(chunk -> {
            //实时拼接字符串
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                //流式返回后，保存代码
                String completeHtmlCode = codeBuilder.toString();
                //调用文件解析器解析代码为对象
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                //保存代码
                File saveDir = CodeFileSaver.saveHtmlCode(htmlCodeResult);
                log.info("保存的代码文件路径：{}", saveDir.getAbsolutePath());
            }catch (Exception e){
                log.error("代码保存失败：{}",e.getMessage());
            }
        });
    }

    /**
     * 生成多文件代码(流式)
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //定义一个字符串拼接器
        StringBuilder codeBuilder=new StringBuilder();
        return result.doOnNext(chunk -> {
            //实时拼接字符串
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                //流式返回后，保存代码
                String completeMultiFileCode = codeBuilder.toString();
                //调用文件解析器解析代码为对象
                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                //保存代码
                File saveDir = CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
                log.info("保存的代码文件路径：{}", saveDir.getAbsolutePath());

            }catch (Exception e){
                log.error("代码保存失败：{}",e.getMessage());
            }
        });
    }



    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
    }

    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCode(htmlCodeResult);
    }


}
