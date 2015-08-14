package com.greglturnquist.springagram.fileservice.s3;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.ToString;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@ToString(exclude = "password")
public class User implements Serializable {

	public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

	@Id @GeneratedValue
	private long id;

	private String name;

	// tag::user[]
	// This field MUST be protected against any form of
	// serialization to avoid security leakage
	@JsonIgnore
	private String password;
	//end::user[]

	private String[] roles;

	public void setPassword(String password) {
		this.password = PASSWORD_ENCODER.encode(password);
	}

}
