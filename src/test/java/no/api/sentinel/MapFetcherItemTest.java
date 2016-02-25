package no.api.sentinel;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import no.api.sentinel.enumuration.Service;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;

/**
 *
 */
public class MapFetcherItemTest {
    private static final String VERSION2 = "v2";
    private static final String SEPARATOR = "/";
    private static final String FRONTIER_API_KEY = "bearer 9a76cd49-7ef3-481a-a854-f6721c9c0730";

    @Test
    public void testGetCategory() throws Exception {

        String requestAsJson = "{\n" +
                "      \"item_id\": null,\n" +
                "      \"entry_id\": 2691071,\n" +
                "      \"priority\": 1,\n" +
                "      \"object_type\": \"company\",\n" +
                "      \"added_time\": \"2016-02-25T15:10:57Z\"\n" +
                "    }";
        Response response = given().body(requestAsJson).contentType(ContentType.JSON).when().
                header("Authorization", FRONTIER_API_KEY).
                post(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION2 + SEPARATOR + "mapfetchers");

        response.then().statusCode(HttpStatus.SC_CREATED);
    }
}
