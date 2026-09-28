package vn.minxi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.minxi.entity.User;
import vn.minxi.services.UserService;

import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public ResponseEntity<User> authenticatedUser(@AuthenticationPrincipal User currentUser) {
		return ResponseEntity.ok(currentUser);
	}

	@GetMapping
	public ResponseEntity<List<User>> allUsers() {
		List<User> users = userService.allUsers();
		return ResponseEntity.ok(users);
	}
}