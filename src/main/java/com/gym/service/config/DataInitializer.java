package com.gym.service.config;

import com.gym.service.entity.User;
import com.gym.service.entity.Admin;
import com.gym.service.entity.Product;
import com.gym.service.repository.UserRepository;
import com.gym.service.repository.AdminRepository;
import com.gym.service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!adminRepository.findByUsername("admin").isPresent()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("系统管理员");
            admin.setEmail("admin@fitlife.com");
            adminRepository.save(admin);
            System.out.println("Default admin user created: admin / admin123, email: admin@fitlife.com");
        }

        // 初始化商品数据
        if (productRepository.count() == 0) {
            Product product1 = new Product();
            product1.setName("专业跑步机");
            product1.setDescription("高端商用跑步机，带多种运动模式和心率监测功能");
            product1.setCategory("健身器材");
            product1.setPrice(3999.99);
            product1.setStockQuantity(10);
            product1.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20treadmill%20in%20gym&image_size=square_hd");
            
            Product product2 = new Product();
            product2.setName("哑铃套装");
            product2.setDescription("20-50kg可调节哑铃套装，适合家庭健身");
            product2.setCategory("健身器材");
            product2.setPrice(1299.00);
            product2.setStockQuantity(20);
            product2.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=adjustable%20dumbbell%20set&image_size=square_hd");
            
            Product product3 = new Product();
            product3.setName("运动紧身衣");
            product3.setDescription("高弹性运动紧身衣，透气排汗，适合各种运动");
            product3.setCategory("运动服装");
            product3.setPrice(299.00);
            product3.setStockQuantity(30);
            product3.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=sports%20compression%20shirt&image_size=square_hd");
            
            Product product4 = new Product();
            product4.setName("蛋白质粉");
            product4.setDescription("乳清蛋白粉，增强肌肉恢复，提高运动表现");
            product4.setCategory("营养补充");
            product4.setPrice(499.00);
            product4.setStockQuantity(15);
            product4.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=protein%20powder%20container&image_size=square_hd");
            
            Product product5 = new Product();
            product5.setName("智能手环");
            product5.setDescription("多功能智能手环，监测心率、步数、睡眠等");
            product5.setCategory("智能设备");
            product5.setPrice(599.00);
            product5.setStockQuantity(12);
            product5.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20fitness%20band&image_size=square_hd");
            
            Product product6 = new Product();
            product6.setName("运动水壶");
            product6.setDescription("大容量运动水壶，保温保冷，适合户外运动");
            product6.setCategory("运动配件");
            product6.setPrice(89.00);
            product6.setStockQuantity(40);
            product6.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=sports%20water%20bottle&image_size=square_hd");
            
            Product[] products = {product1, product2, product3, product4, product5, product6};

            for (Product product : products) {
                productRepository.save(product);
            }
            System.out.println("Default products created: " + products.length + " items");
        }
    }
}
