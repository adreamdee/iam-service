package io.choerodon.iam.infra.repository.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.iam.domain.oauth.entity.LdapE;
import io.choerodon.iam.domain.repository.LdapRepository;
import io.choerodon.iam.infra.dataobject.LdapDO;
import io.choerodon.iam.infra.mapper.LdapMapper;
import org.springframework.stereotype.Component;

/**
 * @author wuguokai
 */
@Component
public class LdapRepositoryImpl implements LdapRepository {
    private LdapMapper ldapMapper;

    public LdapRepositoryImpl(LdapMapper ldapMapper) {
        this.ldapMapper = ldapMapper;
    }

    @Override
    public LdapE create(LdapE ldapE) {
        LdapDO ldapDO = ConvertHelper.convert(ldapE, LdapDO.class);
        if (ldapMapper.insertSelective(ldapDO) != 1) {
            throw new CommonException("error.ldap.insert");
        }
        ldapDO = ldapMapper.selectByPrimaryKey(ldapDO.getId());
        return ConvertHelper.convert(ldapDO, LdapE.class);
    }

    @Override
    public LdapDO update(Long id, LdapDO ldap) {
        ldap.setId(id);
        if (ldapMapper.updateByPrimaryKey(ldap) != 1) {
            throw new CommonException("error.ldap.update");
        }
        return ldapMapper.selectByPrimaryKey(id);
    }

    @Override
    public LdapDO queryById(Long id) {
        return ldapMapper.selectByPrimaryKey(id);
    }

    @Override
    public LdapDO queryByOrgId(Long orgId) {
        LdapDO ldapDO = new LdapDO();
        ldapDO.setOrganizationId(orgId);
        return ldapMapper.selectOne(ldapDO);
    }

    @Override
    public Boolean delete(Long id) {
        if (ldapMapper.selectByPrimaryKey(id) == null) {
            throw new CommonException("error.ldap.not.exist");
        }
        if (ldapMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.ldap.delete");
        }
        return true;
    }
}
