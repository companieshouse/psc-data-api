package uk.gov.companieshouse.pscdataapi.pscnotifications;

import java.util.List;

record Filter (boolean isFilterEnabled, List<String> filterStatuses) {}