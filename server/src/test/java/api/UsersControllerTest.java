package api;

import com.stasdev.backend.model.entitys.ApplicationUser;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*
* Тесты для Users API
* проверяются защищеннсоть всех ендпоинтов
* проверяется функционал админа и функционал обычного юзера
* */
class UsersControllerTest extends CommonApiTest{

    @Test
    void allEndpointsSecured() {
        ResponseEntity<String> allUsers = nonAuth().restClientWithoutErrorHandler().getForEntity("/users/all", String.class);
        assertThat(allUsers.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> createUser = nonAuth().restClientWithoutErrorHandler().postForEntity("/users",null ,String.class);
        assertThat(createUser.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> deleteUser = nonAuth().restClientWithoutErrorHandler().exchange("/users?username=user", HttpMethod.DELETE, null, String.class);
        assertThat(deleteUser.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }


    @Test
    void adminCanNotCreateUserWithSameUsernameMoreThenOneTime() {
        String userName = "UserForTestRestriction";
        ResponseEntity<ApplicationUser> userRs = createUserByAdmin(userName);
        assertThat(userRs.getStatusCode(), equalTo(HttpStatus.OK));

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> createUserByAdmin(userName));
        assertThat(runtimeException.getMessage(), containsString("User with name '"+userName+"' already exists!"));
    }

    @Test
    void adminCanSeeAllUsers() {
        ResponseEntity<List> forEntity = authAdmin()
                .restClientWithoutErrorHandler()
                .getForEntity("/users/all", List.class);

        assertThat(forEntity.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    void userCanNotCreateUser() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> createUserByUser( "UserForTestRestriction"));
        assertThat(runtimeException.getMessage(), containsString("Access is denied"));
    }

    @Test
    void userCanNotSeeAnotherUser() {
        ResponseEntity<String> all = authUser()
                .restClientWithoutErrorHandler()
                .getForEntity("/users/all", String.class);

        assertThat(all.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void adminCanDeleteUser() {
        String userName = "UserForDelete";
        createUserByAdmin(userName);
        checkUserExists(userName);

        authAdmin().restClientWithErrorHandler()
                .delete("/users?username="+userName);

        checkUserNotExists(userName);
    }

    @Test
    void adminCanCreateUser() {
        String userName = "UserForCheckCreate";
        ResponseEntity<ApplicationUser> user = createUserByAdmin(userName);

        ApplicationUser createdUser = user.getBody();
        assert createdUser != null;
        assertThat(createdUser.getUsername(), equalTo(userName));
        assertThat(createdUser.getPassword(), notNullValue());//пароль не проверяем потому что зашифровано
        assertThat(createdUser.getUser_id(), notNullValue());
        assertThat(createdUser.getRoles(), hasSize(1));
        assertThat(createdUser.getRoles(), hasItem(hasProperty("role", equalTo("user"))));
    }

    @Test
    void userCanNotDeleteUser() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser()
                .restClientWithErrorHandler()
                .delete("/users?username=user"));
        assertThat(runtimeException.getMessage(), containsString("Access is denied"));
    }

}

