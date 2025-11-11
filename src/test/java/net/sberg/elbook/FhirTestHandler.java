package net.sberg.elbook;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class FhirTestHandler {

    @Test
    public void execute4HealthcareService() throws Exception {
        FhirContext ctx = FhirContext.forR4();

        String serverBase = "https://fhir-directory.vzd.ti-dienste.de/search";

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);
        String accessToken = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6InNlYXJjaDpyZWFkIG93bmVyOnJlYWQgb3duZXI6d3JpdGUgbGluazpyZWFkIGxpbms6ZGVueSBjZXJ0aWZpY2F0ZTpyZWFkIGxvZzpyZWFkIiwiaXNzIjoiaHR0cHM6Ly9maGlyLWRpcmVjdG9yeS52emQudGktZGllbnN0ZS5kZS9ob2xkZXItYXV0aGVudGljYXRlIiwiYXVkIjpbImh0dHBzOi8vZmhpci1kaXJlY3RvcnkudnpkLnRpLWRpZW5zdGUuZGUvaG9sZGVyLXNlcnZpY2VzIiwiaHR0cHM6Ly9maGlyLWRpcmVjdG9yeS52emQudGktZGllbnN0ZS5kZS9vd25lciIsImh0dHBzOi8vZmhpci1kaXJlY3RvcnkudnpkLnRpLWRpZW5zdGUuZGUvc2VhcmNoIiwiaHR0cHM6Ly9maGlyLWRpcmVjdG9yeS52emQudGktZGllbnN0ZS5kZS9QZXJzb25JbnN0aXR1dGlvbkxpbmsiLCJodHRwczovL2ZoaXItZGlyZWN0b3J5LnZ6ZC50aS1kaWVuc3RlLmRlL2NlcnRpZmljYXRlcyIsImh0dHBzOi8vZmhpci1kaXJlY3RvcnkudnpkLnRpLWRpZW5zdGUuZGUvZmFjaGxpY2hlcy1sb2ciXSwiaWF0IjoxNzYyNTA1OTYwLCJleHAiOjE3NjI1OTIzNjAsInN1YiI6bnVsbCwiY2xpZW50SWQiOiJsYWtybHAiLCJob2xkZXJJZCI6Imxha3JscCJ9.A4kmk9yLOyuwBvJt_TvebKlnjR8Z9QwScEKlSiM5H_I4P2ftKLf6psdbeZOWb6lUi48yPIxLOdbvmqGKGPy3CQ"; // dein Token
        client.registerInterceptor(new BearerTokenAuthInterceptor(accessToken));

        Bundle bundle = client
            .search()
            .byUrl("HealthcareService?organization.active=true&_include=HealthcareService:organization&_include=HealthcareService:location&_include=HealthcareService:endpoint&organization.identifier=https://gematik.de/fhir/sid/telematik-id|3-11.2.0000022531.888")
            .returnBundle(Bundle.class)
            .execute();

        List<HealthcareService> healthcareServices = new ArrayList<>();
        List<Organization> organizations = new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        List<Endpoint> endpoints = new ArrayList<>();

        bundle.getEntry().forEach(e -> {
            if (e.getResource() instanceof HealthcareService hs) {
                healthcareServices.add(hs);
            } else if (e.getResource() instanceof Organization org) {
                organizations.add(org);
            } else if (e.getResource() instanceof Location loc) {
                locations.add(loc);
            } else if (e.getResource() instanceof Endpoint endpoint) {
                endpoints.add(endpoint);
            }
        });

        System.out.println(healthcareServices);

    }

    @Test
    public void execute4PractitionerRole() throws Exception {
        FhirContext ctx = FhirContext.forR4();

        String serverBase = "https://fhir-directory.vzd.ti-dienste.de/search";

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);
        String accessToken = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6InNlYXJjaDpyZWFkIG93bmVyOnJlYWQgb3duZXI6d3JpdGUgbGluazpyZWFkIGxpbms6ZGVueSBjZXJ0aWZpY2F0ZTpyZWFkIGxvZzpyZWFkIiwiaXNzIjoiaHR0cHM6Ly9maGlyLWRpcmVjdG9yeS52emQudGktZGllbnN0ZS5kZS9ob2xkZXItYXV0aGVudGljYXRlIiwiYXVkIjpbImh0dHBzOi8vZmhpci1kaXJlY3RvcnkudnpkLnRpLWRpZW5zdGUuZGUvaG9sZGVyLXNlcnZpY2VzIiwiaHR0cHM6Ly9maGlyLWRpcmVjdG9yeS52emQudGktZGllbnN0ZS5kZS9vd25lciIsImh0dHBzOi8vZmhpci1kaXJlY3RvcnkudnpkLnRpLWRpZW5zdGUuZGUvc2VhcmNoIiwiaHR0cHM6Ly9maGlyLWRpcmVjdG9yeS52emQudGktZGllbnN0ZS5kZS9QZXJzb25JbnN0aXR1dGlvbkxpbmsiLCJodHRwczovL2ZoaXItZGlyZWN0b3J5LnZ6ZC50aS1kaWVuc3RlLmRlL2NlcnRpZmljYXRlcyIsImh0dHBzOi8vZmhpci1kaXJlY3RvcnkudnpkLnRpLWRpZW5zdGUuZGUvZmFjaGxpY2hlcy1sb2ciXSwiaWF0IjoxNzYyNTA1OTYwLCJleHAiOjE3NjI1OTIzNjAsInN1YiI6bnVsbCwiY2xpZW50SWQiOiJsYWtybHAiLCJob2xkZXJJZCI6Imxha3JscCJ9.A4kmk9yLOyuwBvJt_TvebKlnjR8Z9QwScEKlSiM5H_I4P2ftKLf6psdbeZOWb6lUi48yPIxLOdbvmqGKGPy3CQ"; // dein Token
        client.registerInterceptor(new BearerTokenAuthInterceptor(accessToken));

        Bundle bundle = client
            .search()
            .byUrl("PractitionerRole?practitioner.active=true&_include=PractitionerRole:practitioner&_include=PractitionerRole:location&_include=PractitionerRole:endpoint&practitioner.identifier=https://gematik.de/fhir/sid/telematik-id|1-10900100008837")
            //.byUrl("PractitionerRole?practitioner.active=true&_include=PractitionerRole:practitioner&_include=PractitionerRole:location&_include=PractitionerRole:endpoint&_count=1")
            //.byUrl("PractitionerRole?practitioner.active=true&_include=PractitionerRole:practitioner&_include=PractitionerRole:location&_include=PractitionerRole:endpoint&location.address-city=Gelsenkirchen&location.address=45884&practitioner.qualification=1.2.276.0.76.4.241&endpoint.payload-type=tim-chat&endpoint.status=active")
            //.byUrl("PractitionerRole?practitioner.active=true&practitioner.name=Timjamin&_include=PractitionerRole:practitioner&_include=PractitionerRole:location&_include=PractitionerRole:endpoint&endpoint.status=active")
            .returnBundle(Bundle.class)
            .execute();

        List<PractitionerRole> practitionerRoles = new ArrayList<>();
        List<Practitioner> practitioners = new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        List<Endpoint> endpoints = new ArrayList<>();

        bundle.getEntry().forEach(e -> {
            if (e.getResource() instanceof PractitionerRole pr) {
                practitionerRoles.add(pr);
            } else if (e.getResource() instanceof Practitioner p) {
                practitioners.add(p);
            } else if (e.getResource() instanceof Location loc) {
                locations.add(loc);
            } else if (e.getResource() instanceof Endpoint endpoint) {
                endpoints.add(endpoint);
            }
        });

        System.out.println(practitionerRoles);
    }
}
