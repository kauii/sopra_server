package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static ch.uzh.ifi.hase.soprafs24.constant.UserStatus.OFFLINE;
import static ch.uzh.ifi.hase.soprafs24.constant.UserStatus.ONLINE;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<UserGetDTO> loginUser(@RequestBody UserPostDTO loginDTO) {
        // validate login credentials
        User loggedInUser = userService.loginUser(loginDTO.getUsername(), loginDTO.getPassword());

        if (loggedInUser != null) {
						// Update the user status to online
						userService.updateStatus(loggedInUser, ONLINE);
            // convert internal representation of user to API representation
            UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
            return ResponseEntity.ok(userGetDTO);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

	@PostMapping("users/{id}/logout")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<String> logoutUser(@PathVariable Long id) {
		try {
			// Retrieve the user from the database
			User user = userService.getUserById(id);

			if (user != null) {
				// Update the user status to offline
				userService.updateStatus(user, OFFLINE);

				return ResponseEntity.ok("User logged out successfully.");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during logout.");
		}
	}

    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
		public ResponseEntity<UserGetDTO> getUser(@PathVariable Long id) {
			User user = userService.getUserById(id);
			UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
			return ResponseEntity.ok(userGetDTO);
		}

    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserPostDTO postDTO) {
        try {
            // Check if the user with the provided ID exists
            User existingUser = userService.getUserById(id);

            System.out.println("Token: " + existingUser.getToken());
            System.out.println(postDTO.getBirthDate());
            userService.updateUser(existingUser, postDTO.getUsername(), postDTO.getBirthDate());

            // Convert the updated user to API representation
            DTOMapper.INSTANCE.convertEntityToUserGetDTO(existingUser);

            return ResponseEntity.accepted().body("Changes saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

}

