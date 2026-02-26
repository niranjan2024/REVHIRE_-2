package com.revhire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmployerCompanyProfileRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String companyName;

    @NotBlank
    @Size(min = 3, max = 120)
    private String industry;

    @NotBlank
    @Size(max = 50)
    private String companySize;

    @Size(max = 1000)
    private String companyDescription;

    @Size(max = 255)
    private String website;

    @Size(max = 120)
    private String companyLocation;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getCompanySize() {
        return companySize;
    }

    public void setCompanySize(String companySize) {
        this.companySize = companySize;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCompanyLocation() {
        return companyLocation;
    }

    public void setCompanyLocation(String companyLocation) {
        this.companyLocation = companyLocation;
    }
}
