package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/users")
public class UserController implements IUserController {

  @Autowired
    private IUserService userService;

  @PostMapping("/register")
  @Override
  public ResponseEntity<User> createUser(@RequestBody User user) {
    if (user == null
    || user.getFirstName() == null || user.getFirstName().isEmpty()
    || user.getLastName() == null || user.getLastName().isEmpty()
    || user.getEmail() == null || user.getEmail().isEmpty()
    || user.getPassword() == null || user.getPassword().isEmpty()){

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }
    try{
      userService.createUser(user);
      userService.sendActivationMail(user.getEmail());
      return new ResponseEntity<>(user, HttpStatus.OK);
    } catch (Exception e){
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }
  @PostMapping("/send-activation")
  @Override
  public ResponseEntity<User> sendActivationMail(@RequestParam String email) {
    try {
      userService.sendActivationMail(email);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/activate/{id}")
  @Override
  public ResponseEntity<User> activateUser(@PathVariable long id) {
    try {
      User activedUser = userService.activeUser(id);
      return new ResponseEntity<>(activedUser, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/login")
  @Override
  public ResponseEntity<User> authenticatedUser(@RequestBody String email, @RequestBody String password){
    if(email == null || email.isEmpty() || password == null || password.isEmpty()){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try{
      if(userService.authenticatedUser(email, password).isActive()){
        return new ResponseEntity<>(HttpStatus.OK);
      }
      else{
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
      }
    }catch (Exception e){
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/unsubscribe")
  @Override
  public ResponseEntity<User> deleteUser(@RequestBody User user) {
    try{
      userService.deleteUser(user.getId());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}