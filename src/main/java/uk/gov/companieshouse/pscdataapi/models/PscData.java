package uk.gov.companieshouse.pscdataapi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

import java.util.Objects;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PscData {

    @JsonProperty("ceased_on")
    private LocalDate ceasedOn;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("name")
    private String name;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("country_of_residence")
    private String countryOfResidence;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("notified_on")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate notifiedOn;

    @JsonProperty("description")
    private String description;

    @JsonProperty("service_address_is_same_as_registered_office_address")
    private Boolean serviceAddressIsSameAsRegisteredOfficeAddress;

    @JsonProperty("is_sanctioned")
    private Boolean isSanctioned;

    @JsonProperty("ceased")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Boolean ceased;

    @JsonProperty("natures_of_control")
    private List<String> naturesOfControl;

    @JsonProperty("name_elements")
    private NameElements nameElements;

    @JsonProperty("links")
    private Links links;

    @JsonProperty("principal_office_address")
    private Address principalOfficeAddress;

    @JsonProperty("identification")
    private PscIdentification identification;

    public Address getPrincipalOfficeAddress() {
        return principalOfficeAddress;
    }

    public void setPrincipalOfficeAddress(Address principalOfficeAddress) {
        this.principalOfficeAddress = principalOfficeAddress;
    }

    public PscIdentification getIdentification() {
        return identification;
    }

    public void setIdentification(PscIdentification identification) {
        this.identification = identification;
    }

    public LocalDate getCeasedOn() {
        return ceasedOn;
    }

    public void setCeasedOn(LocalDate ceasedOn) {
        this.ceasedOn = ceasedOn;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getCountryOfResidence() {
        return countryOfResidence;
    }

    public void setCountryOfResidence(String countryOfResidence) {
        this.countryOfResidence = countryOfResidence;
    }

    public Boolean getServiceAddressIsSameAsRegisteredOfficeAddress() {
        return serviceAddressIsSameAsRegisteredOfficeAddress;
    }

    public void setServiceAddressIsSameAsRegisteredOfficeAddress(
            Boolean serviceAddressIsSameAsRegisteredOfficeAddress) {
        this.serviceAddressIsSameAsRegisteredOfficeAddress
                = serviceAddressIsSameAsRegisteredOfficeAddress;
    }

    public List<String> getNaturesOfControl() {
        return naturesOfControl;
    }

    public void setNaturesOfControl(List<String> naturesOfControl) {
        this.naturesOfControl = naturesOfControl;
    }

    public NameElements getNameElements() {
        return nameElements;
    }

    public void setNameElements(NameElements nameElements) {
        this.nameElements = nameElements;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public LocalDate getNotifiedOn() {
        return notifiedOn;
    }

    public void setNotifiedOn(LocalDate notifiedOn) {
        this.notifiedOn = notifiedOn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSanctioned() {
        return isSanctioned;
    }

    public void setSanctioned(Boolean sanctioned) {
        isSanctioned = sanctioned;
    }

    public Boolean getCeased() {
        return ceased;
    }

    public void setCeased(Boolean ceased) {
        this.ceased = ceased;
    }

    @Override
    public String toString() {
        return "PscData{"
                + "ceasedOn="
                + ceasedOn
                + ", etag='"
                + etag
                + '\''
                + ", address="
                + address
                + ", name='"
                + name
                + '\''
                + ", nationality='"
                + nationality
                + '\''
                + ", countryOfResidence='"
                + countryOfResidence
                + '\''
                + ", kind='"
                + kind
                + ", notifiedOn="
                + notifiedOn
                + '\''
                + ", description='"
                + description
                + '\''
                + ", serviceAddressIsSameAsRegisteredOfficeAddress="
                + serviceAddressIsSameAsRegisteredOfficeAddress
                + ", isSanctioned="
                + isSanctioned
                + ", ceased="
                + ceased
                + ", naturesOfControl="
                + naturesOfControl
                + ", nameElements="
                + nameElements
                + ", links="
                + links
                + ", identification="
                + identification
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
        PscData pscData = (PscData) object;
        return Objects.equals(ceasedOn, pscData.ceasedOn)
                && Objects.equals(etag, pscData.etag)
                && Objects.equals(address, pscData.address)
                && Objects.equals(name, pscData.name)
                && Objects.equals(nationality, pscData.nationality)
                && Objects.equals(countryOfResidence, pscData.countryOfResidence)
                && Objects.equals(kind, pscData.kind)
                && Objects.equals(notifiedOn, pscData.notifiedOn)
                && Objects.equals(description, pscData.description)
                && Objects.equals(serviceAddressIsSameAsRegisteredOfficeAddress,
                pscData.serviceAddressIsSameAsRegisteredOfficeAddress)
                && Objects.equals(isSanctioned, pscData.isSanctioned)
                && Objects.equals(ceased, pscData.ceased)
                && Objects.equals(naturesOfControl, pscData.naturesOfControl)
                && Objects.equals(nameElements, pscData.nameElements)
                && Objects.equals(links, pscData.links)
                && Objects.equals(identification, pscData.identification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ceasedOn, etag, address, name, nationality, countryOfResidence,
                kind, notifiedOn, description, serviceAddressIsSameAsRegisteredOfficeAddress,
                isSanctioned, ceased, naturesOfControl,
                nameElements, links, identification);
    }
}
