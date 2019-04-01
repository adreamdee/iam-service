package io.choerodon.iam.domain.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.iam.domain.iam.entity.LookupE;
import io.choerodon.iam.infra.dataobject.LookupDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * @author superlee
 */
public interface LookupRepository {

    LookupE insert(LookupE lookupE);

    Page<LookupDO> pagingQuery(PageRequest pageRequest, LookupDO lookupDO, String param);

    void delete(LookupE lookupE);

    LookupE update(LookupE lookupE, Long id);

    LookupE selectById(Long id);

    List<LookupE> select(LookupE lookupE);

    void deleteById(Long id);

    LookupDO listByCodeWithLookupValues(String code);
}
