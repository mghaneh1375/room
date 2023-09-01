package bogen.studio.Room.Security;


import bogen.studio.Room.Service.MyUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetails myUserDetails;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String token = header.split(" ")[1].trim();

        String pubkeyb64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtx5XIQ7QRnKZRRDexf7X" +
                "zZxMhf+hE807qwi0Ul1WWcLt5be7zsHGdOsn3BGGB8BAmeA54qespU7MJFNIW21l" +
                "Qb/XqexShrsiOvVxs8Z75RZfA2UjYwV1tHW58MTIgRdER67aJj0hIofgOFztB0CN" +
                "RHaehltR3up3tEPnz0HxsuSESmPccU86YJUKyu2QUW7hcrj0yUBeFiFrDhRKel5O" +
                "9+X862FOE+aSWAaX69hTUTf8CDSXpAlH93xX27Uz5h/bTbSIB2fXbsINe0d4HdX2" +
                "TQceyBQe+LoNmIfrnTPjyvf67ICGYFkCH8G7zF9851o63sbquWKA6NQ90ydkV/hO" +
                "twIDAQAB";

        byte[] pubkeyder = Base64.getDecoder().decode(pubkeyb64);
        PublicKey pubkey = null;
        try {
            pubkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubkeyder));

            Claims claims = Jwts.parser().setSigningKey(pubkey).parseClaimsJws(token).getBody();

            Object username =  claims.get("user_name");
            Object exp = claims.get("exp");

            if(username == null || exp == null) {
                chain.doFilter(request, response);
                return;
            }

            if((int)exp < System.currentTimeMillis() / 1000) {
                chain.doFilter(request, response);
                return;
            }

            UserDetails userDetails = myUserDetails.loadUserByUsername(username.toString());

            if(userDetails == null) {
                chain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken
                    authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null,
                    userDetails.getAuthorities()
            );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
