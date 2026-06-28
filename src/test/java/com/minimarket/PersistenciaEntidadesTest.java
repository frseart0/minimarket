package com.minimarket;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de persistencia (capa de repositorios) sobre H2 en memoria.
 * Cubren escenarios de ÉXITO (guardar/recuperar) y de ERROR
 * (violación de restricciones de integridad como campos obligatorios o unicidad).
 */
@DataJpaTest
class PersistenciaEntidadesTest {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    // ---------- ÉXITO ----------
    @Test
    void guardaYRecuperaProductoConCategoria() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Abarrotes");
        categoria = categoriaRepository.save(categoria);

        Producto producto = new Producto();
        producto.setNombre("Fideos");
        producto.setPrecio(890.0);
        producto.setStock(30);
        producto.setCategoria(categoria);

        Producto guardado = productoRepository.save(producto);

        assertNotNull(guardado.getId());
        assertEquals("Fideos", productoRepository.findById(guardado.getId()).get().getNombre());
    }

    @Test
    void encuentraProductosPorCategoria() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Limpieza");
        categoria = categoriaRepository.save(categoria);

        Producto producto = new Producto();
        producto.setNombre("Detergente");
        producto.setPrecio(1990.0);
        producto.setStock(15);
        producto.setCategoria(categoria);
        productoRepository.save(producto);

        assertEquals(1, productoRepository.findByCategoriaId(categoria.getId()).size());
    }

    @Test
    void guardaYBuscaUsuarioPorUsername() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPassword("clave");
        usuarioRepository.save(usuario);

        assertTrue(usuarioRepository.findByUsername("admin").isPresent());
        assertFalse(usuarioRepository.findByUsername("inexistente").isPresent());
    }

    // ---------- ERROR ----------
    @Test
    void guardarProductoSinNombreLanzaError() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Snacks");
        categoria = categoriaRepository.save(categoria);

        Producto producto = new Producto();
        producto.setPrecio(500.0);
        producto.setStock(5);
        producto.setCategoria(categoria);
        // nombre es obligatorio (nullable = false)

        assertThrows(Exception.class, () -> productoRepository.saveAndFlush(producto));
    }

    @Test
    void guardarProductoSinCategoriaLanzaError() {
        Producto producto = new Producto();
        producto.setNombre("Sin categoria");
        producto.setPrecio(500.0);
        producto.setStock(5);
        // categoria es obligatoria (FK nullable = false)

        assertThrows(Exception.class, () -> productoRepository.saveAndFlush(producto));
    }

    @Test
    void usernameDuplicadoViolaRestriccionDeUnicidad() {
        Usuario u1 = new Usuario();
        u1.setUsername("repetido");
        u1.setPassword("a");
        usuarioRepository.saveAndFlush(u1);

        Usuario u2 = new Usuario();
        u2.setUsername("repetido");
        u2.setPassword("b");

        assertThrows(Exception.class, () -> usuarioRepository.saveAndFlush(u2));
    }
}
