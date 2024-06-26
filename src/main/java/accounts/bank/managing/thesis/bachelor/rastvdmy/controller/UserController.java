package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * This class is responsible for handling user related requests.
 * It provides endpoints for getting all users, filtering users, getting a user by id,
 * creating a user, updating a user, uploading a user avatar,
 * updating a user's email, password, role, status, visibility, phone number,
 * and deleting a user.
 */
@Slf4j
@RestController
@RequestMapping(path = "/profile")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * Constructor for the UserController.
     *
     * @param userService The service to handle user operations.
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * This method is used to get all users.
     *
     * @return A list of all users.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<List<User>> getUsers() {
        LOG.info("Getting all users ...");
        return ResponseEntity.ok(userService.getUsers());
    }

    /**
     * This method is used to filter users.
     *
     * @param page The page number.
     * @param size The size of the page.
     * @param sort The sort order.
     * @return A page of filtered users.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<User>> filterUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.info("Filtering users ...");
        switch (sort.toLowerCase()) {
            case "asc":
                LOG.info("Sorting users by name in ascending order ...");
                Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
                return ResponseEntity.ok(userService.filterAndSortUsers(pageable));
            case "desc":
                LOG.info("Sorting users by name in descending order ...");
                Pageable pageable1 = PageRequest.of(page, size, Sort.by("name").descending());
                return ResponseEntity.ok(userService.filterAndSortUsers(pageable1));
            default:
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    /**
     * This method is used to get a user by id.
     *
     * @param userId The id of the user.
     * @return The user with the given id.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long userId) {
        LOG.info("Getting user id: {} ...", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * This method is used to create a user.
     *
     * @param user The request containing the user's name, surname, date of birth, country of origin, email, password, and phone number.
     * @return The created user.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/register")
    public ResponseEntity<User> createUser(@RequestBody UserRequest user) {
        LOG.info("Creating user: {} ...", user.name());
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

    /**
     * This method is used to update a user by id.
     *
     * @param userId The id of the user.
     * @param user   The request containing the user's email, password, and phone number.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void updateUserById(@PathVariable(value = "id") Long userId, @RequestBody UserRequest user) {
        LOG.info("Updating user id: {} ...", userId);
        userService.updateUserById(userId,
                user.email(),
                user.password(),
                user.phoneNumber());
    }

    /**
     * This method is used to upload a user avatar.
     *
     * @param userId     The id of the user.
     * @param userAvatar The user's avatar file.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/avatar")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void uploadUserAvatar(@PathVariable(value = "id") Long userId, @RequestBody MultipartFile userAvatar) {
        LOG.info("Uploading user avatar id: {} ...", userId);
        userService.uploadUserAvatar(userId, userAvatar);
    }

    /**
     * This method is used to update a user's email by id.
     *
     * @param userId  The id of the user.
     * @param request The request containing the user's email.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/email")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void updateUserEmailById(@PathVariable(value = "id") Long userId, @RequestBody UserRequest request) {
        LOG.info("Updating user email id: {} ...", userId);
        userService.updateUserEmailById(userId, request.email());
    }

    /**
     * This method is used to update a user's password by id.
     *
     * @param userId  The id of the user.
     * @param request The request containing the user's password.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/password")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void updateUserPasswordById(@PathVariable(value = "id") Long userId, @RequestBody UserRequest request) {
        LOG.info("Updating user password id: {} ...", userId);
        userService.updateUserPasswordById(userId, request.password());
    }

    /**
     * This method is used to update a user's role by id.
     *
     * @param userId  The id of the user.
     * @param request The request containing the user's role.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateUserRoleById(@PathVariable(value = "id") Long userId, @RequestBody UserRequest request) {
        LOG.info("Updating user role id: {} ...", userId);
        userService.updateUserRoleById(userId, request.userRole());
    }

    /**
     * This method is used to update a user's status by id.
     *
     * @param userId The id of the user.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    public void updateUserStatusById(@PathVariable(value = "id") Long userId) {
        LOG.info("Updating user state id: {} ...", userId);
        userService.updateUserStatusById(userId);
    }

    /**
     * This method is used to update a user's visibility by id.
     *
     * @param userId The id of the user.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/visibility")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN', 'ROLE_USER')")
    public void updateUserVisibilityById(@PathVariable(value = "id") Long userId) {
        LOG.info("Updating user visibility id: {} ...", userId);
        userService.updateUserVisibilityById(userId);
    }

    /**
     * This method is used to update a user's phone number by id.
     *
     * @param userId  The id of the user.
     * @param request The request containing the user's phone number.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/phone-number")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void updateUserPhoneNumberById(@PathVariable(value = "id") Long userId, @RequestBody UserRequest request) {
        LOG.info("Updating user phone number id: {} ...", userId);
        userService.updateUserPhoneNumberById(userId, request.phoneNumber());
    }

    /**
     * This method is used to delete a user by id.
     *
     * @param userId The id of the user.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteUserById(@PathVariable(value = "id") Long userId) {
        LOG.info("Deleting user by id: {} ...", userId);
        userService.deleteUserById(userId);
    }
}
