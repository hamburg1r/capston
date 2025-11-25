package com.document.dto;
public class UserRegisterRequestDto {
    private String username;
    private String email;
    private String password;
    public UserRegisterRequestDto() {}
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
	public UserRegisterRequestDto(String username, String email, String password) {
		super();
		this.username = username;
		this.email = email;
		this.password = password;
	}
    
}
