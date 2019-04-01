package io.choerodon.iam.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.excel.ExcelReadConfig;
import io.choerodon.core.excel.ExcelReadHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.iam.api.dto.*;
import io.choerodon.iam.app.service.RoleMemberService;
import io.choerodon.iam.domain.iam.entity.MemberRoleE;
import io.choerodon.iam.domain.repository.MemberRoleRepository;
import io.choerodon.iam.domain.repository.UploadHistoryRepository;
import io.choerodon.iam.domain.service.IRoleMemberService;
import io.choerodon.iam.infra.common.utils.ParamUtils;
import io.choerodon.iam.infra.common.utils.excel.ExcelImportUserTask;
import io.choerodon.iam.infra.dataobject.ClientDO;
import io.choerodon.iam.infra.dataobject.UploadHistoryDO;
import io.choerodon.iam.infra.enums.ExcelSuffix;
import io.choerodon.iam.infra.enums.MemberType;
import io.choerodon.iam.infra.mapper.OrganizationMapper;
import io.choerodon.iam.infra.mapper.ProjectMapper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author superlee
 * @author wuguokai
 * @author zmf
 */
@Component
public class RoleMemberServiceImpl implements RoleMemberService {

    private final Logger logger = LoggerFactory.getLogger(RoleMemberServiceImpl.class);

    private MemberRoleRepository memberRoleRepository;
    private IRoleMemberService iRoleMemberService;
    private UploadHistoryRepository uploadHistoryRepository;
    private ExcelImportUserTask excelImportUserTask;
    private OrganizationMapper organizationMapper;
    private ProjectMapper projectMapper;
    private ExcelImportUserTask.FinishFallback finishFallback;

    public RoleMemberServiceImpl(IRoleMemberService iRoleMemberService,
                                 UploadHistoryRepository uploadHistoryRepository,
                                 ExcelImportUserTask excelImportUserTask,
                                 ExcelImportUserTask.FinishFallback finishFallback,
                                 OrganizationMapper organizationMapper,
                                 ProjectMapper projectMapper,
                                 MemberRoleRepository memberRoleRepository) {
        this.iRoleMemberService = iRoleMemberService;
        this.uploadHistoryRepository = uploadHistoryRepository;
        this.excelImportUserTask = excelImportUserTask;
        this.finishFallback = finishFallback;
        this.organizationMapper = organizationMapper;
        this.projectMapper = projectMapper;
        this.memberRoleRepository = memberRoleRepository;
    }


    @Transactional(rollbackFor = CommonException.class)
    @Override
    public List<MemberRoleDTO> createOrUpdateRolesByMemberIdOnSiteLevel(Boolean isEdit, List<Long> memberIds, List<MemberRoleDTO> memberRoleDTOList, String memberType) {
        List<MemberRoleDTO> memberRoleDTOS = new ArrayList<>();

        memberType = validate(memberRoleDTOList, memberType);
        // member type 为 'client' 时
        if (memberType != null && memberType.equals(MemberType.CLIENT.value())) {
            for (Long memberId : memberIds) {
                memberRoleDTOList.forEach(m ->
                        m.setMemberId(memberId)
                );
                memberRoleDTOS.addAll(ConvertHelper.convertList(
                        iRoleMemberService.insertOrUpdateRolesOfClientByMemberId(isEdit, 0L, memberId,
                                ConvertHelper.convertList(memberRoleDTOList, MemberRoleE.class),
                                ResourceLevel.SITE.value()), MemberRoleDTO.class));
            }
            return memberRoleDTOS;
        }

        // member type 为 'user' 时
        for (Long memberId : memberIds) {
            memberRoleDTOList.forEach(m ->
                    m.setMemberId(memberId)
            );
            memberRoleDTOS.addAll(ConvertHelper.convertList(
                    iRoleMemberService.insertOrUpdateRolesOfUserByMemberId(isEdit, 0L, memberId, ConvertHelper.convertList(
                            memberRoleDTOList, MemberRoleE.class), ResourceLevel.SITE.value()), MemberRoleDTO.class));
        }
        return memberRoleDTOS;
    }

    @Transactional(rollbackFor = CommonException.class)
    @Override
    public List<MemberRoleDTO> createOrUpdateRolesByMemberIdOnOrganizationLevel(Boolean isEdit, Long organizationId, List<Long> memberIds, List<MemberRoleDTO> memberRoleDTOList, String memberType) {
        List<MemberRoleDTO> memberRoleDTOS = new ArrayList<>();

        memberType = validate(memberRoleDTOList, memberType);

        // member type 为 'client' 时
        if (memberType != null && memberType.equals(MemberType.CLIENT.value())) {
            for (Long memberId : memberIds) {
                memberRoleDTOList.forEach(m ->
                        m.setMemberId(memberId)
                );
                memberRoleDTOS.addAll(ConvertHelper.convertList(
                        iRoleMemberService.insertOrUpdateRolesOfClientByMemberId(isEdit, organizationId, memberId,
                                ConvertHelper.convertList(memberRoleDTOList, MemberRoleE.class),
                                ResourceLevel.ORGANIZATION.value()), MemberRoleDTO.class));
            }
            return memberRoleDTOS;
        }

        // member type 为 'user' 时
        for (Long memberId : memberIds) {
            memberRoleDTOList.forEach(m ->
                    m.setMemberId(memberId)
            );
            memberRoleDTOS.addAll(ConvertHelper.convertList(
                    iRoleMemberService.insertOrUpdateRolesOfUserByMemberId(isEdit, organizationId, memberId,
                            ConvertHelper.convertList(memberRoleDTOList, MemberRoleE.class),
                            ResourceLevel.ORGANIZATION.value()), MemberRoleDTO.class));
        }
        return memberRoleDTOS;
    }

