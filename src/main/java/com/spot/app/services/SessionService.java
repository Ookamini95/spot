package com.spot.app.services;

import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
	private final Map<String, Integer> sessions = new ConcurrentHashMap<>();

	public String createSession(int userId) {
		String token = Base64.getEncoder().encodeToString((userId + ":" + UUID.randomUUID()).getBytes());
		sessions.put(token, userId);
		return token;
	}

	public Integer getUserIdFromSession(String token) {
		if (token == null) return null;
		return sessions.get(token);
	}
	public Integer getUserIdFromToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return null; // invalid token
        }
    }
    
}
