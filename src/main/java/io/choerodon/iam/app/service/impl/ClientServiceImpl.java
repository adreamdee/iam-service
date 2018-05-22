package io.choerodon.iam.app.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.iam.api.dto.ClientDTO;
import io.choerodon.iam.app.service.ClientService;
import io.choerodon.iam.domain.oauth.entity.ClientE;
import io.choerodon.iam.domain.repository.ClientRepository;
import io.choerodon.iam.domain.repository.OrganizationRepository;
import io.choerodon.iam.infra.dataobject.ClientDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author wuguokai
 */
@Component
public class ClientServiceImpl implements ClientService {

    private OrganizationRepository organizationRepository;
    private ClientRepository clientRepository;
    private static final String ORGANIZATION_ID_NOT_EQUAL_EXCEPTION = "error.organizationId.not.same";

    public ClientServiceImpl(OrganizationRepository organizationRepository, ClientRepository clientRepository) {
        this.organizationRepository = organizationRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public ClientDTO create(Long orgId, ClientDTO clientDTO) {
        isOrgExist(orgId);
        clientDTO.setId(null);
        clientDTO.setOrganizationId(orgId);
        return ConvertHelper.convert(
                clientRepository.create(ConvertHelper.convert(clientDTO, ClientE.class)), ClientDTO.class);
    }

    @Override
    public ClientDTO update(Long orgId, Long clientId, ClientDTO clientDTO) {
        isOrgExist(orgId);
        return ConvertHelper.convert(
                clientRepository.update(clientId, ConvertHelper.convert(clientDTO, ClientE.class)),
                ClientDTO.class);
    }

    @Override
    public Boolean delete(Long orgId, Long clientId) {
        ClientDTO clientDTO = query(orgId, clientId);
        if (!orgId.equals(clientDTO.getOrganizationId())) {
            throw new CommonException(ORGANIZATION_ID_NOT_EQUAL_EXCEPTION);
        }
        return clientRepository.delete(clientId);
    }

    @Override
    public ClientDTO query(Long orgId, Long clientId) {
        ClientDTO clientDTO = ConvertHelper.convert(clientRepository.query(clientId), ClientDTO.class);
        if (clientDTO == null) {
            throw new CommonException("error.client.not.exist");
        }
        if (!orgId.equals(clientDTO.getOrganizationId())) {
            throw new CommonException(ORGANIZATION_ID_NOT_EQUAL_EXCEPTION);
        }
        return clientDTO;
    }

    @Override
    public ClientDTO queryByName(Long orgId, String clientName) {
        ClientDTO clientDTO = ConvertHelper.convert(clientRepository.queryByClientName(clientName), ClientDTO.class);
        if (clientDTO == null) {
            throw new CommonException("error.client.not.exist");
        }
        if (!orgId.equals(clientDTO.getOrganizationId())) {
            throw new CommonException(ORGANIZATION_ID_NOT_EQUAL_EXCEPTION);
        }
        return clientDTO;
    }

    @Override
    public Page<ClientDTO> list(ClientDTO clientDTO, PageRequest pageRequest, String[] params) {
        isOrgExist(clientDTO.getOrganizationId());
        return ConvertPageHelper.convertPage(
                clientRepository.pagingQuery(pageRequest,
                        ConvertHelper.convert(clientDTO, ClientDO.class),
                        params), ClientDTO.class);
    }

    @Override
    public void check(ClientDTO client) {
        Boolean createCheck = StringUtils.isEmpty(client.getId());
        Boolean checkName = !StringUtils.isEmpty(client.getName());
        if (!checkName) {
            throw new CommonException("error.clientName.null");
        }
        ClientDO clientDO = ConvertHelper.convert(client, ClientDO.class);
        if (createCheck) {
            Boolean existed = clientRepository.selectOne(clientDO) != null;
            if (existed) {
                throw new CommonException("error.clientName.exist");
            }
        } else {
            Long id = clientDO.getId();
            clientDO.setId(null);
            ClientDO clientDO1 = clientRepository.selectOne(clientDO);
            Boolean existed = clientDO1 != null && !id.equals(clientDO1.getId());
            if (existed) {
                throw new CommonException("error.clientName.exist");
            }
        }

    }

    private void isOrgExist(Long orgId) {
        if (organizationRepository.selectByPrimaryKey(orgId) == null) {
            throw new CommonException("error.organization.notFound");
        }
    }
}
