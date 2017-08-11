/*
 * Copyright (C) 2005 - 2014 Jaspersoft Corporation. All rights  reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License  as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero  General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program.&nbsp; If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.jaxrs.client.core;

import com.jaspersoft.jasperserver.jaxrs.client.core.enums.AuthenticationType;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AuthenticationFailedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.JSClientWebException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.jaspersoft.jasperserver.jaxrs.client.filters.BasicAuthenticationFilter;
import com.jaspersoft.jasperserver.jaxrs.client.filters.SessionOutputFilter;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BufferedHeader;
import org.glassfish.jersey.client.ClientProperties;

public class JasperserverRestClient {
    private final RestClientConfiguration configuration;

    public JasperserverRestClient(RestClientConfiguration configuration) {
        if (configuration == null || configuration.getJasperReportsServerUrl() == null) {
            throw new IllegalArgumentException("You must define the configuration (at least Jasperserver URL)");
        }
        this.configuration = configuration;
    }

    public Session authenticate(String username, String password) {
        return this.authenticate(username, password, Locale.getDefault(), TimeZone.getDefault());
    }

    public Session authenticate(String username, String password, String userTimeZone) {
        return this.authenticate(username, password, Locale.getDefault(), TimeZone.getTimeZone(userTimeZone));
    }

    public Session authenticate(String username, String password, TimeZone userTimeZone) {
        return this.authenticate(username, password, Locale.getDefault(), userTimeZone);
    }
    public Session authenticate(String username, String password, String userLocale, String userTimeZone) {
        return this.authenticate(username,
                password,
                (userLocale == null) ? Locale.getDefault(): new Locale(userLocale),
                (userTimeZone == null) ? TimeZone.getDefault(): TimeZone.getTimeZone(userTimeZone));
    }

    public Session authenticate(String username, String password, Locale userLocale, TimeZone userTimeZone) {

        if (username != null && username.length() > 0 && password != null && password.length() > 0) {
            AuthenticationCredentials credentials = new AuthenticationCredentials(username, password);
            SessionStorage sessionStorage =
                    new SessionStorage(configuration,
                            credentials,
                            (userLocale == null) ? Locale.getDefault() : userLocale,
                            (userTimeZone == null) ? TimeZone.getDefault() : userTimeZone);
            login(sessionStorage);
            return new Session(sessionStorage);
        }
        return null;
    }

    public Session getTokenSession (String username, String role, String organization, String... pAttributes) {
        if (username != null && username.length() > 0 && role != null && role.length() > 0 && organization != null && organization.length() > 0) {
            AuthenticationCredentials credentials = new AuthenticationCredentials(username);
            SessionStorage sessionStorage = new SessionStorage(configuration,
                    credentials,
                    Locale.getDefault(),
                    TimeZone.getDefault());
            getToken(sessionStorage, role, organization);
            return new Session(sessionStorage);
        }

        return null;
    }

    public AnonymousSession getAnonymousSession() {
        return new AnonymousSession(new SessionStorage(configuration, null, Locale.getDefault(), TimeZone.getDefault()));
    }

    protected void login(SessionStorage storage) throws JSClientWebException {

        AuthenticationCredentials credentials = storage.getCredentials();
        WebTarget rootTarget = storage.getRootTarget();
        if (configuration.getAuthenticationType() == AuthenticationType.BASIC) {
            rootTarget.register(new BasicAuthenticationFilter(credentials));
            return;
        }
        Form form = new Form();
        form.param("j_username", credentials.getUsername()).param("j_password", credentials.getPassword());
        form.param("userTimezone", storage.getUserTimeZone().getID());
        form.param("userLocale", storage.getUserLocale().toString());
        WebTarget target = rootTarget.path("/j_spring_security_check")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
        Response response = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        if (response.getStatus() == Status.FOUND.getStatusCode()) {
            String location = response.getLocation().toString();
            String sessionId;
            if (!location.matches("[^?]+\\?([^&]*&)*error=1(&[^&]*)*$")) {
                sessionId = response.getCookies().get("JSESSIONID").getValue();
                storage.setSessionId(sessionId);
            } else {
                throw new AuthenticationFailedException("Invalid credentials supplied. Could not login to JasperReports Server.");
            }
            rootTarget.register(new SessionOutputFilter(sessionId));
        } else {
            throw  new ResourceNotFoundException("Server was not found");
        }
    }

        protected void getToken(SessionStorage sessionStorage, String role, String organization, String... pAttributes) {
        AuthenticationCredentials credentials = sessionStorage.getCredentials();
        Form form = new Form();
        form.param("principleParameter", "pp").param("u", credentials.getUsername()).param("r", role).param("o", organization);

        WebTarget rootTarget = sessionStorage.getRootTarget();

        WebTarget queryParam = rootTarget.queryParam("pp", "u=" + credentials.getUsername() + "|r=" + role + "|o=" + organization);
        Invocation.Builder acceptTarget = queryParam.request().accept(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        Response response = acceptTarget.get(Response.class);
//
//            WebTarget target = rootTarget.queryParam("pp", "u=" + credentials.getUsername() + "|r=" + role + "|o=" + organization)
//                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
//        Response response = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON, "UTF-8").cookie("Set-Cookie",sessionStorage.getSessionId()).get();
        if (response.getStatus() == Status.OK.getStatusCode()) {
            Map<String, NewCookie> testCookie = response.getCookies();
            NewCookie newCookie = testCookie.get("JSESSIONID");

            String sessionId = newCookie.getValue();
//                sessionId = response.getCookies().get("JSESSIONID").getValue();
//                Map<String, Object> jsonResponse = response.readEntity(Map.class);
                sessionStorage.setSessionId(sessionId);
            rootTarget.register(new SessionOutputFilter(sessionId));
        } else {
            throw new ResourceNotFoundException("Server was not found");
        }
    }

//    protected void getToken(SessionStorage sessionStorage, String role, String organization, String... pAttributes) {
//        AuthenticationCredentials credentials = sessionStorage.getCredentials();
//        WebTarget rootTarget = sessionStorage.getRootTarget();
//        HttpClient client = HttpClientBuilder.create().build();
//        HttpGet request = new HttpGet(rootTarget.getUri() + "?pp=u%3D" + credentials.getUsername()
//                + "%7Cr%3D" + role + "%7Co%3D" + organization);
//        HttpResponse response;
//        String ret;
//        try {
//            response = client.execute(request);
//            ret = response.getHeaders("Set-Cookie")[0].getValue();
//            Map<String, String> cookieMap = parseMap(ret);
//            if (cookieMap.containsKey("JSESSIONID")) {
//                String sessionId = cookieMap.get("JSESSIONID");
//                sessionStorage.setSessionId(sessionId);
//                rootTarget.register(new SessionOutputFilter(sessionId));
//            } else {
//                throw new AuthenticationFailedException("There was no JSESSIONID returned in the request");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private Map<String, String> parseMap(String input) {
        Map<String, String> map = new HashMap<String, String>();
        for (String str : input.split(";")) {
            String[] kv = str.split("=");
            if (kv.length > 1) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
