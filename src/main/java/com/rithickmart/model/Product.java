package com.rithickmart.model;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.LocalDateTime;
@Entity @Table(name="products") public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private String name; private String category; @Column(nullable=false,precision=10,scale=2) private BigDecimal price=BigDecimal.ZERO; private int stock; private String image; @Column(length=2000) private String description; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;} public String getCategory(){return category;} public void setCategory(String v){category=v;} public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;} public int getStock(){return stock;} public void setStock(int v){stock=v;} public String getImage(){return image;} public void setImage(String v){image=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
