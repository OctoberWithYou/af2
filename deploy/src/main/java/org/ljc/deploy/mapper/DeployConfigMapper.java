package org.ljc.deploy.mapper;

import org.apache.ibatis.annotations.*;
import org.ljc.deploy.entity.DeployConfig;

import java.util.List;

/**
 * 部署配置Mapper
 */
@Mapper
public interface DeployConfigMapper {

    @Select("SELECT id, name, type, config_json as configJson, status, created_at as createdAt, updated_at as updatedAt, created_by as createdBy FROM deploy_config ORDER BY created_at DESC")
    List<DeployConfig> findAll();

    @Select("SELECT id, name, type, config_json as configJson, status, created_at as createdAt, updated_at as updatedAt, created_by as createdBy FROM deploy_config WHERE id = #{id}")
    DeployConfig findById(Long id);

    @Select("SELECT id, name, type, config_json as configJson, status, created_at as createdAt, updated_at as updatedAt, created_by as createdBy FROM deploy_config WHERE type = #{type}")
    List<DeployConfig> findByType(String type);

    @Insert("INSERT INTO deploy_config(name, type, config_json, status, created_at, updated_at, created_by) " +
        "VALUES(#{name}, #{type}, #{configJson}, #{status}, #{createdAt}, #{updatedAt}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DeployConfig config);

    @Update("UPDATE deploy_config SET name = #{name}, type = #{type}, config_json = #{configJson}, " +
        "status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(DeployConfig config);

    @Delete("DELETE FROM deploy_config WHERE id = #{id}")
    int deleteById(Long id);
}