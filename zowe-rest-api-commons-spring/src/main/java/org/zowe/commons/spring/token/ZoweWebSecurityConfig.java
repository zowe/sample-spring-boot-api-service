package org.zowe.commons.spring.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class ZoweWebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final AuthConfigurationProperties authConfigurationProperties;
    private SuccessfulLoginHandler successfulLoginHandler;
    private final TokenFailureHandler tokenFailureHandler;

    @Autowired
    TokenService tokenService;

    public ZoweWebSecurityConfig(AuthConfigurationProperties authConfigurationProperties,
                                 SuccessfulLoginHandler successfulLoginHandler,
                                 TokenFailureHandler tokenFailureHanlder) {
        this.authConfigurationProperties = authConfigurationProperties;
        this.successfulLoginHandler = successfulLoginHandler;
        this.tokenFailureHandler = tokenFailureHanlder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //CSRF configuration
        http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).ignoringAntMatchers("/api/**")

            .and()
            .headers()
            .httpStrictTransportSecurity().disable()
            .frameOptions().disable()

            //Session config
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            //Login endpoint
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, authConfigurationProperties.getServiceLoginEndpoint()).permitAll()
            .anyRequest().authenticated()

            .and()
            .addFilterBefore(loginFilter(authConfigurationProperties.getServiceLoginEndpoint(), authenticationManager()), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JWTAuthorizationFilter(tokenFailureHandler, authConfigurationProperties), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new CookieAuthorizationFilter(tokenFailureHandler, authConfigurationProperties), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Processes /login requests
     */
    private LoginFilter loginFilter(String loginEndpoint,
                                    AuthenticationManager authenticationManager) {
        return new LoginFilter(
            loginEndpoint,
            authConfigurationProperties,
            authenticationManager,
            successfulLoginHandler);
    }

    /**
     * Method to provide Authentication provider as it cant be null
     * in spring security filter chain
     *
     * @param auth
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider((AuthenticationProvider) tokenService);
    }
}
