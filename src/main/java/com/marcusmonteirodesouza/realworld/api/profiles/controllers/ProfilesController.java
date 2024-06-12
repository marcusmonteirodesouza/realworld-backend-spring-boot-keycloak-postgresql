package com.marcusmonteirodesouza.realworld.api.profiles.controllers;

import com.marcusmonteirodesouza.realworld.api.authentication.IAuthenticationFacade;
import com.marcusmonteirodesouza.realworld.api.profiles.controllers.dto.ProfileResponse;
import com.marcusmonteirodesouza.realworld.api.profiles.controllers.dto.ProfileResponse.ProfileResponseProfile;
import com.marcusmonteirodesouza.realworld.api.profiles.services.ProfilesService;
import com.marcusmonteirodesouza.realworld.api.users.services.users.UsersService;
import jakarta.ws.rs.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/profiles")
public class ProfilesController {
    private final IAuthenticationFacade authenticationFacade;
    private final UsersService usersService;
    private final ProfilesService profilesService;

    public ProfilesController(
            IAuthenticationFacade authenticationFacade,
            UsersService usersService,
            ProfilesService profilesService) {
        this.authenticationFacade = authenticationFacade;
        this.usersService = usersService;
        this.profilesService = profilesService;
    }

    @PostMapping("/{username}/follow")
    public ProfileResponse followUser(@PathVariable String username) {
        var user = usersService.getUserByUsername(username).orNull();

        if (user == null) {
            throw new NotFoundException("Username '" + username + "' not found");
        }

        var authenticatedUserId = authenticationFacade.getAuthentication().getName();

        profilesService.followUser(authenticatedUserId, user.getId());

        return new ProfileResponse(
                new ProfileResponseProfile(
                        user.getUsername(),
                        user.getBio().orNull(),
                        user.getImage().orNull(),
                        true));
    }

    @GetMapping("/{username}")
    public ProfileResponse getProfile(@PathVariable String username) {
        var user = usersService.getUserByUsername(username).orNull();

        if (user == null) {
            throw new NotFoundException("Username '" + username + "' not found");
        }

        var authentication = authenticationFacade.getAuthentication();
        var isFollowing = false;

        if (authentication != null) {
            var authenticatedUserId = authentication.getName();

            isFollowing = this.profilesService.isFollowing(authenticatedUserId, user.getId());
        }

        return new ProfileResponse(
                new ProfileResponseProfile(
                        user.getUsername(),
                        user.getBio().orNull(),
                        user.getImage().orNull(),
                        isFollowing));
    }
}
