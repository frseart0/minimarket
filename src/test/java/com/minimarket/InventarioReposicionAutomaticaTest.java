package com.minimarket;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.EstadoOrdenCompra;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.OrdenCompraRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración end-to-end de la reposición automática de stock:
 * registrar un movimiento de inventario que deja el stock de una sucursal en
 * el nivel mínimo (o por debajo) debe generar automáticamente una orden de
 * compra PENDIENTE, sin afectar el stock de otras sucursales.
 *
 * Se ejecuta contra la base H2 real (no se mockea ninguna capa) y se marca
 * como {@code @Transactional} para revertir los cambios al finalizar cada prueba.
 */
@SpringBootTest
@Transactional
class InventarioReposicionAutomaticaTest {

    @Autowired
    private InventarioService inventarioService;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private StockSucursalRepository stockSucursalRepository;
    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    private Producto producto;
    private Sucursal sucursalA;
    private Sucursal sucursalB;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.save(nuevaCategoria("Categoria reposicion test"));
        producto = productoRepository.save(nuevoProducto("Producto reposicion test", 500.0, 100, categoria));

        sucursalA = sucursalRepository.save(nuevaSucursal("Sucursal reposicion A"));
        sucursalB = sucursalRepository.save(nuevaSucursal("Sucursal reposicion B"));

        stockSucursalRepository.save(nuevoStock(producto, sucursalA, 10, 5));
        stockSucursalRepository.save(nuevoStock(producto, sucursalB, 20, 5));
    }

    @Test
    void unaSalidaQueDejaElStockEnElMinimoGeneraOrdenDeCompraAutomatica() {
        long ordenesAntes = ordenCompraRepository.count();

        registrarSalida(producto, sucursalA, 5); // 10 - 5 = 5 (== minimo)

        StockSucursal stockActualizado = stockSucursalRepository
                .findByProductoIdAndSucursalId(producto.getId(), sucursalA.getId()).orElseThrow();
        assertEquals(5, stockActualizado.getCantidad());

        List<OrdenCompra> ordenes = ordenCompraRepository.findAll();
        assertEquals(ordenesAntes + 1, ordenes.size());

        OrdenCompra generada = ordenes.get(ordenes.size() - 1);
        assertTrue(generada.isAutomatica());
        assertEquals(EstadoOrdenCompra.PENDIENTE, generada.getEstado());
        assertEquals(producto.getId(), generada.getProducto().getId());
        assertEquals(sucursalA.getId(), generada.getSucursal().getId());
    }

    @Test
    void unaSalidaQueNoLlegaAlMinimoNoGeneraOrdenDeCompra() {
        long ordenesAntes = ordenCompraRepository.count();

        registrarSalida(producto, sucursalA, 2); // 10 - 2 = 8 (> minimo de 5)

        assertEquals(ordenesAntes, ordenCompraRepository.count());
    }

    @Test
    void laReposicionEnUnaSucursalNoAfectaElStockDeOtraSucursal() {
        registrarSalida(producto, sucursalA, 8); // deja sucursalA en 2 (bajo el minimo)

        StockSucursal stockB = stockSucursalRepository
                .findByProductoIdAndSucursalId(producto.getId(), sucursalB.getId()).orElseThrow();
        assertEquals(20, stockB.getCantidad(), "El stock de la sucursal B no debe cambiar");
    }

    @Test
    void unaEntradaDeInventarioAumentaElStockYNoGeneraOrdenDeCompra() {
        long ordenesAntes = ordenCompraRepository.count();

        Inventario entrada = new Inventario();
        entrada.setProducto(producto);
        entrada.setSucursal(sucursalA);
        entrada.setCantidad(15);
        entrada.setTipoMovimiento("ENTRADA");
        entrada.setFechaMovimiento(new Date());
        inventarioService.save(entrada);

        StockSucursal stockActualizado = stockSucursalRepository
                .findByProductoIdAndSucursalId(producto.getId(), sucursalA.getId()).orElseThrow();
        assertEquals(25, stockActualizado.getCantidad());
        assertEquals(ordenesAntes, ordenCompraRepository.count());
    }

    private void registrarSalida(Producto producto, Sucursal sucursal, int cantidad) {
        Inventario salida = new Inventario();
        salida.setProducto(producto);
        salida.setSucursal(sucursal);
        salida.setCantidad(cantidad);
        salida.setTipoMovimiento("SALIDA");
        salida.setFechaMovimiento(new Date());
        inventarioService.save(salida);
    }

    private Categoria nuevaCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoria;
    }

    private Producto nuevoProducto(String nombre, double precio, int stock, Categoria categoria) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setCategoria(categoria);
        return producto;
    }

    private Sucursal nuevaSucursal(String nombre) {
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(nombre);
        sucursal.setDireccion("Direccion de prueba");
        sucursal.setComuna("Comuna de prueba");
        return sucursal;
    }

    private StockSucursal nuevoStock(Producto producto, Sucursal sucursal, int cantidad, int stockMinimo) {
        StockSucursal stock = new StockSucursal();
        stock.setProducto(producto);
        stock.setSucursal(sucursal);
        stock.setCantidad(cantidad);
        stock.setStockMinimo(stockMinimo);
        return stock;
    }
}
