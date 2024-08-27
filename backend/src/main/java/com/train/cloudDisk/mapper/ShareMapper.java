package com.train.cloudDisk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.cloudDisk.model.Share;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShareMapper extends BaseMapper<Share> {
}
