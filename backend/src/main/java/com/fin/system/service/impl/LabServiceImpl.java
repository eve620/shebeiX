package com.fin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fin.system.Mapper.LabMapper;
import com.fin.system.entity.Lab;
import com.fin.system.service.LabService;
import org.springframework.stereotype.Service;

@Service
public class LabServiceImpl extends ServiceImpl<LabMapper, Lab> implements LabService {

}
