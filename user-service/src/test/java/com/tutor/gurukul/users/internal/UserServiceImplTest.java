package com.tutor.gurukul.users.internal;

import com.tutor.gurukul.users.model.UserRequest;
import com.tutor.gurukul.users.exception.UserAlreadyExist;
import com.tutor.gurukul.users.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepo userRepo;
    @InjectMocks
    UserServiceImpl userService;

    private Users sampleUser(String id, String email, String companyId) {
        return Users.builder()
                .userId(id)
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("pass")
                .phoneNumber("1234567890")
                .companyId(companyId)
                .role(Role.STUDENT)
                .address(Address.builder().street("street").city("city").state("state").zipCode("zip").country("country").build())
                .build();
    }

    private UserRequest sampleRequest(String email, String companyId) {
        return UserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email(email)
                .password("pass")
                .phoneNumber("0987654321")
                .role("STUDENT")
                .street("street")
                .city("city")
                .state("state")
                .zipCode("zip")
                .country("country")
                .companyId(companyId)
                .build();
    }

    @Test
    void createUser_success() {
        var req = sampleRequest("a@b.com", "comp1");
        when(userRepo.findByEmail(req.email())).thenReturn(Optional.empty());
        userService.createUser(req);
        verify(userRepo).save(argThat(u ->
                u.getEmail().equals(req.email())
                        && u.getFirstName().equals(req.firstName())
                        && u.getCompanyId().equals(req.companyId())
        ));
    }

    @Test
    void createUser_alreadyExists_throws() {
        var req = sampleRequest("a@b.com", "comp1");
        var existing = sampleUser("id1", req.email(), req.companyId());
        when(userRepo.findByEmail(req.email())).thenReturn(Optional.of(existing));
        assertThrows(UserAlreadyExist.class, () -> userService.createUser(req));
    }

    @Test
    void updateUser_success() throws UserNotFoundException {
        var id = "u1";
        var existing = sampleUser(id, "old@x.com", "comp1");
        when(userRepo.findById(id)).thenReturn(Optional.of(existing));
        var req = sampleRequest("new@x.com", "comp1");
        userService.updateUser(id, req);
        verify(userRepo).save(argThat(u ->
                u.getUserId().equals(id)
                        && u.getFirstName().equals(req.firstName())
                        && u.getEmail().equals(req.email())
        ));
    }

    @Test
    void updateUser_notFound_throws() {
        when(userRepo.findById("missing")).thenReturn(Optional.empty());
        var req = sampleRequest("no@x.com", "comp1");
        assertThrows(UserNotFoundException.class, () -> userService.updateUser("missing", req));
    }

    @Test
    void deleteUser_success() throws UserNotFoundException {
        var id = "u2";
        var existing = sampleUser(id, "del@x.com", "comp1");
        when(userRepo.findById(id)).thenReturn(Optional.of(existing));
        userService.deleteUser(id);
        verify(userRepo).delete(existing);
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepo.findById("missing")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("missing"));
    }

    @Test
    void getAllUsers_nonEmpty_returnsMappedResponses() {
        var u1 = sampleUser("id1", "one@x.com","comp1");
        var u2 = sampleUser("id2", "two@x.com","comp1");
        when(userRepo.findAll()).thenReturn(List.of(u1,u2));
        var res = userService.getAllUsers();
        assertEquals(2, res.size());
        assertEquals(u1.getEmail(), res.get(0).email());
        assertEquals(u2.getEmail(), res.get(1).email());
    }

    @Test
    void getAllUsers_empty_returnsEmptyList() {
        when(userRepo.findAll()).thenReturn(List.of());
        var res = userService.getAllUsers();
        assertTrue(res.isEmpty());
    }

    @Test
    void getUserById_success() throws UserNotFoundException {
        var u = sampleUser("idx", "byid@x.com","comp1");
        when(userRepo.findById("idx")).thenReturn(Optional.of(u));
        var resp = userService.getUserById("idx");
        assertEquals(u.getUserId(), resp.userId());
        assertEquals(u.getEmail(), resp.email());
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepo.findById("missing")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById("missing"));
    }

    @Test
    void getUserByEmail_success() throws UserNotFoundException {
        var u = sampleUser("id3", "byemail@x.com","comp1");
        when(userRepo.findByEmail("byemail@x.com")).thenReturn(Optional.of(u));
        var resp = userService.getUserByEmail("byemail@x.com");
        assertEquals(u.getUserId(), resp.userId());
        assertEquals(u.getEmail(), resp.email());
    }

    @Test
    void getUserByEmail_notFound_throws() {
        when(userRepo.findByEmail("missing@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("missing@x.com"));
    }

    @Test
    void getUsersByCompanyId_success() throws UserNotFoundException {
        var u1 = sampleUser("c1", "c1@x.com","companyA");
        var u2 = sampleUser("c2", "c2@x.com","companyA");
        when(userRepo.findByCompanyId("companyA")).thenReturn(Optional.of(List.of(u1,u2)));
        var res = userService.getUsersByCompanyId("companyA");
        assertEquals(2, res.size());
        assertEquals("companyA", res.getFirst().companyId());
    }

    @Test
    void getUsersByCompanyId_notFound_throws() {
        when(userRepo.findByCompanyId("nope")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUsersByCompanyId("nope"));
    }

    @Test
    void deleteUsersByCompanyId_success() throws UserNotFoundException {
        var u1 = sampleUser("d1", "d1@x.com","co1");
        var u2 = sampleUser("d2", "d2@x.com","co1");
        var list = List.of(u1,u2);
        when(userRepo.findByCompanyId("co1")).thenReturn(Optional.of(list));
        userService.deleteUsersByCompanyId("co1");
        verify(userRepo).deleteAll(list);
    }

    @Test
    void deleteUsersByCompanyId_notFound_throws() {
        when(userRepo.findByCompanyId("nope")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUsersByCompanyId("nope"));
    }
}
