package io.choerodon.iam.domain.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.iam.domain.iam.entity.RoleE;
import io.choerodon.iam.infra.dataobject.RoleDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;
import java.util.Set;

/**
 * @author superlee
 * @data 2018/3/27
 */
public interface RoleRepository {

    Page<RoleDO> pagingQuery(PageRequest pageRequest, RoleDO roleDO, String param);

    RoleDO selectByCode(String code);

    RoleE insertSelective(RoleE roleE);

    RoleE selectByPrimaryKey(Long id);

    RoleE updateSelective(RoleE roleE);

    void deleteByPrimaryKey(Long id);

    RoleDO selectRoleWithPermissionsAndLabels(Long id);

    List<RoleDO> select(RoleDO roleDO);

    List<Long> queryRoleByUser(Long userId, String sourceType, Long sourceId);

    RoleDO selectOne(RoleDO roleDO);

    boolean judgeRolesSameLevel(List<Long> roleIds);

    List<RoleDO> selectRolesByLabelNameAndType(String name, String type);

    List<RoleDO> selectInitRolesByPermissionId(Long permissionId);

    List<RoleDO> selectUsersRolesBySourceIdAndType(String sourceType, Long sourceId, Long userId);

    Set<String> matchCode(Set<String> codeSet);

    List<RoleDO> queryRoleByOrgId(Long orgId);

    List<RoleDO> selectAll();

    List<RoleDO> fuzzySearchRolesByName(String roleName, String sourceType);
}
