package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
//    @Autowired
//    private DictMapper dictMapper;
    //导出字典接口
    @Override
    public void exportData(HttpServletResponse response) {
        //设置下载的信息，让操作以下载方式打开
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = "dict";
        response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

        //查询数据库
        List<Dict> dictList = baseMapper.selectList(null);
        List<DictEeVo> dictVolist= new ArrayList<>();
        for (Dict dict: dictList) {
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict, dictEeVo);
            dictVolist.add(dictEeVo);
        }
        //调用方法写
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict").doWrite(dictVolist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @CacheEvict(value = "dict", allEntries=true) //清空dict缓存下的所有内容
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator") //根据包名，类名，方法名称生成缓存。keyGenerator在redisConfig类中
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        List<Dict> dictList = baseMapper.selectList(wrapper);
        for (Dict dict: dictList) {
            Long dictId = dict.getId();
            boolean hasChild = this.hasChildren(dictId);
            dict.setHasChildren(hasChild);
        }
        return dictList;
    }

    private boolean hasChildren(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
}
