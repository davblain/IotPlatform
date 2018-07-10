package com.gemini.iot.services;

import com.gemini.iot.dto.*;
import com.gemini.iot.exceptions.AlreadyExistException;
import com.gemini.iot.exceptions.DeviceNotFoundException;
import com.gemini.iot.exceptions.UserNotFoundException;
import com.gemini.iot.exceptions.WrongPasswordException;
import com.gemini.iot.models.User;
import com.gemini.iot.repository.GroupDao;
import com.gemini.iot.repository.RoleDao;
import com.gemini.iot.repository.DeviceDao;
import com.gemini.iot.repository.UserDao;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService{
    final private UserDao userDao;
    final private DeviceDao deviceDao;
    final private RoleDao roleDao;
    final private GroupDao groupDao;
    final private ModelMapper modelMapper;
    final private BCryptPasswordEncoder bCryptPasswordEncoder;
    UserService(UserDao userDao, DeviceDao deviceDao, RoleDao roleDao, GroupDao groupDao, BCryptPasswordEncoder bCryptPasswordEncoder, ModelMapper modelMapper) {
        this.userDao = userDao;
        this.deviceDao = deviceDao;
        this.roleDao = roleDao;
        this.groupDao = groupDao;
        this.modelMapper = modelMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    @Transactional
    public User registerUser(UserRegisterRequest userRegisterDto)  throws AlreadyExistException, DeviceNotFoundException {
        if (userDao.existsByUsername(userRegisterDto.getUsername())) throw  new AlreadyExistException(userRegisterDto.getUsername());
        User user =  modelMapper.map(userRegisterDto,User.class);
        user.setRoles(Arrays.asList(roleDao.findByName("ROLE_USER")));
        return  userDao.save(user);


    }

    @Transactional
    public User getUserByUsername(String name) throws UserNotFoundException {
        return Optional.ofNullable(userDao.findUserByUsername(name)).orElseThrow(() -> new UserNotFoundException(name) );
    }
    @Transactional
    public UserProfileDto getUserProfile(String name) throws UserNotFoundException {

        return modelMapper.map(getUserByUsername(name),UserProfileDto.class);
    }


    public UserDto getUser(UUID uuid) throws UserNotFoundException{
        return Optional.ofNullable(userDao.findOne(uuid)).map(user -> modelMapper.map(user, UserDto.class))
                .orElseThrow(()-> new UserNotFoundException(uuid.toString()));
    }

    public List<GroupDto> getAdministratedGroup(String username) {
        return Optional.ofNullable( userDao.findUserByUsername(username))
                .map(u -> groupDao.findByAdmin(u).stream()
                        .map(g -> modelMapper.map(g,GroupDto.class))
                                .collect(Collectors.toList()))
                .orElseThrow(()-> new UserNotFoundException(username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return  getUserByUsername(username);
    }
    @Transactional
    public String changePassword(String name,String oldPassword, String password)throws UserNotFoundException {
        User user = getUserByUsername(name);
        if (bCryptPasswordEncoder.matches(oldPassword,user.getPassword())) {
            user.setPassword(bCryptPasswordEncoder.encode(password));
            userDao.save(user);
            return "SUCCESS";
        } else {
            throw  new WrongPasswordException();
        }
    }

}
