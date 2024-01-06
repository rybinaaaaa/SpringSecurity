# #Spring/security
## 0. Аунтентификация / авторизация

**Аутентификация** -  вход в систему (кто ты)
**Авторизация** - предоставление прав после аутентификации (какую ты играешь роль - пользователь / админ)

Spring security работает с помощью фильтров

> **Фильтр** - объект, который перехватывает все хттп запросы

![](Screenshot%202023-10-18%20at%2017.43.18.png)

## 1. Имплементация

Для Спринг секьюрити нам надо написать: `SecurityConfig` , `authProvider`,  `PersonDetails`, `PersonDetailService`

- `authProvider` 

```
@Component
public class AuthProviderImpl implements AuthenticationProvider {

    private final PersonDetailService personDetailService;

    @Autowired
    public AuthProviderImpl(PersonDetailService personDetailService) {
        this.personDetailService = personDetailService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();

        UserDetails personDetails = personDetailService.loadUserByUsername(username);

        String password = authentication.getCredentials().toString();
        if (!password.equals(personDetails.getPassword())) {
            throw new BadCredentialsException("Bad Craditionals");
        }

        return new UsernamePasswordAuthenticationToken(personDetails, password, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
```

- `PersonDetails` - класс - обертка над нашей сущностью, которая представляет из себя сущность пользователя с **password**, **username**

```
class PersonDetails implements UserDetails
```

- `PersonDetailService` - класс, который загружает из БД сущность Person и парсит ее в сущность PersonDetails

```
@Service
public class PersonDetailService implements UserDetailsService {
    private final PeopleRepository peopleRepository;

    @Autowired
    public PersonDetailService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Person> person = peopleRepository.findByUsername(username);

        if (person.isEmpty()) {
            throw new UsernameNotFoundException("User does not exist!");
        }

        return new PersonDetails(person.get());
    }
}
```

- `SecurityConfig` - класс с конфигом. Раньше надо было самим писать используемые провайдеры, но в спринге 6 этого делать больше не надо![](Screenshot%202023-10-20%20at%2017.23.20.png)<!-- {"width":369} -->
  раньше было так*

Итого мы имеем такую UML
![](Screenshot%202023-10-20%20at%2017.25.00.png)

## 2. Убираем кастомные провайдер

> Кастомный провайдер нужен в случае нестандартной аутентификации, например если мы не можем достать пароль с бд сервера (бывает часто такое, что пароль хранится на удаленном центральном сервере **CAS**)

Для того нам надо просто напросто **обязательно, иначе будет ошибка** написать метод шифрования пароля в конфигурации. **ТАКЖЕ В ФОРМЕ ЛОГИНА НАДО УКАЗЫВАТЬ action="/process_login"**

```
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
```

## 3. Кастомный логин

Добавляем в нашу конфигурацию

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auths ->
            auths.requestMatchers("/auth/login", "/error").permitAll()
                    .anyRequest().authenticated())
            .formLogin(login -> login
            .loginPage("/auth/login")
            .loginProcessingUrl("/process_login")
            .defaultSuccessUrl("/hello", true)
            .failureUrl("/auth/login?error")
    );
    return http.build();
}
```

**permitAll - доступно всем**
**authenticated - доступно аутентифицированым**

## 4. Регистрация
— регистрацию мы делаем полностью сами, просто добавляем обычный сервис, валидатор и форму метода пост в хтмл

## 5. Шифрование CSRF

**Имплементация - добавляем шифрование**

```
SecurityConfig:

@Bean
public PasswordEncoder getPasswordEncoder() {
 -->   return new BCryptPasswordEncoder();
}

RegistrationService:

@Transactional
public void register(Person person) {
    String password = person.getPassword();
  -->  String encodedPass = passwordEncoder.encode(password);

    person.setPassword(encodedPass);

    peopleRepository.save(person);
}

```

Логин автоматически как мы уже знаем делается с помощью секюрити.

 **Имплементация - добавляем csrf**

```
login.html

<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
```

— в регистрации все добавлено автоматически, мы ничего не делаем, log out должен представлять из себя форму

```
<form th:action="@{/logout}" th:method="post">
    <input type="submit" value="Log out" />
</form>
```
## 6. Авторизация

Авторищировать людей можно с помощью ролей (давая им допустимый набор действий) либо просто предоставлением действий. Для спринга разницы нет. Но проще и чаще делать с помощью ролей

1. Роли все начитаются с приставки *ROLE*
2. Определяем так:
```
PersonDetails:

@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(person.getRole()));
}
```

Тут мы скорее по переводу передаем список действий. Но в нашей имплементацию мы передаем роль
3. Добавляем конфиг 

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(auths ->
            ----->        auths.requestMatchers("/admin").hasRole("ADMIN")
                    .requestMatchers("/auth/login", "auth/registration", "/error").permitAll()
              --->              .anyRequest().hasAnyRole("USER", "ADMIN"))
            .formLogin(login -> login
                    .loginPage("/auth/login")
                    .loginProcessingUrl("/process_login")
                    .defaultSuccessUrl("/hello", true)
                    .failureUrl("/auth/login?error"))
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/auth/login"));
    return http.build();
}
```

 .anyRequest().hasAnyRole("USER", "ADMIN")) - любой реквест только для ролей user/admin
 .requestMatchers("/admin").hasRole("ADMIN”) - на запрос /админ пускает только админов

## 7. @Preauthorise("hasRole(‘?’)”)

1. Добавляем в конфигурацию аннотацию
```
@EnableMethodSecurity(prePostEnabled = true) //default
```

2. Обычно аннотации прям в контролер не включают. Их помещают например в **сервис**

```
Контроллер: 
@GetMapping("/admin")
public String admin() {
    adminService.doSth(); ----- интересующий нас метод
    return "admin";
}
```

```
Сервис

@Service
public class AdminService {

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void doSth() { --- этот метод
        System.out.println("You are admin");
    }
}

```

*После запроса - запрос пользователя перенаправит на контроллер, но как только вызовется функция* adminService.doSth(), сервер наткнется на то, что у него нет полномочий и выкинет **ошибку 403**  

