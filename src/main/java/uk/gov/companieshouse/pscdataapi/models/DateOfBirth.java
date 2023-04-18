package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class DateOfBirth {

    @Field("day")
    private Integer day;
    @Field("month")
    private Integer month;
    @Field("year")
    private Integer year;

    public DateOfBirth() {}

    /**
     * Contructor using SDK DoB.
     * @param dob API DoB object.
     */
    public DateOfBirth(uk.gov.companieshouse.api.psc.DateOfBirth dob) {
        this.day = dob.getDay();
        this.month = dob.getMonth();
        this.year = dob.getYear();
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "DateOfBirth{"
                + "day="
                + day
                + ", month="
                + month
                + ", year="
                + year
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
        DateOfBirth that = (DateOfBirth) object;
        return Objects.equals(day, that.day)
                && Objects.equals(month, that.month)
                && Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, month, year);
    }
}
