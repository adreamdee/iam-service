package io.choerodon.iam.infra.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.iam.api.dto.ClientRoleSearchDTO;
import io.choerodon.iam.api.dto.SimplifiedClientDTO;
import io.choerodon.iam.domain.oauth.entity.ClientE;
import io.choerodon.iam.domain.repository.ClientRepository;
import io.choerodon.iam.infra.common.utils.ParamUtils;
import io.choerodon.iam.infra.dataobject.ClientDO;
import io.choerodon.iam.infra.mapper.ClientMapper;
import io.choerodon.iam.infra.mapper.MemberRoleMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author wuguokai
 */
@Component
public class ClientRepositoryImpl implements ClientRepository {

    private ClientMapper clientMapper;

    private MemberRoleMapper memberRoleMapper;

    public ClientRepositoryImpl(ClientMapper clientMapper, MemberRoleMapper memberRoleMapper) {
        this.clientMapper = clientMapper;
        this.memberRoleMapper = memberRoleMapper;
    }


    @Override
    public ClientE create(ClientE clientE) {
        ClientDO clientDO = ConvertHelper.convert(clientE, ClientDO.class);
        int isInsert = clientMapper.insertSelective(clientDO);
        if (isInsert != 1) {
            throw new CommonException("error.client.create");
        }
        clientDO = clientMapper.selectByPrimaryKey(clientDO.getId());
        return ConvertHelper.convert(clientDO, ClientE.class);
    }

    @Override
    public ClientE query(Long clientId) {
        ClientDO clientDO = clientMapper.selectByPrimaryKey(clientId);
        return ConvertHelper.convert(clientDO, ClientE.class);
    }

    @Override
    public Boolean delete(Long clientId) {
        // delete the member-role relationship before the client was deleted
        memberRoleMapper.deleteMemberRoleByMemberIdAndMemberType(clientId, "client");

        int isDelete = clientMapper.deleteByPrimaryKey(clientId);
        if (isDelete != 1) {
            throw new CommonException("error.client.delete");
        }
        return true;
    }

    @Override
    public ClientE queryByClientName(String clientName) {
        ClientDO clientDO = new ClientDO();
        clientDO.setName(clientName);
        List<ClientDO> clientDOS = clientMapper.select(clientDO);
        if (clientDOS.isEmpty()) {
            throw new CommonException("error.client.not.exist");
        }
        return ConvertHelper.convert(clientDOS.get(0), ClientE.class);
    }

    @Override
    public ClientE update(Long clientId, ClientE clientE) {
        ClientDO clientDO = ConvertHelper.convert(clientE, ClientDO.class);
        clientDO.setId(clientId);
        int isUpdate = clientMapper.updateByPrimaryKey(clientDO);
        if (isUpdate != 1) {
            throw new CommonException("error.client.update");
        }
        clientDO = clientMapper.selectByPrimaryKey(clientDO.getId());
        return ConvertHelper.convert(clientDO, ClientE.class);
    }

    @Override
    public Page<ClientE> pagingQuery(PageRequest pageRequest, ClientDO clientDO, String param) {
        clientDO.setOrganizationId(clientDO.getOrganizationId());
        Page<ClientDO> clientDOPage
                = PageHelper.doPageAndSort(pageRequest, () -> clientMapper.fulltextSearch(clientDO, param));
        return ConvertPageHelper.convertPage(clientDOPage, ClientE.class);
    }

    @Override
    public ClientDO selectOne(ClientDO clientDO) {
        return clientMapper.selectOne(clientDO);
    }

    @Override
    public Integer selectClientCountFromMemberRoleByOptions(Long roleId, Long sourceId, String sourceType, ClientRoleSearchDTO clientRoleSearchDTO, String param) {
        return clientMapper.selectClientCountFromMemberRoleByOptions(roleId, sourceType, sourceId, clientRoleSearchDTO, param);
    }

    @Override
    public Page<ClientDO> pagingQueryClientsByRoleIdAndOptions(PageRequest pageRequest, ClientRoleSearchDTO clientRoleSearchDTO, Long roleId, Long sourceId, String sourceType) {
        String param = Optional.ofNullable(clientRoleSearchDTO).map(dto -> ParamUtils.arrToStr(dto.getParam())).orElse(null);
        return PageHelper.doPageAndSort(pageRequest,
                () -> clientMapper.selectClientsByRoleIdAndOptions(roleId, sourceId, sourceType, clientRoleSearchDTO, param));
    }

    @Override
    public Page<SimplifiedClientDTO> pagingAllClientsByParams(PageRequest pageRequest, String params) {
        return PageHelper.doPageAndSort(pageRequest, () -> clientMapper.selectAllClientSimplifiedInfo(params));
    }
}
