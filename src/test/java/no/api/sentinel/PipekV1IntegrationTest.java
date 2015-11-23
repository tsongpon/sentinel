package no.api.sentinel;

import com.jayway.restassured.http.ContentType;
import no.api.sentinel.enumuration.Service;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jayway.restassured.RestAssured.get;

/**
 *
 */

public class PipekV1IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipekV1IntegrationTest.class);

    private static final String VERSION1 = "v1";
    private static final String SEPARATOR = "/";

    @Test
    public void testPingElasticsearch() throws Exception {
        get(Service.ELASTICSEARCH.pingAddress()).then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    public void testPingPipek() throws Exception {
        get(Service.PIPEK.pingAddress()).then().statusCode(200).contentType(ContentType.TEXT);
    }

    @Test
    public void testSearchEndpoint() throws Exception {
        get(Service.PIPEK.serviceAddress() + SEPARATOR + VERSION1 + SEPARATOR + "ads/search").then().statusCode(200).contentType(ContentType.JSON);
    }

}
