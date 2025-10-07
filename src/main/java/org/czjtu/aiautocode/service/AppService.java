package org.czjtu.aiautocode.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.czjtu.aiautocode.model.dto.app.AppQueryRequest;
import org.czjtu.aiautocode.model.entity.App;
import org.czjtu.aiautocode.model.vo.AppVO;

import java.util.List;

/**
* @author 画外人易朽
* @description 针对表【app(应用)】的数据库操作Service
* @createDate 2025-10-07 16:52:13
*/
public interface AppService extends IService<App> {

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

}
