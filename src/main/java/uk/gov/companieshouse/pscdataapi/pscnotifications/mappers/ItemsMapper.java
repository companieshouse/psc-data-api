package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.NotifiedTo;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class ItemsMapper {

    private final AddressMapper addressMapper;
    private final IdentificationMapper identificationMapper;
    private final IdentityVerificationDetailsMapper identityVerificationDetailsMapper;
    private final NameElementsMapper nameElementsMapper;

    ItemsMapper(AddressMapper addressMapper,
                IdentificationMapper identificationMapper,
                IdentityVerificationDetailsMapper identityVerificationDetailsMapper,
                NameElementsMapper nameElementsMapper) {
        this.addressMapper = addressMapper;
        this.identificationMapper = identificationMapper;
        this.identityVerificationDetailsMapper = identityVerificationDetailsMapper;
        this.nameElementsMapper = nameElementsMapper;
    }

    public List<PscNotificationSummary> map(List<PscDocument> notifications) {
        return (List<PscNotificationSummary>) notifications.stream()
                .map(notification -> ofNullable(notification.getData())
                    .map(data -> new PscNotificationSummary()
                        .address(addressMapper.map(data.getAddress()))
                        .ceasedOn(data.getCeasedOn())
                        .countryOfResidence(data.getCountryOfResidence())
                        .etag(data.getEtag())
                        .identification(identificationMapper.map(data.getIdentification()))
                        .identityVerificationDetails(identityVerificationDetailsMapper.map(data.getIdentityVerificationDetails()))
                        .isSanctioned(data.getSanctioned())
                        .kind(PscNotificationSummary.KindEnum.fromValue(data.getKind()))
                        .links(data.getLinks())
                        .name(data.getName())
                        .nameElements(nameElementsMapper.map(data.getNameElements()))
                        .nationality(data.getNationality())
                        .naturesOfControl(data.getNaturesOfControl())
                        .notifiedOn(data.getNotifiedOn().toString())
                        .notifiedTo(new NotifiedTo()
                                .companyNumber(notification.getCompanyNumber()))
                        .principalOfficeAddress(addressMapper.map(data.getPrincipalOfficeAddress()))));
    }

}
