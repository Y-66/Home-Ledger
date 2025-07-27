package com.yu.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.ledger.entity.po.Users;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UsersMapper extends BaseMapper<Users> {
} 