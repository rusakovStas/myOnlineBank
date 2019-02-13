package api;

import com.stasdev.backend.model.entitys.ApplicationUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        ResponseEntity<String> all = nonAuth().restClientWithoutErrorHandler().getForEntity("/users/all", String.class);
        assertThat(all.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> create = nonAuth().restClientWithoutErrorHandler().getForEntity("/users/create", String.class);
        assertThat(create.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> delete = nonAuth().restClientWithoutErrorHandler().getForEntity("/users/delete", String.class);
        assertThat(delete.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
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

        authAdmin().restClientWithoutErrorHandler()
                .delete("/users/delete/"+userName);

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
                .delete("/users/delete/user"));
        assertThat(runtimeException.getMessage(), containsString("Access is denied"));
    }


    private AccessToRestClient createUserByAdminAndAuth(String user){
        ResponseEntity<ApplicationUser> userRs = authAdmin()
                .restClientWithErrorHandler()
                .postForEntity("/users/create", new ApplicationUser(user, "Password"), ApplicationUser.class);
        return authByUser(userRs.getBody().getUsername(), "Password");
    }

    private void createUserByUser(String createdUser){
        authUser()
                .restClientWithErrorHandler()
                .postForEntity("/users/create", new ApplicationUser(createdUser, "Password"), ApplicationUser.class);
    }

    private ResponseEntity<ApplicationUser> createUserByAdmin(String userName){
        return authAdmin()
                .restClientWithErrorHandler()
                .postForEntity("/users/create", new ApplicationUser(userName, "Password"), ApplicationUser.class);
    }

    private void checkUserExists(String userName){
        ResponseEntity<List> allUserRs = authAdmin().restClientWithoutErrorHandler()
                .getForEntity("/users/all", List.class);
        List<Map<String, String>> allUsers = allUserRs.getBody();
        assertThat(allUsers.stream().filter(m -> m.containsValue(userName)).findAny().isPresent(), is(true));
    }

    private void checkUserNotExists(String userName){
        ResponseEntity<List> allUserRs = authAdmin().restClientWithoutErrorHandler()
                .getForEntity("/users/all", List.class);
        List<Map<String, String>> allUsers = allUserRs.getBody();
        assertThat(allUsers.stream().filter(m -> m.containsValue(userName)).findAny().isPresent(), is(false));
    }

}

