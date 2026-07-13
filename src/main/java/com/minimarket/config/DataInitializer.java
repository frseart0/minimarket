package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Carga datos de demostración al iniciar la aplicación (roles, usuarios y
 * productos) para poder probar la autenticación, los roles y los enlaces
 * HATEOAS desde Swagger UI o Postman.
 *
 * Credenciales demo:
 *   admin / admin123     -> rol ADMIN
 *   cajero / cajero123   -> rol CAJERO
 *   cliente / cliente123 -> rol CLIENTE
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UsuarioRepository usuarioRepository,
                               RolRepository rolRepository,
                               CategoriaRepository categoriaRepository,
                               ProductoRepository productoRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                return; // ya inicializado
            }

            Rol admin = rolRepository.save(new Rol("ADMIN"));
            Rol cajero = rolRepository.save(new Rol("CAJERO"));
            Rol cliente = rolRepository.save(new Rol("CLIENTE"));

            crearUsuario(usuarioRepository, passwordEncoder, "admin", "admin123", Set.of(admin));
            crearUsuario(usuarioRepository, passwordEncoder, "cajero", "cajero123", Set.of(cajero));
            crearUsuario(usuarioRepository, passwordEncoder, "cliente", "cliente123", Set.of(cliente));

            Categoria bebidas = categoriaRepository.save(nuevaCategoria("Bebidas"));
            Categoria abarrotes = categoriaRepository.save(nuevaCategoria("Abarrotes"));

            productoRepository.save(nuevoProducto("Agua mineral 500ml", 990.0, 50, bebidas));
            productoRepository.save(nuevoProducto("Bebida cola 1.5L", 1890.0, 30, bebidas));
            productoRepository.save(nuevoProducto("Arroz 1kg", 1290.0, 40, abarrotes));
        };
    }

    private void crearUsuario(UsuarioRepository repo, PasswordEncoder encoder,
                              String username, String password, Set<Rol> roles) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setRoles(roles);
        repo.save(u);
    }

    private Categoria nuevaCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        return c;
    }

    private Producto nuevoProducto(String nombre, Double precio, Integer stock, Categoria categoria) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setCategoria(categoria);
        return p;
    }
}
