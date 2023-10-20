package rybina.SpringBootSecurity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

//    private final AuthProviderImpl authProviderImpl;
//
//    @Autowired
//    public SecurityConfig(AuthProviderImpl authProviderImpl) {
//        this.authProviderImpl = authProviderImpl;
//    }


//    @Bean
//    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
//        AuthenticationManagerBuilder authenticationManagerBuilder =
//                http.getSharedObject(AuthenticationManagerBuilder.class);
//        authenticationManagerBuilder.authenticationProvider(authProviderImpl);
//        return authenticationManagerBuilder.build();
//    }

//    это нам не надо, провайдер автоматически встраивается, я в шоке!!

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//                .anyRequest()
//                .authenticated()
//                .and()
//                .httpBasic(Customizer.withDefaults());
//        return http.build();
//    }
}

