package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
		user.setPassword("123");

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("123");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.password", is(user.getPassword())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

	@Test
	public void createUser_duplicateUsername_conflictStatus() throws Exception {
		// given
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("existingUsername");
		userPostDTO.setPassword("123");

		// Simulate the behavior of the service method throwing a ResponseStatusException
		given(userService.createUser(Mockito.any()))
				.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isConflict());
	}

	@Test
	public void getUser_existingUser_shouldReturnUser() throws Exception {
		// given
		Long userId = 1L;
		User existingUser = new User();
		existingUser.setId(userId);
		existingUser.setName("Test User");
		existingUser.setUsername("testUsername");
		existingUser.setPassword("123");

		given(userService.getUserById(userId)).willReturn(existingUser);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder getRequest = get("/users/{id}", userId)
				.contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(existingUser.getId().intValue())))
				.andExpect(jsonPath("$.name", is(existingUser.getName())))
				.andExpect(jsonPath("$.password", is(existingUser.getPassword())))
				.andExpect(jsonPath("$.username", is(existingUser.getUsername())));
	}

	@Test
	public void getUser_nonexistentUser_shouldReturnNotFound() throws Exception {
		// given
		Long userId = 2L;

		// Simulate the behavior of the service method throwing a ResponseStatusException
		given(userService.getUserById(userId))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder getRequest = get("/users/{id}", userId)
				.contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest)
				.andExpect(status().isNotFound());
	}

	@Test
	public void updateUser_existingUser_shouldReturnAccepted() throws Exception {
		// given
		Long userId = 1L;
		User existingUser = new User();
		existingUser.setId(userId);
		existingUser.setName("Test User");
		existingUser.setUsername("testUsername");
		existingUser.setPassword("123");

		UserPostDTO updatedUserData = new UserPostDTO();
		updatedUserData.setUsername("newUsername");

		given(userService.getUserById(userId)).willReturn(existingUser);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder putRequest = put("/users/{id}", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(updatedUserData));

		// then
		mockMvc.perform(putRequest)
				.andExpect(status().isAccepted());
	}

	@Test
	public void updateUser_nonexistentUser_shouldReturnNotFound() throws Exception {
		// given
		Long userId = 2L;
		UserPostDTO updatedUserData = new UserPostDTO();
		updatedUserData.setUsername("newUsername");

		// Simulate the behavior of the service method throwing a ResponseStatusException
		given(userService.getUserById(userId))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder putRequest = put("/users/{id}", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(updatedUserData));

		// then
		mockMvc.perform(putRequest)
				.andExpect(status().isNotFound());
	}


  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}