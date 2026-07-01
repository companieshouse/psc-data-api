package uk.gov.companieshouse.pscdataapi.kafka;

public interface PscMergeProducer {
    void invokePscMerge(String pscId, String previousPscId); 
}
