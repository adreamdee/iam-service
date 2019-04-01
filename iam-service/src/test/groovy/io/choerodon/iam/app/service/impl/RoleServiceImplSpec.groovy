package io.choerodon.iam.app.service.impl

import io.choerodon.core.domain.Page
import io.choerodon.iam.IntegrationTestConfiguration
import io.choerodon.iam.api.dto.RoleDTO
import io.choerodon.iam.api.dto.RoleSearchDTO
import io.choerodon.iam.app.service.RoleService
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author dengyouquan
 * */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class RoleServiceImplSpec extends Specification {
    @Autowired
    private RoleService roleService

    def "PagingQuery"() {
        given: "构造请求参数"
        PageRequest pageRequest = new PageRequest(0, 20)
        Boolean needUsers = true
        Long sourceId = 0L
        String sourceType = "site"
        RoleSearchDTO role = new RoleSearchDTO()

        when: "调用方法[site层]"
        Page<RoleDTO> page = roleService.pagingQuery(pageRequest, needUsers, sourceId, sourceType, role)

        then: "校验参数"
        page.totalPages != 0
        page.totalElements != 0

        when: "调用方法[organization层]"
        sourceId = 1L
        sourceType = "organization"
        page = roleService.pagingQuery(pageRequest, needUsers, sourceId, sourceType, role)

        then: "校验参数"
        page.totalPages != 0
        page.totalElements != 0

        when: "调用方法[project层]"
        sourceId = 0L
        sourceType = "project"
        page = roleService.pagingQuery(pageRequest, needUsers, sourceId, sourceType, role)

        then: "校验参数"
        page.totalPages != 0
        page.totalElements != 0
    }
}
