package com.rithickmart.model;
import jakarta.persistence.*;import java.time.LocalDateTime;
@Entity @Table(name="login_history") public class LoginHistory {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long userId; private String email; private String role; private String event; private String ipAddress; private LocalDateTime eventAt=LocalDateTime.now();
 public LoginHistory(){} public LoginHistory(Long uid,String email,String role,String event,String ip){this.userId=uid;this.email=email;this.role=role;this.event=event;this.ipAddress=ip;this.eventAt=LocalDateTime.now();}
 public Long getId(){return id;} public Long getUserId(){return userId;} public String getEmail(){return email;} public String getRole(){return role;} public String getEvent(){return event;} public String getIpAddress(){return ipAddress;} public LocalDateTime getEventAt(){return eventAt;}
}
