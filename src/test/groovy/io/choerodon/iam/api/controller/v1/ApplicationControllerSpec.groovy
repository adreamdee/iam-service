package io.choerodon.iam.api.controller.v1

import io.choerodon.asgard.saga.producer.TransactionalProducer
import io.choerodon.core.exception.CommonException
import io.choerodon.iam.IntegrationTestConfiguration
import io.choerodon.iam.api.query.ApplicationQuery
import io.choerodon.iam.app.service.ApplicationService
import io.choerodon.iam.app.service.impl.ApplicationServiceImpl
import io.choerodon.iam.infra.asserts.ApplicationAssertHelper
import io.choerodon.iam.infra.asserts.OrganizationAssertHelper
import io.choerodon.iam.infra.asserts.ProjectAssertHelper
import io.choerodon.iam.infra.dto.ApplicationDTO
import io.choerodon.iam.infra.enums.ApplicationCategory
import io.choerodon.iam.infra.enums.ApplicationType
import io.choerodon.iam.infra.mapper.ApplicationExplorationMapper
import io.choerodon.iam.infra.mapper.ApplicationMapper
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class ApplicationControllerSpec extends Specification {
    @Autowired
    ApplicationMapper applicationMapper
    @Autowired
    ApplicationExplorationMapper applicationExplorationMapper

    @Autowired
    OrganizationAssertHelper organizationAssertHelper

    @Autowired
    ProjectAssertHelper projectAssertHelper
    @Autowired
    ApplicationAssertHelper applicationAssertHelper

    ApplicationController controller
    TransactionalProducer producer


    @Shared
    Long id

    def "setup"() {
        producer = Mock(TransactionalProducer)
        ApplicationService service = new ApplicationServiceImpl(applicationMapper, producer, applicationExplorationMapper,
                organizationAssertHelper, projectAssertHelper, applicationAssertHelper)
        controller = new ApplicationController(service)
    }

    def "Create"() {
        given:
        ApplicationDTO dto = new ApplicationDTO()
        dto.setCode("code")
        dto.setName("name")
        dto.setApplicationCategory("application")
        dto.setApplicationType("test")
        dto.setOrganizationId(1L)
        dto.setEnabled(true)

        when:
        def result = controller.create(1, dto)
        id = result.getBody().getId()
        then:
        result.statusCode.is2xxSuccessful()
        result.body.code == 'code'
    }

    def "Update"() {
        given:
        ApplicationDTO app = applicationAssertHelper.applicationNotExisted(id)

        when:
        def result = controller.update(1, id, app)

        then:
        result.statusCode.is2xxSuccessful()
    }

    def "PagingQuery"() {
        given:
//        PageRequest pageRequest = new PageRequest(0, 10)

        when:
        def result = controller.pagingQuery(1L, 0, 10,false, new ApplicationQuery())
        then:
        result.statusCode.is2xxSuccessful()
        result.body.list.size() > 0
    }

    def "Enabled"() {
        when:
        controller.disable(id)
        then:
        noExceptionThrown()
    }

    def "Disable"() {
        when:
        controller.enabled(id)
        then:
        noExceptionThrown()
    }

    def "Types"() {
        when:
        def result = controller.types()
        then:
        result.body.contains("test")

    }

    def "Check"() {
        given:
        ApplicationDTO dto = new ApplicationDTO()
        dto.setName("nnn")
        dto.setOrganizationId(1L)

        when: "插入校验name"
        controller.check(1L, dto)

        then:
        noExceptionThrown()

        when: "更新校验name"
        dto.setId(1)
        controller.check(1L, dto)

        then:
        noExceptionThrown()

        when: "更新校验code"
        dto.setName(null)
        dto.setCode("ccc")
        controller.check(1L, dto)

        then:
        noExceptionThrown()

        when: "插入校验code"
        dto.setId(null)
        controller.check(1L, dto)

        then:
        noExceptionThrown()
    }

    def "addToCombination"() {
        given: "初始化5个组合应用和一个普通应用"
        ApplicationDTO dto = new ApplicationDTO()
        dto.setOrganizationId(1L)
        dto.setProjectId(0L)
        dto.setEnabled(true)
        dto.setApplicationType(ApplicationType.DEVELOPMENT.code())
        for (int i = 0; i < 5; i++) {
            dto.setId((i + 1) * 100)
            dto.setName(i + "")
            dto.setCode(i + "")
            dto.setApplicationCategory(ApplicationCategory.COMBINATION.code())
            controller.create(1L, dto)
        }
        dto.setName("n123")
        dto.setCode("c123")
        dto.setId(600L)
        dto.setApplicationCategory(ApplicationCategory.APPLICATION.code())
        controller.create(1L, dto)

        when: "添加组"
        def ids = [200L, 300L] as Long[]
        controller.addToCombination(1L, 100L, ids)
        then:
        noExceptionThrown()

        when: "移除300"
        ids = [200L] as Long[]
        controller.addToCombination(1L, 100L, ids)
        then:
        noExceptionThrown()

        when: "添加自己"
        ids = [100L] as Long[]
        controller.addToCombination(1L, 100L, ids)
        then:
        thrown(CommonException)
    }

    def "queryDescendant"() {
        when:
        def result = controller.queryDescendant(1L, 100L)
        then:
        result.statusCode.is2xxSuccessful()
        result.body.size() == 2
    }

    def "queryEnabledApplication"() {
        when:
        def result = controller.queryEnabledApplication(1, 100)
        then:
        result.statusCode.is2xxSuccessful()
    }

    def "queryApplicationList"() {
        given:
//        PageRequest pageRequest = new PageRequest(0, 10)

        when:
        def result = controller.queryApplicationList(0, 10, 1L, 100L, null, null)

        then:
        result.statusCode.is2xxSuccessful()
    }

    def "query"() {
        when:
        def result = controller.query(1, 100, false)

        then:
        result.statusCode.is2xxSuccessful()
        result.body.getName() == "0"
    }
}
