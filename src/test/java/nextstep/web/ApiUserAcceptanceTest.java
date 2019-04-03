package nextstep.web;

import nextstep.domain.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import support.test.AcceptanceTest;

import static nextstep.domain.UserTest.newUser;

public class ApiUserAcceptanceTest extends AcceptanceTest {
    private static final Logger log = LoggerFactory.getLogger(ApiUserAcceptanceTest.class);

    @Test
    public void create() throws Exception {
        // given
        User newUser = newUser("testuser1");
        String resourceLocation = createUserResource(newUser);

        // when
        ResponseEntity<User> response = getUserResource(newUser, resourceLocation);

        // then
        User dbUser = response.getBody();
        softly.assertThat(dbUser).isNotNull();
    }

    @Test
    public void show_다른_사람() throws Exception {
        // given
        User newUser = newUser("testuser2");
        String resourceLocation = createUserResource(newUser);
        User otherUser = defaultUser();

        // when
        ResponseEntity<User> response = getUserResource(otherUser, resourceLocation);

        // then
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void update() throws Exception {
        // given
        User newUser = newUser("testuser3");
        String resourceLocation = createUserResource(newUser);

        User original = getUserResource(newUser, resourceLocation).getBody();
        User updateUser = new User
                (original.getId(), original.getUserId(), original.getPassword(),
                        "javajigi2", "javajigi2@slipp.net");

        // when
        ResponseEntity<User> response = updateUserResource(newUser, resourceLocation, updateUser);

        // then
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(updateUser.equalsNameAndEmail(response.getBody())).isTrue();
    }

    @Test
    public void update_no_login() throws Exception {
        // given
        User newUser = newUser("testuser4");
        String resourceLocation = createUserResource(newUser);

        User original = getUserResource(newUser, resourceLocation).getBody();
        User updateUser = new User
                (original.getId(), original.getUserId(), original.getPassword(),
                        "javajigi2", "javajigi2@slipp.net");

        // when
        ResponseEntity<String> responseEntity = updateResourceWithoutLogin(resourceLocation, updateUser, String.class);

        // then
        softly.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        log.debug("error message : {}", responseEntity.getBody());
    }

    @Test
    public void update_다른_사람() throws Exception {
        // given
        User newUser = newUser("testuser5");
        String resourceLocation = createUserResource(newUser);
        User otherUser = defaultUser();

        User original = getUserResource(newUser, resourceLocation).getBody();
        User updateUser = new User
                (original.getId(), original.getUserId(), original.getPassword(),
                        "javajigi2", "javajigi2@slipp.net");

        // when
        ResponseEntity<User> responseEntity = updateUserResource(otherUser, resourceLocation, updateUser);

        // then
        softly.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String createUserResource(User resource) {
        return createResource("/api/users", resource);
    }

    private ResponseEntity<User> getUserResource(User loginUser, String location) {
        return getResource(loginUser, location, User.class);
    }

    protected ResponseEntity<User> updateUserResource(User loginUser, String location, User updatedUser) {
        return updateResource(loginUser, location, updatedUser
                , User.class);
    }
}
