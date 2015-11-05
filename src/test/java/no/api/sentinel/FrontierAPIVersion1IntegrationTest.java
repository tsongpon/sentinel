package no.api.sentinel;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import no.api.sentinel.enumuration.Service;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class FrontierAPIVersion1IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierAPIVersion1IntegrationTest.class);

    private static final String VERSION1 = "v1";
    private static final String SEPERATOR = "/";

    @Test
    public void testPingFrontier() throws Exception {
        get(Service.FRONTIER.pingAddress()).then().statusCode(200).contentType(ContentType.TEXT);
    }

    @Test
    public void testVersionsEndpoint() throws Exception {
        get(Service.FRONTIER.serviceAddress() + SEPERATOR + "versions").then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    public void testCreateNewAd() throws Exception {
        URL url = Resources.getResource("ads/newAd.json");
        String newAdASJson = Resources.toString(url, Charsets.UTF_8);
        newAdASJson = newAdASJson.replace("#ext_ref", Long.toString(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));

        Response response = given().body(newAdASJson).contentType(ContentType.JSON).when().
                post(Service.FRONTIER.serviceAddress() + SEPERATOR + VERSION1 + SEPERATOR + "ads");

        response.then().statusCode(201);
        String newAdLocation = response.getHeader("Location");

        Assert.assertNotNull(newAdLocation);

        LOGGER.info("Ad created on this location {}", newAdLocation);

        String adId = newAdLocation.split("/")[7];

        LOGGER.info("Validating response from API");
        get(newAdLocation).then().statusCode(200).contentType(ContentType.JSON).
                assertThat().contentType(ContentType.JSON)
                .body("id", equalTo(Integer.parseInt(adId)))
                .body(matchesJsonSchemaInClasspath("ads/ad_jsonschema.json"))
                .body("title", equalTo("Tønsberg sentrum - Moderne enebolig i rekke med fantastisk utsikt"))
                .body("status", equalTo(1))
                .body("category", equalTo("Eiendom/Tomannsbolig og Rekkehus"))
                .body("contacts[0].contact_fields.size", equalTo(3))
                .body("company.id", equalTo(21960))
                .body("fields.size", equalTo(34))
                .body("media.size", equalTo(46))
                .body("bookings", anything())
                .body("bookings[0].publications.size", equalTo(61))
                .body("bookings[0].publications", hasItems("www.tb.no"));
    }
}
