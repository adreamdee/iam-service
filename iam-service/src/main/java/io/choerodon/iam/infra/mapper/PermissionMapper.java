package io.choerodon.iam.infra.mapper;

import io.choerodon.iam.infra.dataobject.PermissionDO;
import io.choerodon.iam.infra.dataobject.RoleDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author wuguokai
 */
public interface PermissionMapper extends BaseMapper<PermissionDO> {

    List fulltextSearch(@Param("permissionDO") PermissionDO permissionDO, @Param("param") String param);

    List<PermissionDO> selectByRoleId(@Param("roleId") Long roleId,
                                      @Param("params") String params);

    Set<String> checkPermission(@Param("member_id") Long memberId, @Param("source_type") String sourceType,
                                @Param("source_id") Long sourceId, @Param("codes") Set<String> codes);

    List<PermissionDO> selectErrorLevelPermissionByRole(@Param("role") RoleDO role);
}
