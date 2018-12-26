package io.choerodon.iam.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.iam.api.dto.OrganizationDTO;
import io.choerodon.iam.api.dto.OrganizationSimplifyDTO;
import io.choerodon.iam.api.dto.UserDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author wuguokai
 */
public interface OrganizationService {

    OrganizationDTO updateOrganization(Long organizationId, OrganizationDTO organizationDTO);

    OrganizationDTO queryOrganizationById(Long organizationId);

    OrganizationDTO queryOrganizationWithRoleById(Long organizationId);

    Page<OrganizationDTO> pagingQuery(OrganizationDTO organizationDTO, PageRequest pageRequest, String param);

    OrganizationDTO enableOrganization(Long organizationId, Long userId);

    OrganizationDTO disableOrganization(Long organizationId, Long userId);

    void check(OrganizationDTO organization);

    Page<UserDTO> pagingQueryUsersInOrganization(Long organizationId,
                                                 Long userId, String email, PageRequest pageRequest, String param);

    List<OrganizationDTO> queryByIds(Set<Long> ids);

    /**
     * 获取所有组织{id,name}
     *
     * @return list
     */
    List<OrganizationSimplifyDTO> getAllOrgs(PageRequest pageRequest);

}
