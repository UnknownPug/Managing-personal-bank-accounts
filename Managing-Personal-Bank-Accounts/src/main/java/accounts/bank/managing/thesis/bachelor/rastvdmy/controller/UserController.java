package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;


import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/profile")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    public ResponseEntity<List<User>> getUsers() {
        LOG.debug("Getting all users ...");
        return ResponseEntity.ok(userService.getUsers());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long userId) {
        LOG.debug("Getting user id: {} ...", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/")
    public ResponseEntity<User> createUser(@RequestBody UserRequest user) {
        LOG.debug("Creating user: {} ...", user.name());
        return ResponseEntity.ok(userService.createUser(
                user.name(),
                user.surname(),
                user.dateOfBirth(),
                user.countryOfOrigin(),
                user.email(),
                user.password(),
                user.phoneNumber()
        ));
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping(path = "/{id}")
    public void updateUserById(@PathVariable(value = "id") Long userId, @RequestBody UserRequest user) {
        LOG.debug("Updating user id: {} ...", userId);
        userService.updateUserById(userId,
                user.email(),
                user.password(),
                user.phoneNumber());
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/avatar")
    public void uploadUserAvatar(@PathVariable(value = "id") Long userId, @RequestBody MultipartFile userAvatar) {
        LOG.debug("Uploading user avatar id: {} ...", userId);
        userService.uploadUserAvatar(userId, userAvatar);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/email")
    public void updateUserEmailById(@PathVariable(value = "id") Long userId, @RequestBody String email) {
        LOG.debug("Updating user email id: {} ...", userId);
        userService.updateUserEmailById(userId, email);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/password")
    public void updateUserPasswordById(@PathVariable(value = "id") Long userId, @RequestBody String password) {
        LOG.debug("Updating user password id: {} ...", userId);
        userService.updateUserPasswordById(userId, password);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/role")
    public void updateUserRoleById(@PathVariable(value = "id") Long userId, @RequestBody String role) {
        LOG.debug("Updating user role id: {} ...", userId);
        userService.updateUserRoleById(userId, role);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/status")
    public void updateUserStatusById(@PathVariable(value = "id") Long userId, @RequestBody String status) {
        LOG.debug("Updating user state id: {} ...", userId);
        userService.updateUserStateById(userId, status);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/phoneNumber")
    public void updateUserPhoneNumberById(@PathVariable(value = "id") Long userId, @RequestBody String phoneNumber) {
        LOG.debug("Updating user phone number id: {} ...", userId);
        userService.updateUserPhoneNumberById(userId, phoneNumber);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public void deleteUserById(@PathVariable(value = "id") Long userId) {
        LOG.debug("Deleting user by id: {} ...", userId);
        userService.deleteUserById(userId);
    }
}
