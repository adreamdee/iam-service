package io.choerodon.iam.api.eventhandler

import com.fasterxml.jackson.databind.ObjectMapper
import io.choerodon.iam.IntegrationTestConfiguration
import io.choerodon.iam.api.dto.payload.UserEventPayload
import io.choerodon.iam.domain.service.IUserService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author dengyouquan
 * */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class NotifyListenerSpec extends Specification {
    private IUserService iUserService = Mock(IUserService)
    private NotifyListener notifyListener = new NotifyListener(iUserService)
    private final ObjectMapper mapper = new ObjectMapper()

    def "Create"() {
        given: "构造请求参数"
        UserEventPayload eventPayload = new UserEventPayload()
        eventPayload.setFromUserId(1L)
        List<UserEventPayload> userEventPayloads = new ArrayList<>()
        userEventPayloads.add(eventPayload)
        String message = mapper.writeValueAsString(userEventPayloads)

        when: "调用方法"
        notifyListener.create(message)

        then: "校验结果"
        1 * iUserService.sendNotice(_, _, _, _, _)
        0 * _
    }
}