    private String validate(List<MemberRoleDTO> memberRoleDTOList, String memberType) {
        if (memberType == null && memberRoleDTOList != null && !memberRoleDTOList.isEmpty()) {
            memberType = memberRoleDTOList.get(0).getMemberType();
        }
        if (memberRoleDTOList == null) {
            throw new CommonException("error.memberRole.null");
        }
        return memberType;
    }

    @Override
    public Page<ClientWithRoleDTO> pagingQueryClientsWithOrganizationLevelRoles(PageRequest pageRequest, ClientRoleSearchDTO clientRoleSearchDTO, Long sourceId) {
        String param = ParamUtils.arrToStr(clientRoleSearchDTO.getParam());
        Page<ClientDO> page = memberRoleRepository.pagingQueryClientsWithOrganizationLevelRoles(pageRequest, clientRoleSearchDTO, sourceId, param);
        return convert(page);
    }

    @Override
    public Page<ClientWithRoleDTO> pagingQueryClientsWithSiteLevelRoles(PageRequest pageRequest, ClientRoleSearchDTO clientRoleSearchDTO) {
        String param = ParamUtils.arrToStr(clientRoleSearchDTO.getParam());
        Page<ClientDO> page = memberRoleRepository.pagingQueryClientsWithSiteLevelRoles(pageRequest, clientRoleSearchDTO, param);
        return convert(page);
    }

    @Override
    public Page<ClientWithRoleDTO> pagingQueryClientsWithProjectLevelRoles(PageRequest pageRequest, ClientRoleSearchDTO clientRoleSearchDTO, Long sourceId) {
        String param = ParamUtils.arrToStr(clientRoleSearchDTO.getParam());
        Page<ClientDO> page = memberRoleRepository.pagingQueryClientsWithProjectLevelRoles(pageRequest, clientRoleSearchDTO, sourceId, param);
        return convert(page);
    }

    /**
     * 转化 do 和 dto
     *
     * @param origin 被转化
     * @return 转化后
     */
    private Page<ClientWithRoleDTO> convert(Page<ClientDO> origin) {
        Page<ClientWithRoleDTO> newPage = new Page<>();
        BeanUtils.copyProperties(origin, newPage, "content");
        newPage.setContent(origin.getContent().stream().map(clientDO -> {
            ClientWithRoleDTO dto = new ClientWithRoleDTO();
            BeanUtils.copyProperties(clientDO, dto, "roles");
            dto.setRoles(clientDO.getRoles());
            return dto;
        }).collect(Collectors.toList()));
        return newPage;
    }

    @Transactional(rollbackFor = CommonException.class)
    @Override
    public List<MemberRoleDTO> createOrUpdateRolesByMemberIdOnProjectLevel(Boolean isEdit, Long projectId, List<Long> memberIds, List<MemberRoleDTO> memberRoleDTOList, String memberType) {
        List<MemberRoleDTO> memberRoleDTOS = new ArrayList<>();

        memberType = validate(memberRoleDTOList, memberType);

        // member type 为 'client' 时
        if (memberType != null && memberType.equals(MemberType.CLIENT.value())) {
            for (Long memberId : memberIds) {
                memberRoleDTOList.forEach(m ->
                        m.setMemberId(memberId)
                );
                memberRoleDTOS.addAll(ConvertHelper.convertList(
                        iRoleMemberService.insertOrUpdateRolesOfClientByMemberId(isEdit, projectId, memberId,
                                ConvertHelper.convertList(memberRoleDTOList, MemberRoleE.class),
                                ResourceLevel.PROJECT.value()), MemberRoleDTO.class));
            }
            return memberRoleDTOS;
        }

        // member type 为 'user' 时
        for (Long memberId : memberIds) {
            memberRoleDTOList.forEach(m ->
                    m.setMemberId(memberId)
            );
            memberRoleDTOS.addAll(ConvertHelper.convertList(
                    iRoleMemberService.insertOrUpdateRolesOfUserByMemberId(isEdit, projectId, memberId,
                            ConvertHelper.convertList(memberRoleDTOList, MemberRoleE.class),
                            ResourceLevel.PROJECT.value()), MemberRoleDTO.class));
        }
        return memberRoleDTOS;
    }

