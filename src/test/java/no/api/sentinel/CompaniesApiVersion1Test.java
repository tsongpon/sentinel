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

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

/**
 *
 */
public class CompaniesApiVersion1Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompaniesApiVersion1Test.class);

    private static final String VERSION1 = "v1";
    private static final String SEPARATOR = "/";

    @Test
    public void testCompanyAPI() throws Exception {
        URL url = Resources.getResource("company/company.json");
        String newCompanyAsJson = Resources.toString(url, Charsets.UTF_8);

        Response response = given().body(newCompanyAsJson).contentType(ContentType.JSON).when().
                post(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION1 + SEPARATOR + "companies");

        response.then().statusCode(201);
        String newCompanyLocation = response.getHeader("Location");
        Assert.assertNotNull(newCompanyLocation);

        String newCompanyId = newCompanyLocation.split("/")[7];
        get(newCompanyLocation).then().statusCode(200).contentType(ContentType.JSON)
                .assertThat().contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("company/company_jsonschema.json"))
                .body("title", equalTo("Dnb NOR Eiendom Asker"))
                .body("fields.size", equalTo(8));

        Response responseFromGet = get(newCompanyLocation);
        String newAdAsJson = responseFromGet.asString();

        // test update ad
        String modifiedAd = newAdAsJson.replace("Dnb NOR Eiendom Asker", "new title");

        Response putResponse = given().body(modifiedAd).contentType(ContentType.JSON).when().
                put(Service.FRONTIER.serviceAddress() + SEPARATOR + VERSION1 + SEPARATOR + "companies" + SEPARATOR + newCompanyId);
        putResponse.then().statusCode(200);

        //another get to verify put request
        get(newCompanyLocation).then().statusCode(200).contentType(ContentType.JSON)
                .assertThat().contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("company/company_jsonschema.json"))
                .body("title", equalTo("new title"))
                .body("fields.size", equalTo(8));
    }
}
