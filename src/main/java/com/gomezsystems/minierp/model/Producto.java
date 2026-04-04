package com.gomezsystems.minierp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(columnDefinition = "integer default 0")
    private Integer version;

    private String nombre;
    private Double precio;      // Precio unitario base
    private String categoria;
    private String imagen;      // URL o nombre de archivo miniatura
    
    @Column(length = 500)
    private String descripcion;

    @Column(columnDefinition = "boolean default false")
    private boolean esPesable;    // Control principal de balanza
    private Double precioPorKilo; // Sólo se usará si esPesable es true

    // Costo pagado al proveedor
    private Double precioCompra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    private Double stock;        // Inventario. Si vas a pesarlo en decimales, evalúa cambiar a Double.

    // Motor de Promociones
    private Boolean promocionActiva;
    private Double precioPromo;
    private java.time.LocalDate fechaInicioPromo;
    private java.time.LocalDate fechaFinPromo;

    // Escáner Múltiple de Productos (Cód. Barras Alias)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_codigos", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "codigo_barra", unique = true)
    private java.util.Set<String> codigosBarras = new java.util.HashSet<>();

    public Producto() {}

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isEsPesable() { return esPesable; }
    public void setEsPesable(boolean esPesable) { this.esPesable = esPesable; }

    public Double getPrecioPorKilo() { return precioPorKilo; }
    public void setPrecioPorKilo(Double precioPorKilo) { this.precioPorKilo = precioPorKilo; }

    public Double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(Double precioCompra) { this.precioCompra = precioCompra; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public Double getStock() { return stock; }
    public void setStock(Double stock) { this.stock = stock; }

    public Boolean getPromocionActiva() { return promocionActiva; }
    public void setPromocionActiva(Boolean promocionActiva) { this.promocionActiva = promocionActiva; }

    public Double getPrecioPromo() { return precioPromo; }
    public void setPrecioPromo(Double precioPromo) { this.precioPromo = precioPromo; }

    public java.time.LocalDate getFechaInicioPromo() { return fechaInicioPromo; }
    public void setFechaInicioPromo(java.time.LocalDate fechaInicioPromo) { this.fechaInicioPromo = fechaInicioPromo; }

    public java.time.LocalDate getFechaFinPromo() { return fechaFinPromo; }
    public void setFechaFinPromo(java.time.LocalDate fechaFinPromo) { this.fechaFinPromo = fechaFinPromo; }

    public java.util.Set<String> getCodigosBarras() { return codigosBarras; }
    public void setCodigosBarras(java.util.Set<String> codigosBarras) { this.codigosBarras = codigosBarras; }
}
