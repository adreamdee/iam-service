package io.choerodon.iam.domain.service;

import io.choerodon.iam.domain.iam.entity.RoleE;

/**
 * @author superlee
 */
public interface IRoleService {

    RoleE create(RoleE roleE);

    RoleE update(RoleE roleE);

    void deleteByPrimaryKey(Long id);

    RoleE updateRoleEnabled(Long id);

    RoleE updateRoleDisabled(Long id);
}
