package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class UserUpdateDTO {

	private String username;
	private String birthDate;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}
}
