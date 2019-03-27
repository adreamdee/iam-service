package io.choerodon.iam.api.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author wuguokai
 */
public class OrganizationDTO {
    private static final String CODE_REGULAR_EXPRESSION
            = "^[a-z](([a-z0-9]|-(?!-))*[a-z0-9])*$";
    @ApiModelProperty(value = "主键/非必填")
    private Long id;
    
    @ApiModelProperty(value = "组织名/必填")
    @NotEmpty(message = "error.organization.name.empty")
    @Size(min = 1, max = 32, message = "error.organization.name.size")
    private String name;
    
    @ApiModelProperty(value = "组织编码/必填")
    @NotEmpty(message = "error.code.empty")
    @Pattern(regexp = CODE_REGULAR_EXPRESSION, message = "error.code.illegal")
    @Size(min = 1, max = 15, message = "error.organization.code.size")
    private String code;
    
    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;
    
    @ApiModelProperty(value = "是否启用/非必填/默认：true")
    private Boolean enabled;

    @ApiModelProperty(value = "项目数量")
    private Integer projectCount;

    @ApiModelProperty(value = "组织图标url")
    private String imageUrl;

    private String ownerLoginName;

    private String ownerRealName;

    private String ownerPhone;

    private String ownerEmail;

    private Boolean isInto = true;

    private List<ProjectDTO> projects;

    private List<RoleDTO> roles;

    private Long userId;

    private String address;

    public Boolean getInto() {
        return isInto;
    }

    public void setInto(Boolean into) {
        isInto = into;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Integer projectCount) {
        this.projectCount = projectCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOwnerLoginName() {
        return ownerLoginName;
    }

    public void setOwnerLoginName(String ownerLoginName) {
        this.ownerLoginName = ownerLoginName;
    }

    public String getOwnerRealName() {
        return ownerRealName;
    }

    public void setOwnerRealName(String ownerRealName) {
        this.ownerRealName = ownerRealName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
