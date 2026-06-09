package com.tutor.gurukul.users.internal;

import com.tutor.gurukul.TestcontainersConfiguration;
import com.tutor.gurukul.users.UserService;
import com.tutor.gurukul.users.exception.UserAlreadyExist;
import com.tutor.gurukul.users.exception.UserNotFoundException;
import com.tutor.gurukul.users.model.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
public class UserServiceIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepo userRepo;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
    }

    @Test
    void createUser_persistsEntity() {
        var req = UserRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@doe.com")
                .password("pass")
                .phoneNumber("12345")
                .role("STUDENT")
                .street("street")
                .city("city")
                .state("state")
                .zipCode("zip")
                .country("country")
                .companyId("comp1")
                .build();

        userService.createUser(req);

        var all = userRepo.findAll();
        assertEquals(1, all.size());
        var saved = all.getFirst();
        assertEquals("Jane", saved.getFirstName());
        assertEquals("jane@doe.com", saved.getEmail());
    }

    @Test
    void createUser_duplicate_throws() {
        var req = UserRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@doe.com")
                .password("pass")
                .role("STUDENT")
                .companyId("comp1")
                .build();

        userService.createUser(req);
        assertThrows(UserAlreadyExist.class, () -> userService.createUser(req));
    }

    @Test
    void getUserById_returnsResponse() throws UserNotFoundException {
        var toSave = Users.builder()
                .firstName("X")
                .lastName("Y")
                .email("x@y.com")
                .password("p")
                .phoneNumber("111")
                .companyId("comp1")
                .role(Role.STUDENT)
                .address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build())
                .build();
        var saved = userRepo.save(toSave);

        var resp = userService.getUserById(saved.getUserId());

        assertEquals(saved.getUserId(), resp.userId());
        assertEquals("x@y.com", resp.email());
    }

    @Test
    void updateUser_updatesFields() throws UserNotFoundException {
        var saved = userRepo.save(Users.builder()
                .firstName("Old")
                .lastName("Name")
                .email("old@old.com")
                .password("p")
                .companyId("compX")
                .role(Role.STUDENT)
                .address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build())
                .build());

        var req = UserRequest.builder()
                .firstName("New")
                .lastName("Name")
                .email("new@new.com")
                .password("p")
                .phoneNumber("222")
                .role("STUDENT")
                .companyId("compX")
                .street("s2")
                .city("c2")
                .state("st2")
                .zipCode("z2")
                .country("co2")
                .build();

        userService.updateUser(saved.getUserId(), req);

        var updated = userRepo.findById(saved.getUserId()).orElseThrow();
        assertEquals("New", updated.getFirstName());
        assertEquals("new@new.com", updated.getEmail());
    }

    @Test
    void deleteUser_removesEntity() throws UserNotFoundException {
        var saved = userRepo.save(Users.builder()
                .firstName("ToDel")
                .lastName("D")
                .email("del@d.com")
                .password("p")
                .companyId("compD")
                .role(Role.STUDENT)
                .address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build())
                .build());

        userService.deleteUser(saved.getUserId());
        assertFalse(userRepo.findById(saved.getUserId()).isPresent());
    }

    @Test
    void deleteUser_nonexistent_throws() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("no-such-id"));
    }

    @Test
    void getAllUsers_returnsResponses() {
        userRepo.save(Users.builder().firstName("A").lastName("A").email("a@a").password("p").companyId("c").role(Role.STUDENT).address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build()).build());
        userRepo.save(Users.builder().firstName("B").lastName("B").email("b@b").password("p").companyId("c").role(Role.TUTOR).address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build()).build());

        var res = userService.getAllUsers();
        assertEquals(2, res.size());
    }

    @Test
    void getUserByEmail_returnsResponse() throws UserNotFoundException {
        var saved = userRepo.save(Users.builder().firstName("E").lastName("E").email("e@e").password("p").companyId("c").role(Role.STUDENT).address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build()).build());
        var resp = userService.getUserByEmail("e@e");
        assertEquals(saved.getUserId(), resp.userId());
        assertEquals("e@e", resp.email());
    }

    @Test
    void getUsersByCompanyId_success() throws UserNotFoundException {
        userRepo.save(Users.builder().firstName("C1").lastName("L").email("c1@x").password("p").companyId("companyA").role(Role.STUDENT).address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build()).build());
        userRepo.save(Users.builder().firstName("C2").lastName("L").email("c2@x").password("p").companyId("companyA").role(Role.TUTOR).address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build()).build());

        var res = userService.getUsersByCompanyId("companyA");
        assertEquals(2, res.size());
        assertEquals("companyA", res.get(0).companyId());
    }

    @Test
    void deleteUsersByCompanyId_success() throws UserNotFoundException {
        var u1 = userRepo.save(Users.builder().firstName("D1").lastName("L").email("d1@x").password("p").companyId("co1").role(Role.STUDENT).address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build()).build());
        var u2 = userRepo.save(Users.builder().firstName("D2").lastName("L").email("d2@x").password("p").companyId("co1").role(Role.TUTOR).address(Address.builder().street("s").city("c").state("st").zipCode("z").country("co").build()).build());
        userService.deleteUsersByCompanyId("co1");

        assertTrue(userRepo.findByCompanyId("co1").isPresent() && userRepo.findByCompanyId("co1").get().isEmpty());
    }

    @Test
    void deleteUsersByCompanyId_notFound_throws() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUsersByCompanyId("nope"));
    }

}
