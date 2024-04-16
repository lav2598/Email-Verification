package emailverificationdemo.registration;


import emailverificationdemo.event.RegistrationCompleteEvent;
import emailverificationdemo.registration.token.VerificationToken;
import emailverificationdemo.registration.token.VerificationTokenRepository;
import emailverificationdemo.user.User;
import emailverificationdemo.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

    private final UserService userService;
    private final ApplicationEventPublisher publisher;

    private final VerificationTokenRepository tokenRepository;


    @PostMapping
    public String registerUser(@RequestBody  RegistrationRequest registrationRequest, final HttpServletRequest request){
        User user = userService.registerUser(registrationRequest);

        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
        return "Success!! Please check your email for REGISTRATION CONFIRMATION";
    }
    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token){
        VerificationToken theToken = tokenRepository.findByToken(token);
        if (theToken.getUser().isEnabled()){
            return "This Account has been Verified, please Login";

        }

        String verificationResult = userService.validateToken(token);
        if (verificationResult.equalsIgnoreCase("valid")){
            return "Email verified Successfully, Login";

        }
        return "Invalid Verification Token";

    }


    public String applicationUrl(HttpServletRequest request) {
        return "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
    }


}
