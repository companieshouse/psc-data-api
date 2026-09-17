package uk.gov.companieshouse.pscdataapi.pscnotifications;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.pscdataapi.models.PscDocument;

@Repository
interface PscNotificationsRepository extends MongoRepository<PscDocument, String> {

    List<PscDocument> findAll();

    @Aggregation(pipeline = {
            "{ $match: { "
                    + "$and: [ "
                    + "{'psc_id': ?0 },"
                    + "{ $or: [ "
                    + "{ 'data.ceased_on': { $exists: false } },"
                    + "{ 'data.ceased_on': { $not: { $exists: ?1 } } }"
                    + "] },"
                    + "{ 'company_status': { $nin: ?2 } }"
                    + "]"
                    + "}"
                    + "}",
            "{ $facet: {"
                    + "'active': ["
                    + "{ $match: { 'data.ceased_on': { $exists: false } } },"
                    + "{ $sort: { 'data.notified_on': -1 } },"
                    + "{ $project: { '_id': 1 } }"
                    + "],"
                    + "'ceased': ["
                    + "{ $match: { 'data.ceased_on': { $exists: true } } },"
                    + "{ $sort: { 'data.ceased_on': -1 } },"
                    + "{ $project: { '_id': 1 } }"
                    + "]"
                    + "} }",
            "{ $project: { "
                    + "'ids': { $slice: ["
                    + "{ $concatArrays: ['$active._id', '$ceased._id'] }, ?3, ?4"
                    + "] }"
                    + "} }"
    })
    @Meta(allowDiskUse = true)
    PscNotificationIds findPscNotificationsIds(String pscId, boolean filterEnabled,
            List<String> filterStatuses, int startIndex, int pageSize);

    @Aggregation(pipeline = {
            "{ $match: { "
                    + "$and: [ "
                    + "{'psc_id': ?0 },"
                    + "{ $or: [ "
                    + "{ 'data.ceased_on': { $exists: false } },"
                    + "{ 'data.ceased_on': { $not: { $exists: ?1 } } }"
                    + "]"
                    + "},"
                    + "{ 'company_status': { $nin: ?2 } }"
                    + "]"
                    + "}"
                    + "}",
            "{ $sort:  {'data.notified_on': -1, 'data.notified_before': -1} }",
            "{ $skip: ?3 }",
            "{ $limit: ?4 }"
    })

    @Meta(allowDiskUse = true)
    List<PscDocument> findRecentPscNotifications(String pscId, boolean filterEnabled,
            List<String> filterStatuses, int startIndex, int pageSize);

    @Aggregation(pipeline = {
            "{ $match: { _id: { $in: ?0 } } }",

            "{ $addFields: {"
                    + "'__sort_order__': { $indexOfArray: [?0, '$_id'] }"
                    + "}"
                    + "}",

            "{ $sort: { '__sort_order__': 1 } }"
    })

    List<PscDocument> findFullPscNotifications(Iterable<String> ids);

    @Query(value = "{ $and: [ "
            + "{'psc_id': ?0 },"
            + "{ $or: [ "
            + "{ 'data.ceased_on': { $exists: false } },"
            + "{ 'data.ceased_on': { $not: { $exists: ?1 } } }"
            + "] },"
            + "{ 'company_status': { $nin: ?2 } }"
            + "] }", count = true)
    int countTotal(String pscId, boolean filterEnabled, List<String> filterStatuses);

    @Query(value = "{ $and: [ "
            + "{ 'psc_id' : ?0 },"
            + "{'data.ceased_on': { $exists: true } } "
            + "] }", count = true)
    int countCeased(String pscId);

    @Query(value = "{ $and: [ "
            + "{ 'psc_id' : ?0 },"
            + "{ 'data.ceased_on': { $exists: false } },"
            + "{ 'company_status': { $in: ['dissolved', 'closed', 'converted-closed'] } }"
            + "] }", count = true)
    int countInactive(String pscId);

    @Aggregation(pipeline = {
            "{ $match: { 'psc_id': ?0 } }",
            "{ $sort:  {'data.notified_on': -1, 'data.notified_before': -1} }",
            "{ $limit: 1 }"
    })
    PscDocument findLatestPscNotification(String pscId);

    int countByPscId(String pscId);
}
