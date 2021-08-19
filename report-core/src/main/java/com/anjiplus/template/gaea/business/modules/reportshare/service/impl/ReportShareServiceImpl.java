
package com.anjiplus.template.gaea.business.modules.reportshare.service.impl;

import com.anji.plus.gaea.constant.BaseOperationEnum;
import com.anji.plus.gaea.curd.mapper.GaeaBaseMapper;
import com.anji.plus.gaea.exception.BusinessException;
import com.anjiplus.template.gaea.business.enums.EnableFlagEnum;
import com.anjiplus.template.gaea.business.modules.reportshare.controller.dto.ReportShareDto;
import com.anjiplus.template.gaea.business.modules.reportshare.dao.ReportShareMapper;
import com.anjiplus.template.gaea.business.modules.reportshare.dao.entity.ReportShare;
import com.anjiplus.template.gaea.business.modules.reportshare.service.ReportShareService;
import com.anjiplus.template.gaea.business.util.DateUtil;
import com.anjiplus.template.gaea.business.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
* @desc ReportShare 报表分享服务实现
* @author Raod
* @date 2021-08-18 13:37:26.663
**/
@Service
public class ReportShareServiceImpl implements ReportShareService {

    /**
     * 默认跳转路由为aj的页面
     */
    private static final String SHARE_FLAG = "#/aj/";

    private static final String SHARE_URL = "#";

    @Autowired
    private ReportShareMapper reportShareMapper;

    @Override
    public GaeaBaseMapper<ReportShare> getMapper() {
      return reportShareMapper;
    }

    @Override
    public ReportShare getDetail(Long id) {
        ReportShare reportShare = this.selectOne(id);
        return reportShare;
    }

    @Override
    public ReportShareDto insertShare(ReportShareDto dto) {
        ReportShareDto reportShareDto = new ReportShareDto();
        ReportShare entity = new ReportShare();
        BeanUtils.copyProperties(dto, entity);
        insert(entity);
        //将分享链接返回
        reportShareDto.setShareUrl(entity.getShareUrl());
        return reportShareDto;
    }

    @Override
    public ReportShare detailByCode(String shareCode) {
        LambdaQueryWrapper<ReportShare> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ReportShare::getShareCode, shareCode);
        wrapper.eq(ReportShare::getEnableFlag, EnableFlagEnum.ENABLE.getCodeDesc());
        return selectOne(wrapper);
    }

    @Override
    public void processBeforeOperation(ReportShare entity, BaseOperationEnum operationEnum) throws BusinessException {
        switch (operationEnum) {
            case INSERT:
                init(entity);
                break;
            default:

                break;
        }
    }

    /**
     * 新增初始化
     * @param entity
     */
    private void init(ReportShare entity) {
        //前端地址  window.location.href https://report.anji-plus.com/index.html#/report/bigscreen
        //截取#之前的内容
        //http://localhost:9528/#/bigscreen/viewer?reportCode=bigScreen2
        //http://127.0.0.1:9095/reportDashboard/getData
        String shareCode = UUID.randomUUID().toString();
        entity.setShareCode(shareCode);
        if (entity.getShareUrl().contains(SHARE_URL)) {
            String prefix = entity.getShareUrl().substring(0, entity.getShareUrl().indexOf("#"));
            entity.setShareUrl(prefix + SHARE_FLAG + shareCode);
        } else {
            entity.setShareUrl(entity.getShareUrl() + SHARE_FLAG + shareCode);
        }
        entity.setShareValidTime(DateUtil.getFutureDateTmdHms(entity.getShareValidType()));
        entity.setShareToken(JwtUtil.createToken(entity.getReportCode(), shareCode, entity.getShareValidTime()));
    }
}
