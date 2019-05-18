package com.example.demo.dao;

import com.example.demo.domain.MiaoshaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MiaoshaUserDao {

    @Select("SELECT * FROM miaosha_user WHERE id=#{id}")
    MiaoshaUser getById(@Param("id")long id);

    @Update("UPDATE miaosha_user SET password = #{password} WHERE id = #{id}")
    void update(MiaoshaUser toBeUpdate);
}
