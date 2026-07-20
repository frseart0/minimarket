package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.entity.Proveedor;
import com.minimarket.entity.Rol;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.repository.ProveedorRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Carga datos de demostración al iniciar la aplicación (roles, usuarios,
 * sucursales, productos, stock, proveedores y una promoción vigente) para
 * poder probar la autenticación JWT, los roles, la disponibilidad por
 * sucursal y los enlaces HATEOAS desde Swagger UI o Postman.
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
                               SucursalRepository sucursalRepository,
                               StockSucursalRepository stockSucursalRepository,
                               ProveedorRepository proveedorRepository,
                               PromocionRepository promocionRepository,
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

            Producto agua = productoRepository.save(nuevoProducto("Agua mineral 500ml", 990.0, 50, bebidas));
            Producto bebidaCola = productoRepository.save(nuevoProducto("Bebida cola 1.5L", 1890.0, 30, bebidas));
            Producto arroz = productoRepository.save(nuevoProducto("Arroz 1kg", 1290.0, 40, abarrotes));

            Sucursal providencia = sucursalRepository.save(nuevaSucursal("Sucursal Providencia", "Av. Providencia 1234", "Providencia"));
            Sucursal lasCondes = sucursalRepository.save(nuevaSucursal("Sucursal Las Condes", "Av. Apoquindo 4500", "Las Condes"));

            // Stock por sucursal: se deja un producto cerca del mínimo para poder demostrar
            // la reposición automática al registrar una salida de inventario.
            stockSucursalRepository.save(nuevoStock(agua, providencia, 30, 10));
            stockSucursalRepository.save(nuevoStock(bebidaCola, providencia, 6, 5));
            stockSucursalRepository.save(nuevoStock(arroz, providencia, 20, 8));
            stockSucursalRepository.save(nuevoStock(agua, lasCondes, 25, 10));
            stockSucursalRepository.save(nuevoStock(bebidaCola, lasCondes, 15, 5));
            stockSucursalRepository.save(nuevoStock(arroz, lasCondes, 18, 8));

            Proveedor proveedor = new Proveedor();
            proveedor.setNombre("Distribuidora Central S.A.");
            proveedor.setContacto("Juan Pérez");
            proveedor.setTelefono("+56 2 2345 6789");
            proveedor.setEmail("contacto@distribuidoracentral.cl");
            proveedorRepository.save(proveedor);

            promocionRepository.save(nuevaPromocionVigente(arroz, 15.0));
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

    private Sucursal nuevaSucursal(String nombre, String direccion, String comuna) {
        Sucursal s = new Sucursal();
        s.setNombre(nombre);
        s.setDireccion(direccion);
        s.setComuna(comuna);
        s.setTelefono("+56 2 2000 0000");
        return s;
    }

    private StockSucursal nuevoStock(Producto producto, Sucursal sucursal, int cantidad, int stockMinimo) {
        StockSucursal stock = new StockSucursal();
        stock.setProducto(producto);
        stock.setSucursal(sucursal);
        stock.setCantidad(cantidad);
        stock.setStockMinimo(stockMinimo);
        return stock;
    }

    private Promocion nuevaPromocionVigente(Producto producto, double porcentajeDescuento) {
        Calendar calendar = Calendar.getInstance();
        Date inicio = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        Date fin = calendar.getTime();

        Promocion promocion = new Promocion();
        promocion.setNombre("Descuento de lanzamiento");
        promocion.setDescripcion("Descuento especial por tiempo limitado");
        promocion.setPorcentajeDescuento(porcentajeDescuento);
        promocion.setProducto(producto);
        promocion.setFechaInicio(inicio);
        promocion.setFechaFin(fin);
        return promocion;
    }
}
