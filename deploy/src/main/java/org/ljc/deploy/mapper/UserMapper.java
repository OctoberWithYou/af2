package org.ljc.deploy.mapper;

import org.apache.ibatis.annotations.*;
import org.ljc.deploy.entity.User;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);
    
    @Update("UPDATE users SET password = #{password}, must_change_password = #{mustChangePassword}, updated_at = NOW() WHERE id = #{id}")
    void update(User user);
    
    @Insert("INSERT INTO users (username, password, must_change_password, created_at, updated_at) VALUES (#{username}, #{password}, #{mustChangePassword}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
}
