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
public class FrontierAPIVersion2IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierAPIVersion2IntegrationTest.class);

    private static final String VERSION2 = "v2";
    private static final String SEPARATOR = "/";
    private static final String FRONTIER_API_KEY = "bearer a2be9ff0-3e07-4e1a-b137-08d6f65cf9ac";

    @Test
    public void testPingFrontier() throws Exception {
        get(Service.FRONTIER.pingAddress()).then().statusCode(200).contentType(ContentType.TEXT);
    }

    @Test
    public void testVersionsEndpoint() throws Exception {
        get(Service.FRONTIER.serviceAddress() + SEPARATOR + "versions").then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    public void createAdWithNoAuthen() throws Exception {
        URL url = Resources.getResource("ads/newAd.json");
        String newAdASJson = Resources.toString(url, Charsets.UTF_8);
        newAdASJson = newAdASJson.replace("#ext_ref", Long.toString(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));

        Response response = given().body(newAdASJson).contentType(ContentType.JSON).when().
                post(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ads");

        response.then().statusCode(401);
    }

    @Test
    public void updateAdWithNoAuthen() throws Exception {
        URL url = Resources.getResource("ads/newAd.json");
        String newAdASJson = Resources.toString(url, Charsets.UTF_8);
        newAdASJson = newAdASJson.replace("#ext_ref", Long.toString(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));

        Response response = given().body(newAdASJson).contentType(ContentType.JSON).when().
                put(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ads" + SEPARATOR + "1234");

        response.then().statusCode(401);
    }

    @Test
    public void testAdEndpoint() throws Exception {
        URL url = Resources.getResource("ads/newAd.json");
        String newAdASJson = Resources.toString(url, Charsets.UTF_8);
        newAdASJson = newAdASJson.replace("#ext_ref", Long.toString(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));

        Response response = given().body(newAdASJson).contentType(ContentType.JSON)
                .header("Authorization", FRONTIER_API_KEY)
                .when()
                .post(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ads");

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
                .body("bookings[0].publications.size", equalTo(6))
                .body("bookings[0].publications", hasItems("www.tb.no"));

        String newTitle = "This is new title at " + LocalDateTime.now().toString();
        String modifiedAd = newAdASJson.replace("Tønsberg sentrum - Moderne enebolig i rekke med fantastisk utsikt", newTitle);

        Response putResponse = given().body(modifiedAd).contentType(ContentType.JSON)
                .header("Authorization", FRONTIER_API_KEY)
                .when().
                put(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ads" + SEPARATOR + adId);

        putResponse.then().statusCode(200);

        get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ads" + SEPARATOR + adId)
                .then().statusCode(200).contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("ads/ad_jsonschema.json"))
                .body("title", equalTo(newTitle))
                .body("status", equalTo(1))
                .body("category", equalTo("Eiendom/Tomannsbolig og Rekkehus"))
                .body("contacts[0].contact_fields.size", equalTo(3))
                .body("company.id", equalTo(21960))
                .body("fields.size", equalTo(34))
                .body("media.size", equalTo(46))
                .body("bookings", anything())
                .body("bookings[0].publications.size", equalTo(6))
                .body("bookings[0].publications", hasItems("www.tb.no"));

        given().param("adid", adId).when()
                .get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ads")
                .then().statusCode(200).body("total", equalTo(1));
    }

    @Test
    public void testQueryAds() throws Exception {
        given().param("status", "1").param("referencetype", "1")
                .param("clientid", "21960")
                .param("externalref", "1447778444443")
                .param("category", "Eiendom")
                .param("modifiedtimefrom", "2015-01-17T16:46:01Z")
                .param("modifiedtimeto", "2015-11-17T16:46:01Z")
                .param("modifiedtimeto", "2015-11-17T16:46:01Z")
                .param("publishperiodfrom", "2015-10-17T16:46:01Z")
                .param("publishperiodto", "2015-11-17T16:46:01Z")
                .when()
                .get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ads")
                .then().statusCode(200).contentType(ContentType.JSON);
    }
}
