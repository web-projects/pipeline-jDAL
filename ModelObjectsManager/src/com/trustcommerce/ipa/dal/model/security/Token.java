package com.trustcommerce.ipa.dal.model.security;



/**
 * 
 * 	{"access_token":"KVJT945K44IWcfkdnqwiz-uAOymZ4fvxP2V_TLOZ2qPAwD3DujQmXrVvvRyB9ZsNF_bYV1YL9_7DzLjfMnwQsM2uma5pe5JxeK7IY1TIzXUOmpkIynaTvUS33HYtRAev0lXOJVPBnJhfL8aUQ2HkiAxSWsSrdyAuXqWu6ataIEHzqfOBytFk7RxNcFjbpR5Uhse6sYSGxFDnwmoFyq4Ty5vZFORos3V4-VFWQ8dKQ5AlZz2LgDGGjeDfSp15xfIvy4d8IoJq118uSsajFLddnRcUVn61SBlLGFu159eSG1fxzfEE7j9U7GAIop4Q-q3kt0PJPMaI_wPxEAVrg7CSN8rR0398heVbL7yCshCvZKFBaQN9fD-Bhj2TuuOzIIYo8h-AngLN8mqJRpVaSmMM_ZZQ0t8xSN7N9UlRdj78tZZi1txcYSH6pChVbXDVt0jHR_oEJFQL4y_Rs43r4e8ddevjE5Q","token_type":"bearer","expires_in":1799,"userName":"ralph56",".issued":"Thu, 22 Oct 2015 22:50:37 GMT",".expires":"Thu, 22 Oct 2015 23:20:37 GMT"}
 * 
 * @author luisa.lamore
 *
 */
public class Token {

	private String access_token;
	
	private String token_type;
	
	private String expires_in;
	
	private String userName;
	

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public String getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(String expires_in) {
		this.expires_in = expires_in;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	
    public String toString() {
	    return "{" 
	    		+ "\"access_token\" : \"" + access_token + "\","
	    		+ "\"token_type\" : \"" + token_type + "\","
	    		+ "\"expires_in\" : \"" + expires_in + "\","
	    		+ "\"userName\" : \"" + userName + "\""
	    		+ "}";
    }

    
}
