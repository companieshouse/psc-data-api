package uk.gov.companieshouse.pscdataapi.pscnotifications;

record PscNotificationsRequest (String pscId, String filter, Integer startIndex, Integer itemsPerPage,
                                String authPrivileges) {

    PscNotificationsRequest (String pscId, String filter, Integer startIndex, Integer itemsPerPage) {
        this(pscId, filter, startIndex, itemsPerPage, null);
    }

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {

        private String pscId;
        private String filter;
        private Integer startIndex;
        private Integer itemsPerPage;
        private String authPrivileges;

        private Builder(){}

        Builder pscId(String pscId){
            this.pscId = pscId;
            return this;
        }

        Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        Builder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        Builder itemsPerPage(Integer itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        Builder authPrivileges(String authPrivileges) {
            this.authPrivileges = authPrivileges;
            return this;
        }

        PscNotificationsRequest build() {
            return new PscNotificationsRequest(pscId, filter, startIndex, itemsPerPage, authPrivileges);
        }
    }
}
