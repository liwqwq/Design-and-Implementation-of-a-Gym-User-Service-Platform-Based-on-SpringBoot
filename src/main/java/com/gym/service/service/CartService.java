package com.gym.service.service;

import com.gym.service.entity.Cart;
import com.gym.service.entity.Product;
import com.gym.service.repository.CartRepository;
import com.gym.service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;

    public Cart addToCart(String username, Long productId, int quantity) {
        // 检查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 检查库存
        if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock");
            }
        
        // 检查购物车中是否已有该商品
        Cart existingCartItem = cartRepository.findByUsernameAndProductId(username, productId);
        
        if (existingCartItem != null) {
            // 更新数量
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            return cartRepository.save(existingCartItem);
        } else {
            // 创建新购物车项
            Cart cart = new Cart();
            cart.setUsername(username);
            cart.setProduct(product);
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }
    }

    public List<Cart> getCartItems(String username) {
        return cartRepository.findByUsername(username);
    }

    public void removeFromCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    public void clearCart(String username) {
        List<Cart> cartItems = cartRepository.findByUsername(username);
        cartRepository.deleteAll(cartItems);
    }
}
