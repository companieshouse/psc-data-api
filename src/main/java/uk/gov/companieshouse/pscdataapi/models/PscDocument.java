package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "delta_company_pscs")
public class PscDocument {

    @Id
    private String id;

    @Field("psc_id")
    private String pscId;

    @Field("previous_psc_id")
    private String previousPscId;

    @Field("delta_at")
    private String deltaAt;

    @Field("notification_id")
    private String notificationId;

    @Field("company_number")
    private String companyNumber;

    @Field("company_name")
    private String companyName;

    @Field("company_status")
    private String companyStatus;

    @Field("updated_by")
    private String updatedBy;

    @Field("created")
    private Created created;

    @Field("updated")
    private Updated updated;

    @Field("data")
    private PscData data;

    @Field("sensitive_data")
    private PscSensitiveData sensitiveData;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPscId() {
        return pscId;
    }

    public void setPscId(String pscId) {
        this.pscId = pscId;
    }

    public String getPreviousPscId() {
        return previousPscId;
    }

    public void setPreviousPscId(String previousPscId) {
        this.previousPscId = previousPscId;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public void setDeltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyStatus() {
        return companyStatus;
    }

    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }

    public Created getCreated() {
        return created;
    }

    public void setCreated(Created created) {
        this.created = created;
    }

    public Updated getUpdated() {
        return updated;
    }

    public void setUpdated(Updated updated) {
        this.updated = updated;
    }

    public PscData getData() {
        return data;
    }

    public void setData(PscData data) {
        this.data = data;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public PscSensitiveData getSensitiveData() {
        return sensitiveData;
    }

    public void setSensitiveData(PscSensitiveData sensitiveData) {
        this.sensitiveData = sensitiveData;
    }

    @Override
    public String toString() {
        return "PscDocument{"
                + "id='"
                + id
                + '\''
                + ", pscId='"
                + pscId
                + '\''
                + ", previousPscId='"
                + previousPscId
                + '\''
                + ", deltaAt='"
                + deltaAt
                + '\''
                + ", notificationId='"
                + notificationId
                + '\''
                + ", companyNumber='"
                + companyNumber
                + '\''
                + ", companyName='"
                + companyName
                + '\''
                + ", companyStatus='"
                + companyStatus
                + '\''
                + ", updatedBy='"
                + updatedBy
                + '\''
                + ", created="
                + created
                + ", updated="
                + updated
                + ", data="
                + data
                + ", sensitiveData="
                + sensitiveData
                + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PscDocument that = (PscDocument) object;
        return Objects.equals(id, that.id)
                && Objects.equals(pscId, that.pscId)
                && Objects.equals(previousPscId, that.previousPscId)
                && Objects.equals(deltaAt, that.deltaAt)
                && Objects.equals(notificationId, that.notificationId)
                && Objects.equals(companyNumber, that.companyNumber)
                && Objects.equals(companyName, that.companyName)
                && Objects.equals(companyStatus, that.companyStatus)
                && Objects.equals(updatedBy, that.updatedBy)
                && Objects.equals(created, that.created)
                && Objects.equals(updated, that.updated)
                && Objects.equals(data, that.data)
                && Objects.equals(sensitiveData, that.sensitiveData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pscId, previousPscId, deltaAt, notificationId, companyNumber, companyName, companyStatus,
                updatedBy, created, updated, data, sensitiveData);
    }
}
