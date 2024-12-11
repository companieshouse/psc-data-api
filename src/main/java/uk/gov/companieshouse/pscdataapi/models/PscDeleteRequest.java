package uk.gov.companieshouse.pscdataapi.models;

public record PscDeleteRequest (String companyNumber, String notificationId, String contextId, String kind, String deltaAt) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {

        private String companyNumber;
        private String notificationId;
        private String contextId;
        private String kind;
        private String deltaAt;

        private Builder() {
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder deltaAt(String deltaAt) {
            this.deltaAt = deltaAt;
            return this;
        }

        public PscDeleteRequest build() {
            return new PscDeleteRequest(companyNumber, notificationId, contextId, kind, deltaAt);
        }
    }
}
