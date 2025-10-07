package org.czjtu.aiautocode.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.czjtu.aiautocode.model.entity.App;
import org.czjtu.aiautocode.service.AppService;
import org.czjtu.aiautocode.mapper.AppMapper;
import org.springframework.stereotype.Service;

/**
* @author 画外人易朽
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2025-10-07 16:52:13
*/
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
    implements AppService{

}




