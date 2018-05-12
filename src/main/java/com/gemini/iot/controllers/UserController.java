package com.gemini.iot.controllers;

import com.gemini.iot.dto.ChangePasswordRequest;
import com.gemini.iot.dto.UserDto;
import com.gemini.iot.dto.UserProfileDto;
import com.gemini.iot.dto.UserRegisterRequest;
import com.gemini.iot.exceptions.AlreadyExistException;
import com.gemini.iot.exceptions.DeviceNotFoundException;
import com.gemini.iot.exceptions.UserNotFoundException;
import com.gemini.iot.exceptions.WrongPasswordException;
import com.gemini.iot.models.User;
import com.gemini.iot.secure.TokenUtils;
import com.gemini.iot.services.UserService;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api")
public class UserController {
    final private UserService userService;
    final private Logger logger = Logger.getLogger(UserController.class);
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private AuthenticationManager authenticationManager;
    private TokenUtils tokenUtils;
    public UserController(UserService userService, BCryptPasswordEncoder e, AuthenticationManager authenticationManager,
                          TokenUtils tokenUtils, UserDetailsService userDetailsService){
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
        this.bCryptPasswordEncoder = e;

    }
    @PostMapping("sign_up")
    @ResponseBody
    User registerUser(@RequestBody UserRegisterRequest user) throws AlreadyExistException,DeviceNotFoundException {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return  userService.registerUser(user);
    }

    @PostMapping("sign_in")
    @ResponseBody
    String sign_in(@RequestBody UserRegisterRequest user) {
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword() )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = this.userService.loadUserByUsername(user.getUsername());
        return this.tokenUtils.generateToken(userDetails);
    }
    @PutMapping("change_password")
    @ResponseBody
    String changePassword(@RequestBody ChangePasswordRequest passwordDto, Authentication authentication)  throws WrongPasswordException {
        return userService.changePassword(authentication.getName(),passwordDto.getOldPassword(),passwordDto.getNewPassword());
    }

    @GetMapping("user")
    UserProfileDto getProfile(Authentication authentication)  throws UserNotFoundException {
        return  userService.getUserProfile(authentication.getName());
    }
    @GetMapping("user/{id}")
    UserDto getUser(@PathVariable  String id  ) throws UserNotFoundException  {
        return userService.getUser(UUID.fromString(id));
    }
}
