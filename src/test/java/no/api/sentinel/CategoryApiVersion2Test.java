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
public class CategoryApiVersion2Test {
    private static final String VERSION2 = "v2";
    private static final String SEPARATOR = "/";

    @Test
    public void testGetCategory() throws Exception {

        get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "adcategories")
                .then().statusCode(HttpStatus.SC_OK).contentType(ContentType.JSON);

        get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "adcategories" + SEPARATOR + "11")
                .then().statusCode(HttpStatus.SC_OK).contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("category/categoryJsonSchema.json"));
    }

    @Test
    public void testNotExistCategory() throws Exception {
        get(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "adcategories" + SEPARATOR + "0")
                .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON);
    }
}
