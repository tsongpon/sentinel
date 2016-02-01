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
import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class ClassifiedIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassifiedIntegrationTest.class);

    private static final String FRONTIER_VERSION2 = "v2";
    private static final String PIPEK_VERSION1 = "v1";
    private static final String SEPARATOR = "/";
    private static final String FRONTIER_API_KEY = "bearer 9a76cd49-7ef3-481a-a854-f6721c9c0730";
    private static final int TIME_WAIT = 3;

    @Test
    public void testPingFrontier() throws Exception {
        get(Service.FRONTIER.pingAddress()).then().statusCode(200).contentType(ContentType.TEXT);
    }

    @Test
    public void testPingPrimus() throws Exception {
        get(Service.PRIMUS.pingAddress()).then().statusCode(200).contentType(ContentType.TEXT);
    }

    @Test
    public void testPingElasticsearch() throws Exception {
        get(Service.ELASTICSEARCH.pingAddress()).then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    public void testPingPipek() throws Exception {
        get(Service.PIPEK.pingAddress()).then().statusCode(200).contentType(ContentType.TEXT);
    }

    @Test
    public void testAllApplicationsStack() throws Exception {
        URL url = Resources.getResource("ads/newAd.json");
        String newAdASJson = Resources.toString(url, Charsets.UTF_8);
        newAdASJson = newAdASJson.replace("#ext_ref", Long.toString(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));

        Response response = given().body(newAdASJson).contentType(ContentType.JSON)
                .header("Authorization", FRONTIER_API_KEY)
                .when()
                .post(Service.FRONTIER.serviceAddress() + SEPARATOR + FRONTIER_VERSION2 + SEPARATOR + "ads");

        response.then().statusCode(201);
        String newAdLocation = response.getHeader("Location");

        Assert.assertNotNull(newAdLocation);

        LOGGER.info("Ad created on this location {}", newAdLocation);

        String adId = newAdLocation.split("/")[7];

        LOGGER.info("Wait to ad index on ES for {} minutes", TIME_WAIT);
        Thread.sleep(TimeUnit.MINUTES.toMillis(TIME_WAIT));

        LOGGER.info("Checking ad on pipek");
        get(Service.PIPEK.serviceAddress() + SEPARATOR + PIPEK_VERSION1 + SEPARATOR + "ads" + SEPARATOR + adId)
                .then().statusCode(200).contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("ads/ad_pipek_jsonschema.json"))
                .body("_source.id", equalTo(Integer.parseInt(adId)))
                .body("_source.title", equalTo("Tønsberg sentrum - Moderne enebolig i rekke med fantastisk utsikt"))
                .body("_source.media.size", equalTo(46))
                .body("_source.contacts[0].email", equalTo("Vivian.Wold@dnbeiendom.no"))
                .body("_source.attributes.cadastralnumber", equalTo(1001))
                .body("_source.attributes.propertytype", equalTo("Rekkehus"))
                .body("_source.attributes.sectionnumber", equalTo(4))
                .body("_source.attributes.price", equalTo(6000000))
                .body("_source.attributes.primaryroomarea", equalTo(155))
                .body("_source.attributes.buildyear", equalTo(2002))
                .body("_source.attributes.saletype", equalTo("Megler"))
                .body("_source.attributes.availablearea", equalTo(160))
                .body("_source.company_id", equalTo(Integer.parseInt("21960")))
                .body("_source.bookings[0].product", equalTo("prospectus"))
                .body("_source.bookings[0].publications", hasItems("www.tb.no"))
                .body("_source.bookings[0].publications", hasItems("www.sb.no"));
    }
}
