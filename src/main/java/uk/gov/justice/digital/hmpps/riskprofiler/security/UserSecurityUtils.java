package uk.gov.justice.digital.hmpps.riskprofiler.security;

import org.apache.commons.lang3.RegExUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserSecurityUtils implements AuthenticationFacade {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public String getCurrentUsername() {
        final String username;

        final var userPrincipal = getUserPrincipal();

        if (userPrincipal instanceof String) {
            username = (String) userPrincipal;
        } else if (userPrincipal instanceof UserDetails) {
            username = ((UserDetails) userPrincipal).getUsername();
        } else if (userPrincipal instanceof Map) {
            final var userPrincipalMap = (Map) userPrincipal;
            username = (String) userPrincipalMap.get("username");
        } else {
            username = null;
        }

        return username;
    }

    private Object getUserPrincipal() {
        final Authentication auth = getAuthentication();
        return auth != null ? auth.getPrincipal() : null;
    }
}
