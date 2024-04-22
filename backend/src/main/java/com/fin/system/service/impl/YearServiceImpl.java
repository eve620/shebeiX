package com.fin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fin.system.Mapper.YearMapper;
import com.fin.system.entity.Year;
import com.fin.system.service.YearService;
import org.springframework.stereotype.Service;

@Service
public class YearServiceImpl extends ServiceImpl<YearMapper, Year> implements YearService {

}
