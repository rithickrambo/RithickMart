package com.rithickmart;
import com.rithickmart.model.*;import com.rithickmart.repo.*;import org.springframework.boot.*;import org.springframework.boot.autoconfigure.SpringBootApplication;import org.springframework.context.annotation.Bean;import org.springframework.security.crypto.password.PasswordEncoder;
@SpringBootApplication
public class RithickMartApplication {
 public static void main(String[] args){SpringApplication.run(RithickMartApplication.class,args);}
 @Bean CommandLineRunner seed(UserRepository users,ProductRepository products,PasswordEncoder encoder){return args->{if(!users.existsByEmail("admin@rithickmart.com")){User u=new User();u.setName("RithickMart Admin");u.setEmail("admin@rithickmart.com");u.setPassword(encoder.encode("ChangeMe@123"));u.setRole(Role.ADMIN);users.save(u);}if(products.count()==0){String[][] p={{"Air Runner Sneakers","Sneakers","2499","25","👟"},{"Velocity Sports Shoes","Sports","1899","30","🏃"},{"Classic Formal Shoes","Formal","2799","18","👞"},{"Everyday Walking Shoes","Walking","1499","35","🥾"},{"Canvas Casual Shoes","Casual","999","40","👟"},{"Trail Grip Shoes","Outdoor","3199","15","🥾"}};for(String[] x:p){Product z=new Product();z.setName(x[0]);z.setCategory(x[1]);z.setPrice(new java.math.BigDecimal(x[2]));z.setStock(Integer.parseInt(x[3]));z.setImage(x[4]);z.setDescription(x[0]);products.save(z);}}};}
}
