package com.document.dto;

public class UserLoginRequestDto {
	
	    private String email;
	    private String password;
	    public UserLoginRequestDto() {}
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
		public UserLoginRequestDto(String email, String password) {
			super();
			this.email = email;
			this.password = password;
		}
	    
	    

}
