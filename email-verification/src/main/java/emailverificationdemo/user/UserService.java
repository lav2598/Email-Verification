package emailverificationdemo.user;

import emailverificationdemo.exception.UserAlreadyExistsException;
import emailverificationdemo.registration.RegistrationRequest;
import emailverificationdemo.registration.token.VerificationToken;
import emailverificationdemo.registration.token.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(RegistrationRequest request) {
        Optional<User> user =   this.findByEmail(request.email());
        if (user.isPresent()){
            throw new UserAlreadyExistsException(
                    "User with Email"+
                            request.email()+
                                "already exists");
        }
        var newUser= new User();
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(request.role());
        return userRepository.save(newUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUserVerificationToken(User theUser, String token) {
        var verificationToken = new VerificationToken(token, theUser);
        tokenRepository.save(verificationToken);
    }

    @Override
    public String validateToken(String theToken) {
        VerificationToken token=tokenRepository.findByToken(theToken);
        if (token==null)
        {
            return "Invalid Verification Token";
        }
        User user =token.getUser();
        Calendar calendar =Calendar.getInstance();
        if((token.getExpirationTime().getTime()- calendar.getTime().getTime())<=0){
            tokenRepository.delete(token);
            return "Token Already Expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }
}
