package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TestCaseInfo {

    @Expose
    private Integer id;
    @Expose
    private String title;
    @SerializedName("section_id")
    @Expose
    private Integer sectionId;
    @SerializedName("type_id")
    @Expose
    private Integer typeId;
    @SerializedName("priority_id")
    @Expose
    private Integer priorityId;
    @SerializedName("milestone_id")
    @Expose
    private Object milestoneId;
    @Expose
    private Object refs;
    @SerializedName("created_by")
    @Expose
    private Integer createdBy;
    @SerializedName("created_on")
    @Expose
    private Integer createdOn;
    @SerializedName("updated_by")
    @Expose
    private Integer updatedBy;
    @SerializedName("updated_on")
    @Expose
    private Integer updatedOn;
    @Expose
    private Object estimate;
    @SerializedName("estimate_forecast")
    @Expose
    private Object estimateForecast;
    @SerializedName("suite_id")
    @Expose
    private Integer suiteId;
    @SerializedName("custom_preconds")
    @Expose
    private String customPreconds;
    @SerializedName("custom_steps")
    @Expose
    private String customSteps;
    @SerializedName("custom_expected")
    @Expose
    private String customExpected;

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The sectionId
     */
    public Integer getSectionId() {
        return sectionId;
    }

    /**
     *
     * @param sectionId
     * The section_id
     */
    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    /**
     *
     * @return
     * The typeId
     */
    public Integer getTypeId() {
        return typeId;
    }

    /**
     *
     * @param typeId
     * The type_id
     */
    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    /**
     *
     * @return
     * The priorityId
     */
    public Integer getPriorityId() {
        return priorityId;
    }

    /**
     *
     * @param priorityId
     * The priority_id
     */
    public void setPriorityId(Integer priorityId) {
        this.priorityId = priorityId;
    }

    /**
     *
     * @return
     * The milestoneId
     */
    public Object getMilestoneId() {
        return milestoneId;
    }

    /**
     *
     * @param milestoneId
     * The milestone_id
     */
    public void setMilestoneId(Object milestoneId) {
        this.milestoneId = milestoneId;
    }

    /**
     *
     * @return
     * The refs
     */
    public Object getRefs() {
        return refs;
    }

    /**
     *
     * @param refs
     * The refs
     */
    public void setRefs(Object refs) {
        this.refs = refs;
    }

    /**
     *
     * @return
     * The createdBy
     */
    public Integer getCreatedBy() {
        return createdBy;
    }

    /**
     *
     * @param createdBy
     * The created_by
     */
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    /**
     *
     * @return
     * The createdOn
     */
    public Integer getCreatedOn() {
        return createdOn;
    }

    /**
     *
     * @param createdOn
     * The created_on
     */
    public void setCreatedOn(Integer createdOn) {
        this.createdOn = createdOn;
    }

    /**
     *
     * @return
     * The updatedBy
     */
    public Integer getUpdatedBy() {
        return updatedBy;
    }

    /**
     *
     * @param updatedBy
     * The updated_by
     */
    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     *
     * @return
     * The updatedOn
     */
    public Integer getUpdatedOn() {
        return updatedOn;
    }

    /**
     *
     * @param updatedOn
     * The updated_on
     */
    public void setUpdatedOn(Integer updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     *
     * @return
     * The estimate
     */
    public Object getEstimate() {
        return estimate;
    }

    /**
     *
     * @param estimate
     * The estimate
     */
    public void setEstimate(Object estimate) {
        this.estimate = estimate;
    }

    /**
     *
     * @return
     * The estimateForecast
     */
    public Object getEstimateForecast() {
        return estimateForecast;
    }

    /**
     *
     * @param estimateForecast
     * The estimate_forecast
     */
    public void setEstimateForecast(Object estimateForecast) {
        this.estimateForecast = estimateForecast;
    }

    /**
     *
     * @return
     * The suiteId
     */
    public Integer getSuiteId() {
        return suiteId;
    }

    /**
     *
     * @param suiteId
     * The suite_id
     */
    public void setSuiteId(Integer suiteId) {
        this.suiteId = suiteId;
    }

    /**
     *
     * @return
     * The customPreconds
     */
    public String getCustomPreconds() {
        return customPreconds;
    }

    /**
     *
     * @param customPreconds
     * The custom_preconds
     */
    public void setCustomPreconds(String customPreconds) {
        this.customPreconds = customPreconds;
    }

    /**
     *
     * @return
     * The customSteps
     */
    public String getCustomSteps() {
        return customSteps;
    }

    /**
     *
     * @param customSteps
     * The custom_steps
     */
    public void setCustomSteps(String customSteps) {
        this.customSteps = customSteps;
    }

    /**
     *
     * @return
     * The customExpected
     */
    public String getCustomExpected() {
        return customExpected;
    }

    /**
     *
     * @param customExpected
     * The custom_expected
     */
    public void setCustomExpected(String customExpected) {
        this.customExpected = customExpected;
    }

}