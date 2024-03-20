package in.reqres;

import data.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

import static Specification.Specification.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.notNullValue;

/** Класс тестов API
 * @author Sharapov Yuri
 */
public class APITests {

    @AfterTest
    public void afterEach() {
        deleteSpec();
    }

    @Test
//"Проверяем что  имена файлов аватаров пользователей уникальны"
    public void filesNamesTest() {
        ExtractableResponse<Response> resp = given()
                .when()
                .get("https://reqres.in/api/users?page=2")
                .then()
                .log().all()
                .extract();
        Assert.assertEquals(resp.statusCode(), 200, "Статускод не тот");

        List<String> fileNames = resp.body().as(UserPage.class).getData().stream()
                .map(user -> {
                            String avatar = user.getAvatar();
                            return avatar.substring(avatar.lastIndexOf("/") + 1, avatar.lastIndexOf("."));
                        }
                ).toList();

        Set<String> set = new HashSet<>();
        List<String> duplicates = new ArrayList<>();
        fileNames.forEach(name -> {
            if (!set.add(name)) {
                duplicates.add(name);
            }
        });
        Assert.assertTrue(duplicates.isEmpty(), "Повторяющиеся имена файлов: " + duplicates);
    }

    @Test
    public void loginSuccessTest() {
        installSpec(requestSpec(), responseSpec(200));

        Login login = new Login();
        login.setEmail("eve.holt@reqres.in");
        login.setPassword("cityslicka");

        given()
                .body(login)
                .when()
                .post("/api/login")
                .then()
                .body("token", notNullValue());

    }

    @Test
    public void loginFailTest() {
        installSpec(requestSpec(), responseSpec(400));

        LoginFail login = new LoginFail();
        login.setEmail("eve.holt@reqres.in");

        JsonPath jsonResponse = given()
                .body(login)
                .when()
                .post("/api/login")
                .then()
                .extract().response().jsonPath();
        Assert.assertEquals(jsonResponse.get("error"), "Missing password",
                "Ошибка ошибки");
    }

    @Test
    public void resourceTest() {
        installSpec(requestSpec(), responseSpec(200));
        ExtractableResponse<Response> resp = given()
                .when()
                .get("/api/unknown")
                .then()
                .extract();

        List<Integer> sorted = resp.jsonPath().getList("data", Resource.class).stream()
                .map(Resource::getYear).collect(Collectors.toList());
        List<Integer> unsorted = List.copyOf(sorted);
        System.out.println(unsorted);
        Collections.sort(sorted);
        Assert.assertEquals(unsorted, sorted, "not sorted");
    }

    @Test
    public void tagTest() {
       String answer = given()
                .when()
                .get("https://gateway.autodns.com")
                .then()
                .log().body()
                .extract().asString();

       int count = answer.split("<[^\\/]").length - 1;
       Assert.assertEquals(count, 15, "Тегов получилось " + count);

    }
}
