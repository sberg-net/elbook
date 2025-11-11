/*
 *  Copyright (C) 2023 sberg it-systeme GmbH
 *
 *  Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 *  European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package net.sberg.elbook.verzeichnisdienstcmpts.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VerzeichnisdienstService;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDConnector;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDProperties;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.EnumTriValue;
import net.sberg.elbook.verzeichnisdienstcmpts.fhir.client.TiFhirProperties;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerzeichnisdienstFhirService {

    private final TiVZDConnector tiVZDConnector;
    private final VerzeichnisdienstService verzeichnisdienstService;

    public boolean isFhirConnected(TiVZDProperties tiVZDProperties) throws Exception {
        TiFhirProperties tiFhirProperties = tiVZDConnector.extractTiFhirProperties(tiVZDProperties);
        return tiFhirProperties.isFhirConnected();
    }

    public Map lade(Mandant mandant, String resourceName, String uid) throws Exception {
        TiVZDProperties tiVZDProperties = mandant.createAndGetTiVZDProperties(verzeichnisdienstService);
        TiFhirProperties tiFhirProperties = tiVZDConnector.extractTiFhirProperties(tiVZDProperties);

        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient(tiFhirProperties.getResourceUri());
        client.registerInterceptor(new BearerTokenAuthInterceptor(tiFhirProperties.getHolderAccessToken().getAccessToken()));

        Class resourceClass = null;
        if (resourceName.equals(Organization.class.getSimpleName())) {
            resourceClass = Organization.class;
        }
        else if (resourceName.equals(HealthcareService.class.getSimpleName())) {
            resourceClass = HealthcareService.class;
        }
        else if (resourceName.equals(PractitionerRole.class.getSimpleName())) {
            resourceClass = PractitionerRole.class;
        }
        else if (resourceName.equals(Practitioner.class.getSimpleName())) {
            resourceClass = Practitioner.class;
        }
        else if (resourceName.equals(Endpoint.class.getSimpleName())) {
            resourceClass = Endpoint.class;
        }
        else if (resourceName.equals(Location.class.getSimpleName())) {
            resourceClass = Location.class;
        }

        DomainResource bundle = (DomainResource)client
            .read()
            .resource(resourceClass)
            .withId(uid)
            .execute();

        return new ObjectMapper().readValue(ctx.newJsonParser().encodeResourceToString(bundle), Map.class);
    }

    private FhirResource create(HealthcareService healthcareService, int order) {
        FhirResource fhirResource = new FhirResource();
        fhirResource.setResourceName("HealthcareService");
        fhirResource.setName("Daten des HealthcareService's");
        fhirResource.setUid(healthcareService.getIdPart());
        fhirResource.setOrder(order);

        FhirResourceAttribute fhirResourceAttribute1 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute1);
        fhirResourceAttribute1.setName("Typ");
        healthcareService.getType().forEach(codeableConcept -> {
            codeableConcept.getCoding().forEach(coding -> {
                if (!fhirResourceAttribute1.getValues().contains(coding.getCode()+" - "+coding.getDisplay())) {
                    fhirResourceAttribute1.getValues().add(coding.getCode()+" - "+coding.getDisplay());
                }
            });
        });

        FhirResourceAttribute fhirResourceAttribute2 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute2);
        fhirResourceAttribute2.setName("Services");
        healthcareService.getSpecialty().forEach(codeableConcept -> {
            codeableConcept.getCoding().forEach(coding -> {
                if (!fhirResourceAttribute2.getValues().contains(coding.getCode()+" - "+coding.getDisplay())) {
                    fhirResourceAttribute2.getValues().add(coding.getCode()+" - "+coding.getDisplay());
                }
            });
        });

        FhirResourceAttribute fhirResourceAttribute3 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute3);
        fhirResourceAttribute3.setName("Kontaktdaten");
        healthcareService.getTelecom().forEach(contactPoint -> {
            if (contactPoint.getValue().startsWith("http")) {
                String html = contactPoint.getSystem().getDisplay()+": "+"<a target=\"_blank\" href=\""+contactPoint.getValue()+"\">"+contactPoint.getValue()+"</a>";
                if (!fhirResourceAttribute3.getHtmlValues().contains(html)) {
                    fhirResourceAttribute3.getHtmlValues().add(html);
                }
            }
            else {
                if (!fhirResourceAttribute3.getValues().contains(contactPoint.getSystem().getDisplay()+": "+contactPoint.getValue())) {
                    fhirResourceAttribute3.getValues().add(contactPoint.getSystem().getDisplay()+": "+contactPoint.getValue());
                }
            }
        });

        FhirResourceAttribute fhirResourceAttribute4 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute4);
        fhirResourceAttribute4.setName("Öffnungszeiten");
        healthcareService.getAvailableTime().forEach(healthcareServiceAvailableTimeComponent -> {
            if (healthcareServiceAvailableTimeComponent.getAllDay()) {
                fhirResourceAttribute4.getValues().add("Ganztägig von "+healthcareServiceAvailableTimeComponent.getAvailableStartTime()+" bis "+healthcareServiceAvailableTimeComponent.getAvailableEndTime());
            }
            else {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("Am ");
                healthcareServiceAvailableTimeComponent.getDaysOfWeek().forEach(daysOfWeekEnumeration -> {
                    if (!contentBuilder.toString().equals("Am ")) {
                        contentBuilder.append(", ");
                    }
                    contentBuilder.append(daysOfWeekEnumeration.getValue().getDisplay());
                });
                fhirResourceAttribute4.getValues().add(contentBuilder.toString()+" von "+healthcareServiceAvailableTimeComponent.getAvailableStartTime()+" bis "+healthcareServiceAvailableTimeComponent.getAvailableEndTime());
            }
        });

        return fhirResource;
    }

    private FhirResource create(Organization organization, int order) {
        FhirResource fhirResource = new FhirResource();
        fhirResource.setResourceName("Organization");
        fhirResource.setName("Daten der Organization");
        fhirResource.setUid(organization.getIdPart());
        fhirResource.setOrder(order);

        FhirResourceAttribute fhirResourceAttribute1 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute1);
        fhirResourceAttribute1.setName("Name");
        fhirResourceAttribute1.getValues().add(organization.getName());

        FhirResourceAttribute fhirResourceAttribute2 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute2);
        fhirResourceAttribute2.setName("Typ");
        organization.getType().forEach(codeableConcept -> {
            codeableConcept.getCoding().forEach(coding -> {
                if (!fhirResourceAttribute2.getValues().contains(coding.getCode()+" - "+coding.getDisplay())) {
                    fhirResourceAttribute2.getValues().add(coding.getCode()+" - "+coding.getDisplay());
                }
            });
        });

        return fhirResource;
    }

    private FhirResource create(PractitionerRole practitionerRole, int order) {
        FhirResource fhirResource = new FhirResource();
        fhirResource.setResourceName("PractitionerRole");
        fhirResource.setName("Daten der PractitionerRole");
        fhirResource.setUid(practitionerRole.getIdPart());
        fhirResource.setOrder(order);

        if (!practitionerRole.getTelecom().isEmpty()) {
            FhirResourceAttribute fhirResourceAttribute1 = new FhirResourceAttribute();
            fhirResource.getAttributes().add(fhirResourceAttribute1);
            fhirResourceAttribute1.setName("Kontaktdaten");
            practitionerRole.getTelecom().forEach(contactPoint -> {
                if (contactPoint.getValue().startsWith("http")) {
                    String html = contactPoint.getSystem().getDisplay()+": "+"<a target=\"_blank\" href=\""+contactPoint.getValue()+"\">"+contactPoint.getValue()+"</a>";
                    if (!fhirResourceAttribute1.getHtmlValues().contains(html)) {
                        fhirResourceAttribute1.getHtmlValues().add(html);
                    }
                }
                else {
                    if (!fhirResourceAttribute1.getValues().contains(contactPoint.getSystem().getDisplay()+": "+contactPoint.getValue())) {
                        fhirResourceAttribute1.getValues().add(contactPoint.getSystem().getDisplay()+": "+contactPoint.getValue());
                    }
                }
            });
        }

        return fhirResource;
    }

    private FhirResource create(Practitioner practitioner, int order) {
        FhirResource fhirResource = new FhirResource();
        fhirResource.setResourceName("Practitioner");
        fhirResource.setName("Daten des Practitioner's");
        fhirResource.setUid(practitioner.getIdPart());
        fhirResource.setOrder(order);

        practitioner.getName().forEach(humanName -> {
            FhirResourceAttribute fhirResourceAttribute1 = new FhirResourceAttribute();
            fhirResource.getAttributes().add(fhirResourceAttribute1);
            fhirResourceAttribute1.setName("Name");
            humanName.getPrefix().forEach(stringType -> fhirResourceAttribute1.getValues().add(stringType.getValue()));
            humanName.getGiven().forEach(stringType -> fhirResourceAttribute1.getValues().add(stringType.getValue()));
            fhirResourceAttribute1.getValues().add(humanName.getFamily());
        });

        FhirResourceAttribute fhirResourceAttribute2 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute2);
        fhirResourceAttribute2.setName("Qualifikationen");
        practitioner.getQualification().forEach(practitionerQualificationComponent -> {
            practitionerQualificationComponent.getCode().getCoding().forEach(coding -> {
                if (!fhirResourceAttribute2.getValues().contains(coding.getCode()+": "+coding.getDisplay())) {
                    fhirResourceAttribute2.getValues().add(coding.getCode()+": "+coding.getDisplay());
                }
            });
        });

        FhirResourceAttribute fhirResourceAttribute3 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute3);
        fhirResourceAttribute3.setName("Aktiv");
        fhirResourceAttribute3.getValues().add(practitioner.getActive()?"Ja":"Nein");

        return fhirResource;
    }

    private FhirResource create(Endpoint endpoint, int order) {
        FhirResource fhirResource = new FhirResource();
        fhirResource.setResourceName("Endpoint");
        fhirResource.setName("Daten des Endpoint's");
        fhirResource.setUid(endpoint.getIdPart());
        fhirResource.setOrder(order);

        FhirResourceAttribute fhirResourceAttribute1 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute1);
        fhirResourceAttribute1.setName("Name");
        fhirResourceAttribute1.getValues().add(endpoint.getName());

        FhirResourceAttribute fhirResourceAttribute2 = new FhirResourceAttribute();
        fhirResource.getAttributes().add(fhirResourceAttribute2);
        fhirResourceAttribute2.setName("Status");
        fhirResourceAttribute2.getValues().add(endpoint.getStatus().getDisplay());

        return fhirResource;
    }

    private FhirResource create(Location location, int order) {
        FhirResource fhirResource = new FhirResource();
        fhirResource.setResourceName("Location");
        fhirResource.setName("Daten der Location");
        fhirResource.setUid(location.getIdPart());
        fhirResource.setOrder(order);

        if (location.getAddress() != null) {
            FhirResourceAttribute fhirResourceAttribute1 = new FhirResourceAttribute();
            fhirResource.getAttributes().add(fhirResourceAttribute1);
            fhirResourceAttribute1.setName("Adresse");
            location.getAddress().getLine().forEach(stringType -> fhirResourceAttribute1.getValues().add(stringType.getValue()));
            fhirResourceAttribute1.getValues().add(location.getAddress().getPostalCode());
            fhirResourceAttribute1.getValues().add(location.getAddress().getCity());
            fhirResourceAttribute1.getValues().add(location.getAddress().getState());
            fhirResourceAttribute1.getValues().add(location.getAddress().getCountry());
        }

        if (location.getPosition() != null && location.getPosition().getLongitude() != null && location.getPosition().getLatitude() != null) {
            FhirResourceAttribute fhirResourceAttribute2 = new FhirResourceAttribute();
            fhirResource.getAttributes().add(fhirResourceAttribute2);
            fhirResourceAttribute2.setName("Position");
            fhirResourceAttribute2.getValues().add("Lat: "+location.getPosition().getLatitude());
            fhirResourceAttribute2.getValues().add("Lon: "+location.getPosition().getLongitude());
            fhirResourceAttribute2.getHtmlValues().add("<a target=\"_blank\" href=\"https://www.google.com/maps?q=" + location.getPosition().getLatitude() + "," + location.getPosition().getLongitude()+"\">Link zu Google Map</a>");
        }

        return fhirResource;
    }

    public List<FhirResource> lade(Mandant mandant, EnumTriValue personalEntry, String telematikId) throws Exception {
        TiVZDProperties tiVZDProperties = mandant.createAndGetTiVZDProperties(verzeichnisdienstService);
        TiFhirProperties tiFhirProperties = tiVZDConnector.extractTiFhirProperties(tiVZDProperties);

        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient(tiFhirProperties.getResourceUri());
        client.registerInterceptor(new BearerTokenAuthInterceptor(tiFhirProperties.getHolderAccessToken().getAccessToken()));

        List<FhirResource> result = new ArrayList<>();
        if (personalEntry.equals(EnumTriValue.YES)) {
            Bundle bundle = client
                .search()
                .byUrl("PractitionerRole?practitioner.active=true&_include=PractitionerRole:practitioner&_include=PractitionerRole:location&_include=PractitionerRole:endpoint&practitioner.identifier=https://gematik.de/fhir/sid/telematik-id|"+telematikId)
                .returnBundle(Bundle.class)
                .execute();

            bundle.getEntry().forEach(e -> {
                if (e.getResource() instanceof PractitionerRole pr) {
                    result.add(create(pr, 2));
                } else if (e.getResource() instanceof Practitioner p) {
                    result.add(create(p, 1));
                } else if (e.getResource() instanceof Location loc) {
                    result.add(create(loc, 3));
                } else if (e.getResource() instanceof Endpoint endpoint) {
                    result.add(create(endpoint, 4));
                }
            });
        }
        else {
            Bundle bundle = client
                .search()
                .byUrl("HealthcareService?organization.active=true&_include=HealthcareService:organization&_include=HealthcareService:location&_include=HealthcareService:endpoint&organization.identifier=https://gematik.de/fhir/sid/telematik-id|" + telematikId)
                .returnBundle(Bundle.class)
                .execute();

            bundle.getEntry().forEach(e -> {
                if (e.getResource() instanceof HealthcareService healthcareService) {
                    result.add(create(healthcareService, 2));
                } else if (e.getResource() instanceof Organization organization) {
                    result.add(create(organization, 1));
                } else if (e.getResource() instanceof Location loc) {
                    result.add(create(loc, 3));
                } else if (e.getResource() instanceof Endpoint endpoint) {
                    result.add(create(endpoint, 4));
                }
            });
        }

        Collections.sort(result, (o1, o2) -> o1.getOrder() - o2.getOrder());
        return result;
    }
}
