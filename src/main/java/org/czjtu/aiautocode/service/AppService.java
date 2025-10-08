package org.czjtu.aiautocode.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.czjtu.aiautocode.model.dto.app.AppQueryRequest;
import org.czjtu.aiautocode.model.entity.App;
import org.czjtu.aiautocode.model.entity.User;
import org.czjtu.aiautocode.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
* @author 画外人易朽
* @description 针对表【app(应用)】的数据库操作Service
* @createDate 2025-10-07 16:52:13
*/
public interface AppService extends IService<App> {

    /**
     * 获取封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取列表
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询包装类
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取代码生成结果
     * @param appId appId
     * @param message 提示语
     * @param longinUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User longinUser);

    /**
     * 部署应用
     * @param appId appId
     * @param longinUser 登录用户
     * @return 可访问的部署地址
     */
    String deployApp(Long appId, User longinUser);

}
