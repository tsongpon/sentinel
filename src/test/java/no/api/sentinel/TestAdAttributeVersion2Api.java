package no.api.sentinel;

import com.jayway.restassured.http.ContentType;
import no.api.sentinel.enumuration.Service;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 *
 */
public class TestAdAttributeVersion2Api {
    private static final String VERSION2 = "v2";
    private static final String SEPARATOR = "/";

    @Test
    public void testGetAttributeElement() throws Exception {

        get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ad_attribute_elements")
                .then().statusCode(HttpStatus.SC_OK).contentType(ContentType.JSON);

        get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ad_attribute_elements" + SEPARATOR + "propertytype")
                .then().statusCode(HttpStatus.SC_OK).contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("adAttribute/adArreibuteJsonSchema.json"));
    }

    @Test
    public void testNotExistAttribute() throws Exception {
        get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "ad_attribute_elements" + SEPARATOR + "notexist")
                .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON);
    }
}
