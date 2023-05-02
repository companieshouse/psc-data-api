package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class NameElements {

    @Field("title")
    private String title;

    @Field("forename")
    private String forename;

    @Field("surname")
    private String surname;

    @Field("middle_name")
    private String middleName;

    public NameElements() {}

    /**
     * Contructor using SDK NameElements.
     * @param nameElements API NameElements object.
     */
    public NameElements(uk.gov.companieshouse.api.psc.NameElements nameElements) {
        this.forename = nameElements.getForename();
        this.surname = nameElements.getSurname();
        this.middleName = nameElements.getMiddleName();
        this.title = nameElements.getTitle();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Override
    public String toString() {
        return "NameElements{"
                + "title='"
                + title
                + '\''
                + ", forename='"
                + forename
                + '\''
                + ", surname='"
                + surname
                + '\''
                + ", middleName='"
                + middleName
                + '\''
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
        NameElements that = (NameElements) object;
        return Objects.equals(title, that.title)
                && Objects.equals(forename, that.forename)
                && Objects.equals(surname, that.surname)
                && Objects.equals(middleName, that.middleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, forename, surname, middleName);
    }
}
