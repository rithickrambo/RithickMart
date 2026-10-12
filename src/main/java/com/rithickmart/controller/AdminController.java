package com.rithickmart.controller;
import com.rithickmart.model.*;import com.rithickmart.repo.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')") public class AdminController {
 private final LoginHistoryRepository history;private final UserRepository users;public AdminController(LoginHistoryRepository h,UserRepository u){history=h;users=u;}
 @GetMapping("/login-history") public List<LoginHistory> history(){return history.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"eventAt"));}
 @GetMapping("/users") public List<Map<String,Object>> users(){return users.findAll().stream().map(u->Map.<String,Object>of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"role",u.getRole().name(),"createdAt",u.getCreatedAt())).toList();}
 @PutMapping("/users/{id}/role") public Map<String,String> role(@PathVariable Long id,@RequestBody Map<String,String> body){User u=users.findById(id).orElseThrow();Role r=Role.valueOf(body.getOrDefault("role","BUYER").toUpperCase());u.setRole(r==Role.ADMIN?Role.BUYER:r);users.save(u);return Map.of("message","Role updated");}
}