    @Transactional(rollbackFor = CommonException.class)
    @Override
    public void deleteOnSiteLevel(RoleAssignmentDeleteDTO roleAssignmentDeleteDTO) {
        String memberType = roleAssignmentDeleteDTO.getMemberType();
        if (memberType != null && memberType.equals(MemberType.CLIENT.value())) {
            iRoleMemberService.deleteClientAndRole(roleAssignmentDeleteDTO, ResourceLevel.SITE.value());
            return;
        }
        iRoleMemberService.delete(roleAssignmentDeleteDTO, ResourceLevel.SITE.value());
    }

    @Transactional(rollbackFor = CommonException.class)
    @Override
    public void deleteOnOrganizationLevel(RoleAssignmentDeleteDTO roleAssignmentDeleteDTO) {
        String memberType = roleAssignmentDeleteDTO.getMemberType();
        if (memberType != null && memberType.equals(MemberType.CLIENT.value())) {
            iRoleMemberService.deleteClientAndRole(roleAssignmentDeleteDTO, ResourceLevel.ORGANIZATION.value());
            return;
        }
        iRoleMemberService.delete(roleAssignmentDeleteDTO, ResourceLevel.ORGANIZATION.value());
    }

    @Override
    public void deleteOnProjectLevel(RoleAssignmentDeleteDTO roleAssignmentDeleteDTO) {
        String memberType = roleAssignmentDeleteDTO.getMemberType();
        if (memberType != null && memberType.equals(MemberType.CLIENT.value())) {
            iRoleMemberService.deleteClientAndRole(roleAssignmentDeleteDTO, ResourceLevel.PROJECT.value());
            return;
        }
        iRoleMemberService.delete(roleAssignmentDeleteDTO, ResourceLevel.PROJECT.value());
    }

    @Override
    public ResponseEntity<Resource> downloadTemplates(String suffix) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("charset", "utf-8");
        //设置下载文件名
        String filename = "用户角色关系导入模板." + suffix;
        try {
            filename = URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.info("url encodes exception: {}", e.getMessage());
            throw new CommonException("error.encode.url");
        }
        headers.add("Content-Disposition", "attachment;filename=\"" + filename + "\"");
        InputStream inputStream;
        if (ExcelSuffix.XLS.value().equals(suffix)) {
            inputStream = this.getClass().getResourceAsStream("/templates/memberRoleTemplates.xls");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(inputStream));
        } else if (ExcelSuffix.XLSX.value().equals(suffix)) {
            inputStream = this.getClass().getResourceAsStream("/templates/memberRoleTemplates.xlsx");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(inputStream));
        } else {
            return null;
        }
    }

    @Override
    public void import2MemberRole(Long sourceId, String sourceType, MultipartFile file) {
        validateSourceId(sourceId, sourceType);
        ExcelReadConfig excelReadConfig = initExcelReadConfig();
        long begin = System.currentTimeMillis();
        try {
            List<ExcelMemberRoleDTO> memberRoles = ExcelReadHelper.read(file, ExcelMemberRoleDTO.class, excelReadConfig);
            if (memberRoles.isEmpty()) {
                throw new CommonException("error.excel.memberRole.empty");
            }
            UploadHistoryDO uploadHistory = initUploadHistory(sourceId, sourceType);
            long end = System.currentTimeMillis();
            logger.info("read excel for {} millisecond", (end - begin));
            excelImportUserTask.importMemberRole(memberRoles, uploadHistory, finishFallback);
        } catch (IOException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new CommonException("error.excel.read", e);
        } catch (IllegalArgumentException e) {
            throw new CommonException("error.excel.illegal.column", e);
        }
    }

    private UploadHistoryDO initUploadHistory(Long sourceId, String sourceType) {
        UploadHistoryDO uploadHistory = new UploadHistoryDO();
        uploadHistory.setBeginTime(new Date(System.currentTimeMillis()));
        uploadHistory.setType("member-role");
        uploadHistory.setUserId(DetailsHelper.getUserDetails().getUserId());
        uploadHistory.setSourceId(sourceId);
        uploadHistory.setSourceType(sourceType);
        uploadHistoryRepository.insertSelective(uploadHistory);
        return uploadHistory;
    }

    private void validateSourceId(Long sourceId, String sourceType) {
        if (ResourceLevel.ORGANIZATION.value().equals(sourceType)
                && organizationMapper.selectByPrimaryKey(sourceId) == null) {
            throw new CommonException("error.organization.not.exist");
        }
        if (ResourceLevel.PROJECT.value().equals(sourceType)
                && projectMapper.selectByPrimaryKey(sourceId) == null) {
            throw new CommonException("error.project.not.exist");
        }
    }

    private ExcelReadConfig initExcelReadConfig() {
        ExcelReadConfig excelReadConfig = new ExcelReadConfig();
        String[] skipSheetNames = {"readme"};
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("登录名*", "loginName");
        propertyMap.put("角色编码*", "roleCode");
        excelReadConfig.setSkipSheetNames(skipSheetNames);
        excelReadConfig.setPropertyMap(propertyMap);
        return excelReadConfig;
    }

}
