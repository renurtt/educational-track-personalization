package com.nurtdinov.educationaltrackpersonalization.security.config;

import javax.crypto.SecretKey;

import com.nurtdinov.educationaltrackpersonalization.exception.ExceptionHandlerFilter;
import com.nurtdinov.educationaltrackpersonalization.security.api.JwtAuthenticationFilter;
import com.nurtdinov.educationaltrackpersonalization.security.jwt.JwtConfig;
import com.nurtdinov.educationaltrackpersonalization.security.api.JwtTokenVerifier;
import com.nurtdinov.educationaltrackpersonalization.security.service.ApplicationUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.nurtdinov.educationaltrackpersonalization.security.ApplicationUserRole.USER;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    private final ApplicationUserService applicationUserService;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .addFilterBefore(new ExceptionHandlerFilter(), JwtAuthenticationFilter.class)
                    .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtConfig, secretKey))
                    .addFilterAfter(new JwtTokenVerifier(jwtConfig, secretKey), JwtAuthenticationFilter.class)
                .authorizeRequests()
//                    .antMatchers("/auth/check/user").hasRole(USER.name())
//                    .antMatchers("/auth/check/author").hasRole(AUTHOR.name())
//                    .antMatchers("/mock/**").permitAll()
                    .antMatchers("/questionnaire/**").hasRole(USER.name())
                    .antMatchers("/track/**").hasRole(USER.name())
                    .antMatchers("/article/**").permitAll()
                    .antMatchers("/auth/**").permitAll()
                    .antMatchers("/test/**").permitAll()
                    .antMatchers("/**").hasRole(USER.name())
                    .anyRequest().authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(applicationUserService);
        return provider;
    }
}
